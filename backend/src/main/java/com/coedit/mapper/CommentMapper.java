package com.coedit.mapper;

import com.coedit.dto.CommentDto;
import com.coedit.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);
    
    List<CommentDto> selectByDocumentId(Long documentId);
    
    int deleteById(Long id);
    
    Comment selectById(Long id);
}
