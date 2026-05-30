package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Service.CommentService;
import com.graduation.smarty_commerce.io.Entity.CommentEntity;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Repository.CommentRepository;
import com.graduation.smarty_commerce.io.Repository.ProductRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.CommentDto;
import com.graduation.smarty_commerce.ui.Model.Request.CommentRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;

    @Override
    public CommentDto createComment(String productId, String userId, CommentRequestModel commentDetails) {
        ProductEntity productEntity = productRepository.findByProductId(productId);
        if (productEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Product");

        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": User");

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentId(utils.generateId(30));
        commentEntity.setContent(commentDetails.getContent());
        commentEntity.setCreatedAt(new Date());
        commentEntity.setProduct(productEntity);
        commentEntity.setUser(userEntity);

        CommentEntity storedComment = commentRepository.save(commentEntity);
        return new ModelMapper().map(storedComment, CommentDto.class);
    }

    @Override
    public List<CommentDto> getProductComments(String productId) {
        List<CommentEntity> comments = commentRepository.findByProductProductId(productId);

        Type listType = new TypeToken<List<CommentDto>>() {}.getType();
        return new ModelMapper().map(comments, listType);
    }

    @Override
    public List<CommentDto> getUserComments(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": User");
        
        List<CommentEntity> comments = commentRepository.findByUserUserId(userId);

        Type listType = new TypeToken<List<CommentDto>>() {}.getType();
        return new ModelMapper().map(comments, listType);
    }

    @Override
    public CommentDto updateComment(String commentId, String userId, CommentRequestModel commentDetails) {
        CommentEntity commentEntity = commentRepository.findByCommentId(commentId);
        if (commentEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Comment");

        if (!commentEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this comment!");
        }

        commentEntity.setContent(commentDetails.getContent());
        CommentEntity updatedComment = commentRepository.save(commentEntity);

        return new ModelMapper().map(updatedComment, CommentDto.class);
    }

    @Override
    public void deleteComment(String commentId, String userId) {
        CommentEntity commentEntity = commentRepository.findByCommentId(commentId);
        if (commentEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Comment");

        // Assuming checking happens in Controller via PreAuthorize or here
        if (!commentEntity.getUser().getUserId().equals(userId)) {
            // Optional: you can check if user is ADMIN using SecurityContext, but normally `@PreAuthorize` handles this.
            throw new RuntimeException("You are not authorized to delete this comment!");
        }

        commentRepository.delete(commentEntity);
    }
}
