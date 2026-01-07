package com.coedit.dto;

import lombok.Data;

@Data
public class SecurityVerifyRequest {
    private Long userId;
    private String answer;
}
