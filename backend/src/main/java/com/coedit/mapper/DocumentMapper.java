package com.coedit.mapper;

import com.coedit.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface DocumentMapper {
    int insert(Document document);
    int update(Document document);
    int deleteById(Long id);
    Document findById(Long id);
    
    List<Document> findUserDocuments(@Param("userId") Long userId);
    
    List<Document> searchDocuments(@Param("keyword") String keyword);

    List<Document> searchDocumentsAdvanced(Map<String, Object> params);

    int insertOrUpdateDocumentFolder(@Param("userId") Long userId, @Param("documentId") Long documentId, @Param("folderId") Long folderId);
}
