package com.pcd.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "report_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplate {

    @Id
    private String id;

    private String name;

    private String description;

    private String content;

    private String filePath;

    private boolean isDefault;

    private String createdBy;


    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private  String HtmlTemplate;


}
