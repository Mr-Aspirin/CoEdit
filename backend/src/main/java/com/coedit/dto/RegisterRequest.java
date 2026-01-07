package com.coedit.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String account;
    private String name;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
}
