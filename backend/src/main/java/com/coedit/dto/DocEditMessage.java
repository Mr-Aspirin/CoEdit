package com.coedit.dto;

import lombok.Data;

@Data
public class DocEditMessage {
    private Long docId;
    private String content; // Could be delta or full HTML
    private Long version;
    private Long senderId;
    private String senderName;
    private Object delta; // For Quill delta
    
    // New fields for collaboration
    private String type; // "EDIT", "CURSOR"
    private Integer cursorIndex;
    private Integer cursorLength;
    private String senderColor; // Optional, or determined by frontend

}
