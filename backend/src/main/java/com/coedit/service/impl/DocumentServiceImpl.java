package com.coedit.service.impl;

import com.coedit.dto.DocumentDto;
import com.coedit.dto.CollaboratorDto;
import com.coedit.entity.Document;
import com.coedit.entity.DocumentCollaborator;
import com.coedit.entity.User;
import com.coedit.mapper.DocumentMapper;
import com.coedit.mapper.UserMapper;
import com.coedit.mapper.CollaboratorMapper;
import com.coedit.service.DocumentService;
import com.coedit.service.CollaboratorService;
import com.coedit.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentMapper documentMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CollaboratorMapper collaboratorMapper;

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public DocumentDto createDocument(DocumentDto documentDto, Long userId) {
        Document document = new Document();
        BeanUtils.copyProperties(documentDto, document);
        document.setOwnerId(userId);
        document.setVersion(1L);
        // Initial content if null
        if (document.getContent() == null) {
            document.setContent("");
        }
        documentMapper.insert(document);
        return convertToDto(document);
    }

    @Override
    public DocumentDto getDocument(Long id, Long userId) {
        Document document = documentMapper.findById(id);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }

        boolean hasAccess = false;
        if (document.getOwnerId().equals(userId)) {
            hasAccess = true;
        } else {
             DocumentCollaborator dc = collaboratorMapper.findByDocAndUser(id, userId);
             if (dc != null && "ACCEPTED".equals(dc.getStatus())) {
                 hasAccess = true;
             }
        }
        
        if (!hasAccess) {
             throw new RuntimeException("No permission to view this document");
        }

        return convertToDto(document);
    }

    @Override
    public DocumentDto updateDocument(Long id, DocumentDto documentDto, Long userId) {
        Document document = documentMapper.findById(id);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        
        boolean canEdit = false;
        if (document.getOwnerId().equals(userId)) {
            canEdit = true;
        } else {
            DocumentCollaborator dc = collaboratorMapper.findByDocAndUser(id, userId);
            // Allow ADMIN and EDITOR to edit, and check if status is ACCEPTED
            if (dc != null && "ACCEPTED".equals(dc.getStatus()) && 
               (dc.getPermission().equals("ADMIN") || dc.getPermission().equals("EDITOR"))) {
                canEdit = true;
            }
        }

        if (!canEdit) {
            throw new RuntimeException("No permission to edit");
        }
        
        if (documentDto.getTitle() != null) document.setTitle(documentDto.getTitle());
        if (documentDto.getContent() != null) document.setContent(documentDto.getContent());
        
        // Optimistic locking / Versioning
        if (documentDto.getVersion() != null) {
             // For simple OT/Last write wins, we might just increment
             document.setVersion(document.getVersion() + 1);
        } else {
             document.setVersion(document.getVersion() + 1);
        }

        documentMapper.update(document);
        return convertToDto(document);
    }

    @Override
    public void deleteDocument(Long id, Long userId) {
        Document document = documentMapper.findById(id);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        
        if (document.getOwnerId().equals(userId)) {
            // Owner: Hard delete and notify collaborators
            List<CollaboratorDto> collaborators = collaboratorService.getCollaborators(id);
            for (CollaboratorDto c : collaborators) {
                 notificationService.createNotification(c.getUserId(), "Document " + document.getTitle() + " has been deleted by owner.", "DOC_DELETED", id);
            }
            documentMapper.deleteById(id);
        } else {
            // Collaborator: Check permission to leave (anyone can leave)
            // But if user meant "delete" as "remove from my list", then it is leaving.
            // If they meant "delete the document globally", only Creator can do that per requirements.
            // Requirement: "User A deletes... removes User A... document still exists"
            DocumentCollaborator dc = collaboratorMapper.findByDocAndUser(id, userId);
            if (dc != null) {
                 collaboratorService.removeCollaborator(id, userId, userId);
            } else {
                 throw new RuntimeException("No permission to delete");
            }
        }
    }

    @Override
    public List<DocumentDto> getUserDocuments(Long userId) {
        return documentMapper.findUserDocuments(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDto> searchDocuments(String keyword) {
        return documentMapper.searchDocuments(keyword).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void moveDocumentToFolder(Long docId, Long folderId, Long userId) {
        Document document = documentMapper.findById(docId);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        
        boolean hasAccess = false;
        if (document.getOwnerId().equals(userId)) {
            hasAccess = true;
        } else {
             DocumentCollaborator dc = collaboratorMapper.findByDocAndUser(docId, userId);
             if (dc != null && "ACCEPTED".equals(dc.getStatus())) {
                 hasAccess = true;
             }
        }
        
        if (!hasAccess) {
             throw new RuntimeException("No permission to organize this document");
        }
        
        documentMapper.insertOrUpdateDocumentFolder(userId, docId, folderId);
    }

    @Override
    public List<DocumentDto> searchDocumentsAdvanced(Map<String, Object> params) {
        return documentMapper.searchDocumentsAdvanced(params).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DocumentDto convertToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        BeanUtils.copyProperties(document, dto);
        User user = userMapper.findById(document.getOwnerId());
        if (user != null) {
            dto.setOwnerName(user.getName() != null ? user.getName() : user.getAccount());
            dto.setOwnerAccount(user.getAccount());
            dto.setOwnerEmail(user.getEmail());
            dto.setOwnerPhone(user.getPhone());
            dto.setOwnerIntro(user.getIntro());
            dto.setOwnerAvatar(user.getAvatar());
        }
        return dto;
    }
}
