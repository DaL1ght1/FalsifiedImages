package com.pcd.report.repository;

import com.pcd.report.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {

    List<Report> findByCaseId(String caseId);

    List<Report> findByInvestigatorId(String investigatorId);

    List<Report> findByExpertId(String expertId);
}
