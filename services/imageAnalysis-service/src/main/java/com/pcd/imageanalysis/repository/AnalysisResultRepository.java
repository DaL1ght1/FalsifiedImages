package com.pcd.imageanalysis.repository;

import com.pcd.imageanalysis.model.AnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends MongoRepository<AnalysisResult, String> {

    Optional<AnalysisResult> findByImageId(String imageId);

    List<AnalysisResult> findByCaseId(String caseId);

    List<AnalysisResult> findByIsFalsified(Boolean isFalsified);

    @Query("{'confidenceScore': {$gte: ?0}}")
    List<AnalysisResult> findByConfidenceScoreGreaterThan(Double threshold);

    List<AnalysisResult> findByAnalysisStatus(String status);
}