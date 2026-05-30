package com.graduation.smarty_commerce.Service;

import com.graduation.smarty_commerce.shared.dto.CommentDto;
import com.graduation.smarty_commerce.ui.Model.Request.CommentRequestModel;

import java.util.List;

public interface CommentService {
    CommentDto createComment(String productId, String userId, CommentRequestModel commentDetails);
    List<CommentDto> getProductComments(String productId);
    CommentDto updateComment(String commentId, String userId, CommentRequestModel commentDetails);
    void deleteComment(String commentId, String userId);
}

