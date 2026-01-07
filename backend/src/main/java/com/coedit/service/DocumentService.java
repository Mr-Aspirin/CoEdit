package com.coedit.service;

import com.coedit.dto.DocumentDto;
import java.util.List;
import java.util.Map;

public interface DocumentService {
    DocumentDto createDocument(DocumentDto documentDto, Long userId);
    DocumentDto getDocument(Long id, Long userId);
    DocumentDto updateDocument(Long id, DocumentDto documentDto, Long userId);
    void deleteDocument(Long id, Long userId);
    List<DocumentDto> getUserDocuments(Long userId);
    List<DocumentDto> searchDocuments(String keyword);
    
    void moveDocumentToFolder(Long docId, Long folderId, Long userId);
    List<DocumentDto> searchDocumentsAdvanced(Map<String, Object> params);
}
