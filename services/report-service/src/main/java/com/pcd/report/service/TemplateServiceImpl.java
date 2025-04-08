package com.pcd.report.service;

import com.pcd.report.exception.TemplateNotFoundException;
import com.pcd.report.model.ReportTemplate;
import com.pcd.report.repository.ReportTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl {

    private final ReportTemplateRepository templateRepository;
    private final ResourceLoader resourceLoader;
    private final TemplateEngine templateEngine;

    @Value("${app.templates.directory:classpath:templates/reports/}")
    private String templatesDirectory;

    public ReportTemplate getDefaultTemplate() {
        return templateRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new TemplateNotFoundException("Default template not found"));
    }

    public ReportTemplate getTemplateById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + id));
    }

    public List<ReportTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public ReportTemplate createTemplate(ReportTemplate template) {
        // If this is set as default, unset any existing default
        if (template.isDefault()) {
            Optional<ReportTemplate> existingDefault = templateRepository.findByIsDefaultTrue();
            existingDefault.ifPresent(t -> {
                t.setDefault(false);
                templateRepository.save(t);
            });
        }

        return templateRepository.save(template);
    }

    public ReportTemplate updateTemplate(String id, ReportTemplate template) {
        ReportTemplate existingTemplate = getTemplateById(id);

        existingTemplate.setName(template.getName());
        existingTemplate.setDescription(template.getDescription());
        existingTemplate.setContent(template.getContent());
        existingTemplate.setFilePath(template.getFilePath());

        // Handle default flag
        if (template.isDefault() && !existingTemplate.isDefault()) {
            // Unset any existing default
            Optional<ReportTemplate> currentDefault = templateRepository.findByIsDefaultTrue();
            currentDefault.ifPresent(t -> {
                t.setDefault(false);
                templateRepository.save(t);
            });
            existingTemplate.setDefault(true);
        } else if (!template.isDefault() && existingTemplate.isDefault()) {
            // Don't allow unsetting default if it's the only template
            long count = templateRepository.count();
            if (count > 1) {
                existingTemplate.setDefault(false);
            }
        }

        return templateRepository.save(existingTemplate);
    }

    public void deleteTemplate(String id) {
        ReportTemplate template = getTemplateById(id);

        // Don't allow deleting the default template if it's the only one
        if (template.isDefault()) {
            long count = templateRepository.count();
            if (count <= 1) {
                throw new IllegalStateException("Cannot delete the only default template");
            }

            // Find another template to set as default
            List<ReportTemplate> templates = templateRepository.findAll();
            for (ReportTemplate t : templates) {
                if (!t.getId().equals(id)) {
                    t.setDefault(true);
                    templateRepository.save(t);
                    break;
                }
            }
        }

        templateRepository.delete(template);
    }

    public String getTemplateContent(ReportTemplate template) {
        // If content is stored in the database, return it
        if (template.getContent() != null && !template.getContent().isEmpty()) {
            return template.getContent();
        }

        // Otherwise, try to load from file
        if (template.getFilePath() != null && !template.getFilePath().isEmpty()) {
            try {
                if (template.getFilePath().startsWith("classpath:")) {
                    Resource resource = resourceLoader.getResource(template.getFilePath());
                    return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                } else {
                    Path path = Paths.get(template.getFilePath());
                    return Files.readString(path, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.error("Failed to read template file: {}", template.getFilePath(), e);
                throw new TemplateNotFoundException("Template file not found: " + template.getFilePath());
            }
        }

        // If no content or file path, throw exception
        throw new TemplateNotFoundException("Template has no content or file path: " + template.getId());
    }

    public String processTemplate(String templateContent, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);

        return templateEngine.process(templateContent, context);
    }
}
