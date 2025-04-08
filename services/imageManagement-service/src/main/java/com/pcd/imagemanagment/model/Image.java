package com.pcd.imagemanagment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "images")
public class Image {
    @Id
    private String id;

    private String originalFilename;
    private String contentType;
    private long fileSize;
    private int width;
    private int height;

    @Indexed
    private String caseId;

    @Indexed
    private String uploaderId;

    private String uploaderRole;

    private Instant uploadTimestamp;

    private String sha256Hash;

    private String gridFsFileId;


    private ImageMetadata metadata;

    // Chain of custody events
    private List<CustodyEvent> custodyTrail = new ArrayList<>();

    // Analysis status tracking
    private String analysisStatus;

}



