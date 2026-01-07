package com.coedit.controller;

import com.coedit.common.Result;
import com.coedit.common.UserSessionManager;
import com.coedit.dto.CommentDto;
import com.coedit.dto.UserDto;
import com.coedit.entity.Comment;
import com.coedit.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserSessionManager sessionManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private UserDto getUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return sessionManager.getUser(token);
    }

    @GetMapping("/documents/{documentId}/comments")
    public Result<List<CommentDto>> getComments(@PathVariable Long documentId) {
        return Result.success(commentService.getComments(documentId));
    }

    @PostMapping("/documents/{documentId}/comments")
    public Result<Comment> addComment(@PathVariable Long documentId, @RequestBody Map<String, String> payload, HttpServletRequest request) {
        UserDto currentUser = getUser(request);
        if (currentUser == null) return Result.error(401, "Not logged in");

        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "Content cannot be empty");
        }

        Comment comment = new Comment();
        comment.setDocumentId(documentId);
        comment.setUserId(currentUser.getId());
        comment.setContent(content);

        Comment savedComment = commentService.addComment(comment);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMENT_UPDATE");
        message.put("action", "ADD");
        messagingTemplate.convertAndSend("/topic/doc/" + documentId, message);

        return Result.success(savedComment);
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<String> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        UserDto currentUser = getUser(request);
        if (currentUser == null) return Result.error(401, "Not logged in");

        Long docId = commentService.deleteComment(commentId, currentUser.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "COMMENT_UPDATE");
        message.put("action", "DELETE");
        message.put("commentId", commentId);
        messagingTemplate.convertAndSend("/topic/doc/" + docId, message);

        return Result.success("Deleted successfully");
    }
}
