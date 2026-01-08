package com.coedit.controller;

import com.coedit.dto.DocEditMessage;
import com.coedit.entity.Document;
import com.coedit.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private DocumentMapper documentMapper;

    @MessageMapping("/doc/{docId}/edit")
    @SendTo("/topic/doc/{docId}")
    public DocEditMessage handleEdit(@DestinationVariable Long docId, DocEditMessage message) {
        // Here we should save to DB or just broadcast
        // For "Real-time", saving every keystroke to DB is heavy.
        // Usually we save asynchronously or debounce.
        // But for "Simple" requirement and "Simplified OT", maybe we just broadcast.
        // And the client calls "Save" API explicitly or auto-save via API.
        
        // However, if we want to support "Simplified OT: Version number + Last write wins",
        // we should probably check version here if we were persisting.
        // If we just broadcast, the clients handle the merge/display.
        
        // Let's assume this is just for broadcasting the live changes (Delta).
        // The persistence happens via the HTTP /api/document/update call.
        
        return message;
    }
}
