package com.coedit.service.impl;

import com.coedit.dto.CommentDto;
import com.coedit.entity.Comment;
import com.coedit.entity.Document;
import com.coedit.entity.DocumentCollaborator;
import com.coedit.mapper.CollaboratorMapper;
import com.coedit.mapper.CommentMapper;
import com.coedit.mapper.DocumentMapper;
import com.coedit.mapper.UserMapper;
import com.coedit.service.CommentService;
import com.coedit.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private CollaboratorMapper collaboratorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        Document doc = documentMapper.findById(comment.getDocumentId());
        if (doc == null) {
            throw new RuntimeException("Document not found");
        }
        
        boolean hasPermission = false;

        if (doc.getOwnerId().equals(comment.getUserId())) {
            hasPermission = true;
        } else {
            DocumentCollaborator collaborator = collaboratorMapper.findByDocAndUser(comment.getDocumentId(), comment.getUserId());
            if (collaborator != null && "ACCEPTED".equals(collaborator.getStatus())) {
                hasPermission = true;
            }
        }
        
        if (!hasPermission) {
            throw new RuntimeException("No permission to comment");
        }

        commentMapper.insert(comment);
        processMentions(comment);

        return comment;
    }

    private void processMentions(Comment comment) {
        if (comment.getContent() == null) return;

        Pattern pattern = Pattern.compile("@\\{uid:(\\d+)\\}");
        Matcher matcher = pattern.matcher(comment.getContent());

        com.coedit.entity.User commenter = userMapper.findById(comment.getUserId());
        String commenterName = (commenter != null) ? (commenter.getName() != null ? commenter.getName() : commenter.getAccount()) : "Someone";
        Document doc = documentMapper.findById(comment.getDocumentId());
        String docTitle = (doc != null) ? doc.getTitle() : "document";

        Set<Long> mentionedUserIds = new HashSet<>();

        while (matcher.find()) {
            try {
                Long mentionedUserId = Long.parseLong(matcher.group(1));
                if (!mentionedUserId.equals(comment.getUserId()) && !mentionedUserIds.contains(mentionedUserId)) {
                    mentionedUserIds.add(mentionedUserId);

                    String content = String.format("%s mentioned you in \"%s\"", commenterName, docTitle);
                    notificationService.createNotification(mentionedUserId, content, "MENTION", comment.getDocumentId());
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    @Override
    public List<CommentDto> getComments(Long documentId) {
        List<CommentDto> comments = commentMapper.selectByDocumentId(documentId);
        if (comments == null || comments.isEmpty()) return comments;

        Set<Long> userIds = new HashSet<>();
        Pattern pattern = Pattern.compile("@\\{uid:(\\d+)\\}");

        for (CommentDto comment : comments) {
            if (comment.getContent() != null) {
                Matcher matcher = pattern.matcher(comment.getContent());
                while (matcher.find()) {
                    try {
                        userIds.add(Long.parseLong(matcher.group(1)));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        if (userIds.isEmpty()) return comments;

        Map<Long, String> userNames = new HashMap<>();
        for (Long uid : userIds) {
            com.coedit.entity.User u = userMapper.findById(uid);
            if (u != null) {
                userNames.put(uid, u.getName() != null ? u.getName() : u.getAccount());
            } else {
                userNames.put(uid, "Unknown User");
            }
        }

        for (CommentDto comment : comments) {
            if (comment.getContent() != null) {
                 Matcher matcher = pattern.matcher(comment.getContent());
                 Map<Long, String> mentions = new HashMap<>();
                 while (matcher.find()) {
                     try {
                         Long uid = Long.parseLong(matcher.group(1));
                         if (userNames.containsKey(uid)) {
                             mentions.put(uid, userNames.get(uid));
                         }
                     } catch (Exception e) {}
                 }
                 comment.setMentionedUsers(mentions);
            }
        }

        return comments;
    }

    @Override
    @Transactional
    public Long deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("Comment not found");
        }

        boolean canDelete = false;

        if (comment.getUserId().equals(userId)) {
            canDelete = true;
        } else {
            Document doc = documentMapper.findById(comment.getDocumentId());
            if (doc != null && doc.getOwnerId().equals(userId)) {
                canDelete = true;
            } else {
                DocumentCollaborator collaborator = collaboratorMapper.findByDocAndUser(comment.getDocumentId(), userId);
                if (collaborator != null && "ADMIN".equals(collaborator.getPermission())) {
                    canDelete = true;
                }
            }
        }

        if (!canDelete) {
            throw new RuntimeException("No permission to delete this comment");
        }

        commentMapper.deleteById(commentId);
        return comment.getDocumentId();
    }
}
