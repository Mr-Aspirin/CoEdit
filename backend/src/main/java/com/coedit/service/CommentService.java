package com.coedit.service;

import com.coedit.dto.CommentDto;
import com.coedit.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment addComment(Comment comment);
    List<CommentDto> getComments(Long documentId);
    Long deleteComment(Long commentId, Long userId);
}
