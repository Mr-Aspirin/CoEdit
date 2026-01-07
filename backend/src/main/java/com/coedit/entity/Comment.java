package com.coedit.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long documentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
