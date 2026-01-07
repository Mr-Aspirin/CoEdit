package com.coedit.dto;

import lombok.Data;

@Data
public class CollaboratorDto {
    private Long id;
    private Long documentId;
    private Long userId;
    private String account;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String intro;
    private String permission;
    private String status;
}
