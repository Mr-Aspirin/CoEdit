package com.coedit.service;

import com.coedit.dto.CollaboratorDto;
import java.util.List;

public interface CollaboratorService {
    void addCollaborator(Long documentId, Long userId, String permission);
    void inviteCollaborator(Long documentId, String identifier, String permission, Long operatorId);
    void removeCollaborator(Long documentId, Long userId, Long operatorId);
    void updatePermission(Long documentId, Long userId, String permission, Long operatorId);
    void acceptInvitation(Long documentId, Long userId);
    List<CollaboratorDto> getCollaborators(Long documentId);
}
