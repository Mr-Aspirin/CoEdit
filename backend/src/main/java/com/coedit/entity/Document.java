package com.coedit.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Document {
    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String permission;
    private Long folderId;
    private String folderName;
}
