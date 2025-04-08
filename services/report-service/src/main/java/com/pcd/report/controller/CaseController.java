package com.pcd.report.controller;

import com.pcd.report.dto.*;
import com.pcd.report.model.CaseStatus;
import com.pcd.report.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report/cases")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    @PreAuthorize("hasRole('INVESTIGATOR')")
    public ResponseEntity<CaseDTO> createCase(@Valid @RequestBody CaseCreationRequest request) {
        log.info("REST request to create a new case");
        CaseDTO createdCase = caseService.createCase(request);
        return new ResponseEntity<>(createdCase, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'EXPERT', 'JUDGE', 'ADMIN')")
        public ResponseEntity<CaseDTO> getCaseById(@PathVariable String id) {
        log.info("REST request to get case by ID: {}", id);
        CaseDTO caseDTO = caseService.getCaseById(id);
        return ResponseEntity.ok(caseDTO);
    }

    @GetMapping("/number/{caseNumber}")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'EXPERT', 'JUDGE', 'ADMIN')")
    public ResponseEntity<CaseDTO> getCaseByCaseNumber(@PathVariable String caseNumber) {
        log.info("REST request to get case by case number: {}", caseNumber);
        CaseDTO caseDTO = caseService.getCaseByCaseNumber(caseNumber);
        return ResponseEntity.ok(caseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'EXPERT', 'JUDGE', 'ADMIN')")
    public ResponseEntity<CaseDTO> updateCase(
            @PathVariable String id,
            @Valid @RequestBody CaseUpdateRequest request) {
        log.info("REST request to update case with ID: {}", id);
        CaseDTO updatedCase = caseService.updateCase(id, request);
        return ResponseEntity.ok(updatedCase);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCase(@PathVariable String id) {
        log.info("REST request to delete case with ID: {}", id);
        caseService.deleteCase(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'EXPERT', 'JUDGE', 'ADMIN')")
    public ResponseEntity<CaseSearchResponse> searchCases(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) String investigatorId,
            @RequestParam(required = false) String expertId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("REST request to search cases");
        CaseSearchResponse response = caseService.searchCases(
                title, status, investigatorId, expertId, startDate, endDate,
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/investigator/{investigatorId}")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'ADMIN')")
    public ResponseEntity<List<CaseDTO>> getCasesByInvestigator(
            @PathVariable String investigatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to get cases by investigator ID: {}", investigatorId);
        List<CaseDTO> cases = caseService.getCasesByInvestigator(investigatorId, page, size);
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/expert/{expertId}")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<List<CaseDTO>> getCasesByExpert(
            @PathVariable String expertId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to get cases by expert ID: {}", expertId);
        List<CaseDTO> cases = caseService.getCasesByExpert(expertId, page, size);
        return ResponseEntity.ok(cases);
    }

    @PostMapping("/{id}/assign-expert/{expertId}")
    @PreAuthorize("hasAnyRole('INVESTIGATOR', 'ADMIN')")
    public ResponseEntity<CaseDTO> assignExpert(
            @PathVariable String id,
            @PathVariable String expertId) {

        log.info("REST request to assign expert {} to case {}", expertId, id);
        CaseDTO updatedCase = caseService.assignExpert(id, expertId);
        return ResponseEntity.ok(updatedCase);
    }

    @PostMapping("/{id}/add-analysis/{analysisId}")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<CaseDTO> addAnalysisToCase(
            @PathVariable String id,
            @PathVariable String analysisId) {

        log.info("REST request to add analysis {} to case {}", analysisId, id);
        CaseDTO updatedCase = caseService.addAnalysisToCase(id, analysisId);
        return ResponseEntity.ok(updatedCase);
    }

    @PostMapping("/{id}/complete-analysis")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<CaseDTO> completeAnalysis(@PathVariable String id) {
        log.info("REST request to mark analysis as complete for case {}", id);
        CaseDTO updatedCase = caseService.completeAnalysis(id);
        return ResponseEntity.ok(updatedCase);
    }

    @PostMapping("/{id}/submit-for-review")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<CaseDTO> submitForReview(@PathVariable String id) {
        log.info("REST request to submit case {} for review", id);
        CaseDTO updatedCase = caseService.submitForReview(id);
        return ResponseEntity.ok(updatedCase);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('JUDGE', 'ADMIN')")
    public ResponseEntity<CaseDTO> completeCase(
            @PathVariable String id,
            @RequestParam String verdict,
            @RequestParam(required = false) String judicialNotes) {

        log.info("REST request to complete case {} with verdict", id);
        CaseDTO updatedCase = caseService.completeCase(id, verdict, judicialNotes);
        return ResponseEntity.ok(updatedCase);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CaseStatisticsDTO> getCaseStatistics() {
        log.info("REST request to get case statistics");
        CaseStatisticsDTO statistics = caseService.getCaseStatistics();
        return ResponseEntity.ok(statistics);
    }
}
