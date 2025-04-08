package com.pcd.report.controller;

import com.pcd.report.dto.ReportRequest;
import com.pcd.report.dto.ReportResponse;
import com.pcd.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@RequestBody ReportRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable String reportId) {
        ReportResponse report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<ReportResponse>> getReportsByCase(@PathVariable String caseId) {
        List<ReportResponse> reports = reportService.getReportsByCase(caseId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/export/{reportId}")
    public ResponseEntity<byte[]> exportReport(@PathVariable String reportId) {
        byte[] report = reportService.exportReport(reportId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report-" + reportId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }
}
