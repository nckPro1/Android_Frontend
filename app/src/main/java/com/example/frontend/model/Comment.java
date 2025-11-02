package com.example.frontend.model;

import java.time.LocalDateTime;

public class Comment {
    private Long commentId;
    private Long productId;
    private Long userId;
    private Long reviewId;
    private String content;
    private String userName;
    private String userAvatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Comment() {
    }

    public Comment(Long commentId, Long productId, Long userId, Long reviewId, String content,
                   String userName, String userAvatarUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.commentId = commentId;
        this.productId = productId;
        this.userId = userId;
        this.reviewId = reviewId;
        this.content = content;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

