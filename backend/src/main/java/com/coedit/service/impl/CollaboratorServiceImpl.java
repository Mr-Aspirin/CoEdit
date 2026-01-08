package com.coedit.service.impl;

import com.coedit.dto.CollaboratorDto;
import com.coedit.entity.Document;
import com.coedit.entity.DocumentCollaborator;
import com.coedit.entity.User;
import com.coedit.mapper.CollaboratorMapper;
import com.coedit.mapper.DocumentMapper;
import com.coedit.mapper.UserMapper;
import com.coedit.service.CollaboratorService;
import com.coedit.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollaboratorServiceImpl implements CollaboratorService {
    @Autowired
    private CollaboratorMapper collaboratorMapper;
    
    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void addCollaborator(Long documentId, Long userId, String permission) {
        if (collaboratorMapper.findByDocAndUser(documentId, userId) != null) {
            throw new RuntimeException("User is already a collaborator");
        }
        DocumentCollaborator collaborator = new DocumentCollaborator();
        collaborator.setDocumentId(documentId);
        collaborator.setUserId(userId);
        collaborator.setPermission(permission);
        collaborator.setStatus("PENDING");
        collaboratorMapper.insert(collaborator);

        Document document = documentMapper.findById(documentId);
        String docName = document != null ? document.getTitle() : "#" + documentId;
        notificationService.createNotification(userId, "You have been invited to collaborate on document " + docName, "INVITE", documentId);
    }

    @Override
    public void acceptInvitation(Long documentId, Long userId) {
        DocumentCollaborator collaborator = collaboratorMapper.findByDocAndUser(documentId, userId);
        if (collaborator == null) {
            throw new RuntimeException("Invitation not found");
        }
        collaboratorMapper.updateStatus(documentId, userId, "ACCEPTED");

        notificationService.markInvitationAsAccepted(userId, documentId);

        User user = userMapper.findById(userId);
        String userName = (user != null) ? (user.getName() != null ? user.getName() : user.getAccount()) : "Unknown User";
        Document document = documentMapper.findById(documentId);
        String docName = document != null ? document.getTitle() : "#" + documentId;
        
        notifyOwnerAndAdmins(documentId, userName + " has accepted the invitation to collaborate on " + docName, "INVITE_ACCEPTED");
    }

    private void checkManagePermission(Long documentId, Long operatorId) {
        Document document = documentMapper.findById(documentId);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        if (document.getOwnerId().equals(operatorId)) return;
        
        DocumentCollaborator operator = collaboratorMapper.findByDocAndUser(documentId, operatorId);
        if (operator != null && "ADMIN".equals(operator.getPermission()) && "ACCEPTED".equals(operator.getStatus())) {
            return;
        }
        
        throw new RuntimeException("No permission to manage collaborators");
    }

    @Override
    public void inviteCollaborator(Long documentId, String identifier, String permission, Long operatorId) {
        checkManagePermission(documentId, operatorId);
        
        User user = userMapper.findByAccount(identifier);
        if (user == null) {
            throw new RuntimeException("User not found: " + identifier);
        }
        
        addCollaborator(documentId, user.getId(), permission);
    }

    @Override
    public void removeCollaborator(Long documentId, Long userId, Long actorId) {
        if (!userId.equals(actorId)) {
            checkManagePermission(documentId, actorId);
        }

        Document document = documentMapper.findById(documentId);
        String docName = document != null ? document.getTitle() : "#" + documentId;
        User user = userMapper.findById(userId);
        String userName = (user != null) ? (user.getName() != null ? user.getName() : user.getAccount()) : "Unknown User";

        collaboratorMapper.delete(documentId, userId);

        if (actorId.equals(userId)) {
            notifyOwnerAndAdmins(documentId, userName + " has left the document " + docName, "COLLABORATOR_LEFT");
        } else {
            notificationService.createNotification(userId, "You have been removed from document " + docName, "REMOVED_FROM_DOC", documentId);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "COLLABORATOR_REMOVED");
        message.put("targetUserId", userId);
        message.put("documentId", documentId);
        messagingTemplate.convertAndSend("/topic/doc/" + documentId, message);
    }

    private void notifyOwnerAndAdmins(Long documentId, String content, String type) {
        Document document = documentMapper.findById(documentId);
        if (document == null) return;

        notificationService.createNotification(document.getOwnerId(), content, type, documentId);

        List<DocumentCollaborator> collaborators = collaboratorMapper.findByDocumentId(documentId);
        for (DocumentCollaborator dc : collaborators) {
            if ("ACCEPTED".equals(dc.getStatus()) && "ADMIN".equals(dc.getPermission())) {
                notificationService.createNotification(dc.getUserId(), content, type, documentId);
            }
        }
    }

    @Override
    public void updatePermission(Long documentId, Long userId, String permission, Long operatorId) {
        checkManagePermission(documentId, operatorId);
        collaboratorMapper.updatePermission(documentId, userId, permission);
        Document document = documentMapper.findById(documentId);
        String docName = document != null ? document.getTitle() : "#" + documentId;
        notificationService.createNotification(userId, "Your permission for document " + docName + " has been updated to " + permission, "PERMISSION_UPDATE", documentId);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "PERMISSION_UPDATE");
        message.put("targetUserId", userId);
        message.put("permission", permission);
        message.put("documentId", documentId);
        messagingTemplate.convertAndSend("/topic/doc/" + documentId, message);
    }

    @Override
    public List<CollaboratorDto> getCollaborators(Long documentId) {
        return collaboratorMapper.findByDocumentId(documentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CollaboratorDto convertToDto(DocumentCollaborator collaborator) {
        CollaboratorDto dto = new CollaboratorDto();
        BeanUtils.copyProperties(collaborator, dto);
        User user = userMapper.findById(collaborator.getUserId());
        if (user != null) {
            dto.setAccount(user.getAccount());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setAvatar(user.getAvatar());
            dto.setIntro(user.getIntro());
        }
        return dto;
    }
}
