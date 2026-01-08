package com.coedit.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentCollaborator {
    private Long id;
    private Long documentId;
    private Long userId;
    private String permission; // ADMIN, EDITOR, VIEWER
    private String status;
    private LocalDateTime createdAt;
}
