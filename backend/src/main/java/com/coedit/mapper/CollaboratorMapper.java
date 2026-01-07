package com.coedit.mapper;

import com.coedit.entity.DocumentCollaborator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CollaboratorMapper {
    int insert(DocumentCollaborator collaborator);
    int delete(@Param("documentId") Long documentId, @Param("userId") Long userId);
    int updatePermission(@Param("documentId") Long documentId, @Param("userId") Long userId, @Param("permission") String permission);
    int updateStatus(@Param("documentId") Long documentId, @Param("userId") Long userId, @Param("status") String status);
    List<DocumentCollaborator> findByDocumentId(Long documentId);
    DocumentCollaborator findByDocAndUser(@Param("documentId") Long documentId, @Param("userId") Long userId);
}
