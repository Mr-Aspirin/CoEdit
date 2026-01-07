package com.coedit.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String account;
    private String securityAnswer;
    private String newPassword;
}
