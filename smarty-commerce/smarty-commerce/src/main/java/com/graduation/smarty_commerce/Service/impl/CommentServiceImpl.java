package com.graduation.smarty_commerce.Service.impl;

import com.graduation.smarty_commerce.Service.CommentService;
import com.graduation.smarty_commerce.io.Entity.CommentEntity;
import com.graduation.smarty_commerce.io.Entity.ProductEntity;
import com.graduation.smarty_commerce.io.Entity.UserEntity;
import com.graduation.smarty_commerce.io.Entity.OrderEntity;
import com.graduation.smarty_commerce.io.Entity.OrderItemEntity;
import com.graduation.smarty_commerce.io.Repository.CommentRepository;
import com.graduation.smarty_commerce.io.Repository.OrderRepository;
import com.graduation.smarty_commerce.io.Repository.ProductRepository;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import com.graduation.smarty_commerce.shared.OrderStatus;
import com.graduation.smarty_commerce.shared.Utils;
import com.graduation.smarty_commerce.shared.dto.CommentDto;
import com.graduation.smarty_commerce.shared.dto.UserDto;
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
    private OrderRepository orderRepository;

    @Autowired
    private Utils utils;

    /*
    COMMENT FEATURE SPEED PROBLEM SOLUTION 1:
     ModelMapper Lazy-Loading Chain-Reaction:
     Whenever ModelMapper mapped a list of CommentEntity back to DTOs in getUserComments and getProductComments, it inherently noticed-
     that your UserDto was packed with multiple collections (Orders, Addresses, Cart, Roles). By default, it fired up-
     deep nested instantiation to build those massive structural trees, producing literal hundreds of implicit SQL LAZY load queries -
     (N+1 scenario).
     Fix: Bypassed ModelMapper for this edge-case mapping completely and generated a dedicated fast mapToDto wrapper that only -
     safely extracts what the CommentRest controller actually requested-
     (commentId, content, createdAt and the basic user firstName, lastName). All secondary recursive cartesian DB relationships are -
     permanently skipped!
     */
    private CommentDto mapToDto(CommentEntity entity) {
        CommentDto dto = new CommentDto();
        dto.setId(entity.getId());
        dto.setCommentId(entity.getCommentId());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            UserDto userDto = new UserDto();
            userDto.setUserId(entity.getUser().getUserId());
            userDto.setFirstName(entity.getUser().getFirstName());
            userDto.setLastName(entity.getUser().getLastName());
            userDto.setEmail(entity.getUser().getEmail());
            dto.setUser(userDto);
        }

        // We skip mapping the entire product to avoid thousands of recursive DB queries, 
        // as Product info is not needed directly inside CommentRest endpoint payloads.
        return dto;
    }

    @Override
    public CommentDto createComment(String productId, String userId, CommentDto commentDetails) {
        ProductEntity productEntity = productRepository.findByProductId(productId);
        if (productEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Product");

        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": User");

        long purchaseCount = orderRepository.countUserPurchasesOfProduct(userId, productId, OrderStatus.DELIVERED);

        if (purchaseCount == 0) {
            throw new RuntimeException("You can only comment on products you have purchased and received.");
        }

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentId(utils.generateId(30));
        commentEntity.setContent(commentDetails.getContent());
        commentEntity.setCreatedAt(new Date());
        commentEntity.setProduct(productEntity);
        commentEntity.setUser(userEntity);

        CommentEntity storedComment = commentRepository.save(commentEntity);
        return mapToDto(storedComment);
    }

    @Override
    public List<CommentDto> getProductComments(String productId) {
        List<CommentEntity> comments = commentRepository.findByProductProductId(productId);
        return comments.stream().map(this::mapToDto).toList();
    }

    @Override
    public List<CommentDto> getUserComments(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": User");
        
        List<CommentEntity> comments = commentRepository.findByUserUserId(userId);
        return comments.stream().map(this::mapToDto).toList();
    }

    @Override
    public CommentDto updateComment(String commentId, String userId, CommentDto commentDetails, String productId) {
        CommentEntity commentEntity = commentRepository.findByCommentId(commentId);
        if (commentEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Comment");

        if (productId != null && !commentEntity.getProduct().getProductId().equals(productId)) {
            throw new RuntimeException("Comment does not belong to the specific product.");
        }

        if (!commentEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this comment!");
        }

        commentEntity.setContent(commentDetails.getContent());
        CommentEntity updatedComment = commentRepository.save(commentEntity);

        return mapToDto(updatedComment);
    }

    @Override
    public void deleteComment(String commentId, String userId, String productId) {
        CommentEntity commentEntity = commentRepository.findByCommentId(commentId);
        if (commentEntity == null) throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + ": Comment");

        if (productId != null && !commentEntity.getProduct().getProductId().equals(productId)) {
            throw new RuntimeException("Comment does not belong to the specific product.");
        }

        // Assuming checking happens in Controller via PreAuthorize or here
        if (!commentEntity.getUser().getUserId().equals(userId)) {
            // Optional: you can check if user is ADMIN using SecurityContext, but normally `@PreAuthorize` handles this.
            throw new RuntimeException("You are not authorized to delete this comment!");
        }

        commentRepository.delete(commentEntity);
    }
}
