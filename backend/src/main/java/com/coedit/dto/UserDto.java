package com.coedit.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String account;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String intro;
    private String securityQuestion;
    private String token;
}
