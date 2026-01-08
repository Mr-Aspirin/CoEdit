package com.coedit.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentDto {
    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private String ownerName; // Extra field for display
    private String ownerAccount;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerIntro;
    private String ownerAvatar;
    private String permission; // OWNER, ADMIN, EDITOR, VIEWER
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Folder info
    private Long folderId;
    private String folderName;
}
