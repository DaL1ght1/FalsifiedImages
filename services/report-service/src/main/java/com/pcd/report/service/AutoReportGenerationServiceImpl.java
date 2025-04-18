/*package com.pcd.report.service;

import com.pcd.report.enums.ReportStatus;
import com.pcd.report.model.Report;
import com.pcd.report.model.ReportTemplate;
import com.pcd.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutoReportGenerationServiceImpl {

    private final ReportRepository reportRepository;
    private final TemplateServiceImpl templateService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    public Report generateReportFromAnalysisResults(String imageId, Map<String, Object> analysisResults,
                                                    List<String> detectedManipulations, String caseNumber) {
        // Use default template
        ReportTemplate defaultTemplate = templateService.getDefaultTemplate();
        return generateReportFromAnalysisResults(imageId, analysisResults, detectedManipulations, caseNumber, defaultTemplate.getId());
    }


    public Report generateReportFromAnalysisResults(String imageId, Map<String, Object> analysisResults,
                                                    List<String> detectedManipulations, String caseNumber,
                                                    String templateId) {
        ReportTemplate template = templateService.getTemplateById(templateId);

        // Create a new report
        Report report = new Report();
        report.setTitle("Image Falsification Analysis Report");
        report.setDescription(generateReportDescription(detectedManipulations));
        report.setImageId(imageId);
        report.setCaseNumber(caseNumber);
        report.setDetectedManipulations(detectedManipulations);
        report.setAnalysisResults(analysisResults);
        report.setStatus(ReportStatus.DRAFT);
        report.setAutoGenerated(true);
        report.setTemplateId(template.getId());
        report.setTemplateName(template.getName());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        // Get the template content (either from file or database)
        String templateContent = templateService.getTemplateContent(template);

        // Generate HTML content
        Map<String, Object> templateData = prepareReportDataFromAnalysisResults(imageId, analysisResults, detectedManipulations);
        templateData.put("caseNumber", caseNumber);
        templateData.put("createdAt", report.getCreatedAt().format(DATE_FORMATTER));
        templateData.put("id", "TBD - Will be set after saving");
        templateData.put("status", report.getStatus().toString());
        templateData.put("expertName", "Auto-Generated");
        templateData.put("description", report.getDescription());

        String htmlContent = templateService.processTemplate(templateContent, templateData);
        report.setHtmlContent(htmlContent);

        // Save the report
        Report savedReport = reportRepository.save(report);

        // Update the HTML content with the correct ID
        templateData.put("id", savedReport.getId());
        htmlContent = templateService.processTemplate(templateContent, templateData);
        savedReport.setHtmlContent(htmlContent);

        return reportRepository.save(savedReport);
    }


    public String renderReportToHtml(Report report, Map<String, Object> additionalData) {
        // Get the template
        ReportTemplate template;
        if (report.getTemplateId() != null) {
            template = templateService.getTemplateById(report.getTemplateId());
        } else {
            template = templateService.getDefaultTemplate();
        }

        // Get the template content from file or database
        String templateContent = templateService.getTemplateContent(template);

        // Prepare the data
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("title", report.getTitle());
        templateData.put("description", report.getDescription());
        templateData.put("caseNumber", report.getCaseNumber());
        templateData.put("imageId", report.getImageId());
        templateData.put("createdAt", report.getCreatedAt().format(DATE_FORMATTER));
        templateData.put("updatedAt", report.getUpdatedAt().format(DATE_FORMATTER));
        templateData.put("id", report.getId());
        templateData.put("status", report.getStatus().toString());

        // Format detected manipulations
        StringBuilder manipulationsHtml = new StringBuilder();
        if (report.getDetectedManipulations() != null && !report.getDetectedManipulations().isEmpty()) {
            manipulationsHtml.append("<ul>");
            for (String manipulation : report.getDetectedManipulations()) {
                manipulationsHtml.append("<li class=\"manipulation-found\">").append(manipulation).append("</li>");
            }
            manipulationsHtml.append("</ul>");
        } else {
            manipulationsHtml.append("<p class=\"manipulation-not-found\">No manipulations detected.</p>");
        }
        templateData.put("detectedManipulations", manipulationsHtml.toString());

        // Format analysis results
        StringBuilder analysisResultsHtml = new StringBuilder();
        if (report.getAnalysisResults() != null && !report.getAnalysisResults().isEmpty()) {
            analysisResultsHtml.append("<table style=\"width:100%; border-collapse: collapse;\">");
            analysisResultsHtml.append("<tr><th style=\"text-align:left; padding:8px; border:1px solid #ddd;\">Method</th><th style=\"text-align:left; padding:8px; border:1px solid #ddd;\">Result</th></tr>");

            for (Map.Entry<String, Object> entry : report.getAnalysisResults().entrySet()) {
                analysisResultsHtml.append("<tr>");
                analysisResultsHtml.append("<td style=\"padding:8px; border:1px solid #ddd;\">").append(entry.getKey()).append("</td>");
                analysisResultsHtml.append("<td style=\"padding:8px; border:1px solid #ddd;\">").append(entry.getValue()).append("</td>");
                analysisResultsHtml.append("</tr>");
            }

            analysisResultsHtml.append("</table>");
        } else {
            analysisResultsHtml.append("<p>No detailed analysis results available.</p>");
        }
        templateData.put("analysisResults", analysisResultsHtml.toString());

        // Format comments
        StringBuilder commentsHtml = new StringBuilder();
        if (report.getComments() != null && !report.getComments().isEmpty()) {
            for (String comment : report.getComments()) {
                commentsHtml.append("<div class=\"comment\">").append(comment).append("</div>");
            }
        } else {
            commentsHtml.append("<p>No expert comments available.</p>");
        }
        templateData.put("comments", commentsHtml.toString());

        // Add conclusion based on detected manipulations
        String conclusion = (report.getDetectedManipulations() != null && !report.getDetectedManipulations().isEmpty())
                ? "<span class=\"manipulation-found\">Image appears to be falsified.</span>"
                : "<span class=\"manipulation-not-found\">No evidence of falsification detected.</span>";
        templateData.put("conclusion", conclusion);

        // Add any additional data
        if (additionalData != null) {
            templateData.putAll(additionalData);
        }

        // Process the template
        return templateService.processTemplate(templateContent, templateData);
    }

    // Missing method implementation (adding here)
    private String generateReportDescription(List<String> detectedManipulations) {
        if (detectedManipulations == null || detectedManipulations.isEmpty()) {
            return "Automated image analysis detected no evidence of falsification.";
        } else {
            return "Automated image analysis detected potential falsification. " +
                    "Found " + detectedManipulations.size() + " suspect manipulation(s).";
        }
    }


    public Map<String, Object> prepareReportDataFromAnalysisResults(String imageId, Map<String, Object> analysisResults,
                                                                    List<String> detectedManipulations) {
        Map<String, Object> data = new HashMap<>();

        // Basic data
        data.put("imageId", imageId);
        data.put("title", "Image Falsification Analysis Report");
        data.put("imageUrl", "/api/images/" + imageId); // Assuming this is the URL pattern to view the image

        // Format detected manipulations
        StringBuilder manipulationsHtml = new StringBuilder();
        if (detectedManipulations != null && !detectedManipulations.isEmpty()) {
            manipulationsHtml.append("<ul>");
            for (String manipulation : detectedManipulations) {
                manipulationsHtml.append("<li class=\"manipulation-found\">").append(manipulation).append("</li>");
            }
            manipulationsHtml.append("</ul>");
        } else {
            manipulationsHtml.append("<p class=\"manipulation-not-found\">No manipulations detected.</p>");
        }
        data.put("detectedManipulations", manipulationsHtml.toString());

        // Format analysis results
        StringBuilder analysisResultsHtml = new StringBuilder();
        if (analysisResults != null && !analysisResults.isEmpty()) {
            analysisResultsHtml.append("<table style=\"width:100%; border-collapse: collapse;\">");
            analysisResultsHtml.append("<tr><th style=\"text-align:left; padding:8px; border:1px solid #ddd;\">Method</th><th style=\"text-align:left; padding:8px; border:1px solid #ddd;\">Result</th></tr>");

            for (Map.Entry<String, Object> entry : analysisResults.entrySet()) {
                analysisResultsHtml.append("<tr>");
                analysisResultsHtml.append("<td style=\"padding:8px; border:1px solid #ddd;\">").append(entry.getKey()).append("</td>");
                analysisResultsHtml.append("<td style=\"padding:8px; border:1px solid #ddd;\">").append(entry.getValue()).append("</td>");
                analysisResultsHtml.append("</tr>");
            }

            analysisResultsHtml.append("</table>");
        } else {
            analysisResultsHtml.append("<p>No detailed analysis results available.</p>");
        }
        data.put("analysisResults", analysisResultsHtml.toString());

        // Add conclusion based on detected manipulations
        String conclusion = (detectedManipulations != null && !detectedManipulations.isEmpty())
                ? "<span class=\"manipulation-found\">Image appears to be falsified.</span>"
                : "<span class=\"manipulation-not-found\">No evidence of falsification detected.</span>";
        data.put("conclusion", conclusion);

        return data;
    }
}
*/