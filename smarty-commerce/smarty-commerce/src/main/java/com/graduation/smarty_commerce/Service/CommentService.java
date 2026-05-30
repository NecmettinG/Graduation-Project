package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(String productId, String userId, CommentDto commentDetails);
    List<CommentDto> getProductComments(String productId);
    List<CommentDto> getUserComments(String userId);
    CommentDto updateComment(String commentId, String userId, CommentDto commentDetails);
    void deleteComment(String commentId, String userId);
}
