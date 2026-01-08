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

    private String userName;
    private String userAvatar;
    private String userAccount;

    private String permission; 

    private java.util.Map<Long, String> mentionedUsers;
}
