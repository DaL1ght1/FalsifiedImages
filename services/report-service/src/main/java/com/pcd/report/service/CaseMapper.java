package com.pcd.report.service;

import com.pcd.report.dto.CaseDTO;
import com.pcd.report.model.Case;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {

    public CaseDTO toDto(Case caseEntity) {
        if (caseEntity == null) {
            return null;
        }

        return CaseDTO.builder()
                .id(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .title(caseEntity.getTitle())
                .description(caseEntity.getDescription())
                .status(caseEntity.getStatus())
                .investigatorId(caseEntity.getInvestigatorId())
                .assignedExpertId(caseEntity.getAssignedExpertId())
                .imageIds(caseEntity.getImageIds())
                .analysisIds(caseEntity.getAnalysisIds())
                .verdict(caseEntity.getVerdict())
                .judicialNotes(caseEntity.getJudicialNotes())
                .createdAt(caseEntity.getCreatedAt())
                .updatedAt(caseEntity.getUpdatedAt())
                .closedAt(caseEntity.getClosedAt())
                .build();
    }

    public Case toEntity(CaseDTO dto) {
        if (dto == null) {
            return null;
        }

        return Case.builder()
                .id(dto.getId())
                .caseNumber(dto.getCaseNumber())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .investigatorId(dto.getInvestigatorId())
                .assignedExpertId(dto.getAssignedExpertId())
                .imageIds(dto.getImageIds())
                .analysisIds(dto.getAnalysisIds())
                .verdict(dto.getVerdict())
                .judicialNotes(dto.getJudicialNotes())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .closedAt(dto.getClosedAt())
                .build();
    }
}
