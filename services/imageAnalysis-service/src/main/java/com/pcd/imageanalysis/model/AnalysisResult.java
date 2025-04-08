package com.pcd.imageanalysis.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "image_analysis_results")
public class AnalysisResult {


    @Id
    private String id;

    @Indexed
    private String imageId;

    @Indexed
    private String caseId;

    private Instant analysisTimestamp;

    private String analysisStatus; // "COMPLETED", "FAILED", "IN_PROGRESS"

    private Boolean isFalsified;

    private Double confidenceScore;

    private String analysisType; // "AUTOMATIC", "MANUAL", etc.

    private String analysisVersion; // Version of the model used

    // Specific details about the falsification detected
    private Map<String, Object> detectionDetails = new HashMap<>();

    // Any error messages if analysis failed
    private String errorMessage;

    // Reference to the analyst who performed/reviewed the analysis
    private String analystId;


}

