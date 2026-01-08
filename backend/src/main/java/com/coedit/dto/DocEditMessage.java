package com.coedit.dto;

import lombok.Data;

@Data
public class DocEditMessage {
    private Long docId;
    private String content; 
    private Long version;
    private Long senderId;
    private String senderName;
    private Object delta; 

    private String type; 
    private Integer cursorIndex;
    private Integer cursorLength;
    private String senderColor; 
}
