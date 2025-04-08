package com.pcd.report.model;

import com.pcd.report.enums.ReportStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Setter
@Getter

public class ReportResponse {

    // Getters and setters
    private String id;
    private String title;
    private String description;
    private String imageId;
    private String expertId;
    private String caseNumber;
    private List<String> detectedManipulations;
    private Map<String, Object> analysisResults;
    private List<String> comments;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finalizedAt;

}