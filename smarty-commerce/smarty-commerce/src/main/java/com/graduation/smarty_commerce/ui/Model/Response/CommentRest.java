package com.graduation.smarty_commerce.ui.Model.Response;

import java.util.Date;

public class CommentRest {
    
    private String commentId;
    private String content;
    private Date createdAt;
    private UserRest user;

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

    public UserRest getUser() {
        return user;
    }

    public void setUser(UserRest user) {
        this.user = user;
    }
}

