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
        return message;
    }
}
