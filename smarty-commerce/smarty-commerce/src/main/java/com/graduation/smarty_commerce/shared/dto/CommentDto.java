package com.graduation.smarty_commerce.shared.dto;

import java.io.Serializable;
import java.util.Date;

public class CommentDto implements Serializable {

    private static final long serialVersionUID = 5312393413548721151L;

    private long id;
    private String commentId;
    private String content;
    private Date createdAt;
    private UserDto user;
    private ProductDto product;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public ProductDto getProduct() {
        return product;
    }

    public void setProduct(ProductDto product) {
        this.product = product;
    }
}

