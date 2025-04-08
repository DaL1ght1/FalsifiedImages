package com.pcd.report.service;

import com.pcd.report.dto.AnalysisDto;
import com.pcd.report.dto.ReportRequest;
import com.pcd.report.dto.ReportResponse;
import com.pcd.report.exception.CaseNotFoundException;
import com.pcd.report.exception.ReportNotFoundException;
import com.pcd.report.model.Case;
import com.pcd.report.model.CaseStatus;
import com.pcd.report.model.Report;
import com.pcd.report.repository.CaseRepository;
import com.pcd.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final RestTemplate restTemplate;
    private final CaseRepository caseRepository;
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final PdfGenerationService pdfGenerationService;

    public AnalysisDto getAnalysis(String analysisId) {
        log.debug("Fetching analysis with ID: {}", analysisId);
        try {
            // Update the service URL to use service discovery
            return restTemplate.getForObject(
                    "http://imageanalysis-service/api/v1/analysis/" + analysisId,
                    AnalysisDto.class
            );
        } catch (Exception e) {
            log.error("Error fetching analysis with ID: {}", analysisId, e);
            return null;
        }
    }

    public ReportResponse createReport(ReportRequest request) {
        log.info("Creating report for case ID: {}", request.getCaseId());

        // Verify case exists
        Case caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new CaseNotFoundException("Case not found with ID: " + request.getCaseId()));

        // Create report entity from request
        Report report = reportMapper.toEntity(request);

        // Set additional case information
        report.setCaseNumber(caseEntity.getCaseNumber());
        report.setInvestigatorId(caseEntity.getInvestigatorId());
        report.setExpertId(caseEntity.getAssignedExpertId());
        report.setStatus(caseEntity.getStatus().toString());

        // Fetch analyses if analysis IDs are provided
        List<Map<String, Object>> analysesData = new ArrayList<>();

        // Add analyses from IDs
        if (request.getAnalysisIds() != null && !request.getAnalysisIds().isEmpty()) {
            for (String analysisId : request.getAnalysisIds()) {
                AnalysisDto analysis = getAnalysis(analysisId);
                if (analysis != null) {
                    Map<String, Object> analysisMap = convertAnalysisToMap(analysis);
                    analysesData.add(analysisMap);
                }
            }
        }

        // Add custom analysis data if provided
        if (request.getCustomAnalysisData() != null && !request.getCustomAnalysisData().isEmpty()) {
            analysesData.addAll(request.getCustomAnalysisData());
        }

        report.setAnalyses(analysesData);

        // Generate PDF content
        byte[] pdfContent = pdfGenerationService.generatePdf(report);
        report.setPdfContent(pdfContent);

        // Save report
        Report savedReport = reportRepository.save(report);
        log.info("Report created successfully with ID: {}", savedReport.getId());

        return reportMapper.toResponse(savedReport);
    }

    public ReportResponse getReport(String reportId) {
        log.info("Fetching report with ID: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + reportId));

        return reportMapper.toResponse(report);
    }

    public List<ReportResponse> getReportsByCase(String caseId) {
        log.info("Fetching reports for case ID: {}", caseId);

        // Verify case exists
        if (!caseRepository.existsById(caseId)) {
            throw new CaseNotFoundException("Case not found with ID: " + caseId);
        }

        List<Report> reports = reportRepository.findByCaseId(caseId);

        return reports.stream()
                .map(reportMapper::toResponse)
                .collect(Collectors.toList());
    }

    public byte[] exportReport(String reportId) {
        log.info("Exporting report with ID: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + reportId));

        // If PDF content is already stored, return it
        if (report.getPdfContent() != null && report.getPdfContent().length > 0) {
            return report.getPdfContent();
        }

        // Otherwise, generate PDF
        byte[] pdfContent = pdfGenerationService.generatePdf(report);

        // Update report with PDF content
        report.setPdfContent(pdfContent);
        reportRepository.save(report);

        return pdfContent;
    }

    public List<ReportResponse> getReportsByInvestigator(String investigatorId) {
        log.info("Fetching reports for investigator ID: {}", investigatorId);

        List<Report> reports = reportRepository.findByInvestigatorId(investigatorId);

        return reports.stream()
                .map(reportMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertAnalysisToMap(AnalysisDto analysis) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", analysis.getId());
        map.put("imageId", analysis.getImageId());
        map.put("analysisType", analysis.getAnalysisType());
        map.put("isFalsified", analysis.isFalsified());
        map.put("confidenceScore", analysis.getConfidenceScore());
        map.put("detailedResults", analysis.getDetailedResults());
        map.put("analysisDate", analysis.getAnalysisDate());
        return map;
    }
}
