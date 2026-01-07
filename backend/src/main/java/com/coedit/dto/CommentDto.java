package com.coedit.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private Long documentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    
    // User info
    private String userName;
    private String userAvatar;
    private String userAccount;
    
    // Permission in this document
    private String permission; 
    
    // Mentioned users info (id -> name)
    private java.util.Map<Long, String> mentionedUsers;
}
