package com.coedit.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String account;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private String intro;
    private String securityQuestion;
    private String securityAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
