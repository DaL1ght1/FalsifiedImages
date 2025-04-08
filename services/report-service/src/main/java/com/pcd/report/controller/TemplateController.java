package com.pcd.report.controller;

import com.pcd.report.model.ReportTemplate;
import com.pcd.report.service.TemplateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateServiceImpl templateService;



    @GetMapping
    public ResponseEntity<List<ReportTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportTemplate> getTemplateById(@PathVariable String id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping("/default")
    public ResponseEntity<ReportTemplate> getDefaultTemplate() {
        return ResponseEntity.ok(templateService.getDefaultTemplate());
    }

    @PostMapping
    public ResponseEntity<ReportTemplate> createTemplate(@RequestBody ReportTemplate template) {
        return new ResponseEntity<>(templateService.createTemplate(template), HttpStatus.CREATED);
    }

    @PostMapping("/upload")
    public ResponseEntity<ReportTemplate> uploadTemplateFile(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("isDefault") boolean isDefault,
            @RequestParam("file") MultipartFile file) {

        try {
            // Create a new template with the file content
            ReportTemplate template = new ReportTemplate();
            template.setName(name);
            template.setDescription(description);
            template.setDefault(isDefault);
            template.setHtmlTemplate(new String(file.getBytes(), StandardCharsets.UTF_8));

            return new ResponseEntity<>(templateService.createTemplate(template), HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportTemplate> updateTemplate(
            @PathVariable String id,
            @RequestBody ReportTemplate template) {
        return ResponseEntity.ok(templateService.updateTemplate(id, template));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}