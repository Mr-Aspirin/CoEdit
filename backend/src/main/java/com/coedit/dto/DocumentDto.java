package com.coedit.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentDto {
    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private String ownerName; 
    private String ownerAccount;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerIntro;
    private String ownerAvatar;
    private String permission; 
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long folderId;
    private String folderName;
}
