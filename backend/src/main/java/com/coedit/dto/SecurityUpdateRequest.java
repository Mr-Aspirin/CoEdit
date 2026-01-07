package com.coedit.dto;

import lombok.Data;

@Data
public class SecurityUpdateRequest {
    private Long userId;
    private String newPassword;
    private String newQuestion;
    private String newAnswer;
}
