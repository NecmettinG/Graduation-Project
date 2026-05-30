package com.graduation.smarty_commerce.io.Repository;

import com.graduation.smarty_commerce.io.Entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    CommentEntity findByCommentId(String commentId);
    List<CommentEntity> findByProductProductId(String productId);
    List<CommentEntity> findByUserUserId(String userId);
}

