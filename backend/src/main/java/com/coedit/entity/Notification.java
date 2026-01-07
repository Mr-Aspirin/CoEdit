package com.coedit.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private String content;
    private String type;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    private Boolean accepted;
}
