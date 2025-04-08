package com.pcd.imageanalysis.controller;

import com.pcd.imageanalysis.model.AnalysisResult;
import com.pcd.imageanalysis.services.ImageAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/analysis")
public class ImageAnalysisController {
    private static final Logger log = LoggerFactory.getLogger(ImageAnalysisController.class);

    private final ImageAnalysisService analysisService;

    @Autowired
    public ImageAnalysisController(ImageAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/images/{imageId}")
    public ResponseEntity<AnalysisResult> analyzeImage(
            @PathVariable String imageId,
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest request) {

        log.info("Received analysis request for image ID: {} from user: {}", imageId, userId);

        try {
            return analysisService.analyzeImage(imageId)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Analysis failed to complete"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing analysis request for image ID: {}", imageId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process analysis request: " + e.getMessage());
        }
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<AnalysisResult> getAnalysisResult(@PathVariable String imageId) {
        return analysisService.getAnalysisResult(imageId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No analysis result found for image ID: " + imageId));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<Iterable<AnalysisResult>> getAnalysisResultsByCase(@PathVariable String caseId) {
        return ResponseEntity.ok(analysisService.getAnalysisResultsByCase(caseId));
    }
}