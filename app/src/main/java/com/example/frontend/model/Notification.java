package com.example.frontend.model;

import java.time.LocalDateTime;

public class Notification {
    private Long notificationId;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private Long relatedId; // orderId, conversationId, etc.
    private String relatedType; // "ORDER", "CONVERSATION", etc.
    private LocalDateTime createdAt;

    public enum NotificationType {
        NEW_ORDER,
        ORDER_STATUS_UPDATED,
        USER_MESSAGE,
        ADMIN_MESSAGE,
        PROMOTION,
        SYSTEM_ALERT
    }

    // Constructors
    public Notification() {
    }

    public Notification(Long notificationId, Long userId, NotificationType type, String title, String message, Boolean isRead, Long relatedId, String relatedType, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isRead() {
        return isRead != null && isRead;
    }

    public int getIconResId() {
        switch (type) {
            case ORDER_STATUS_UPDATED:
                return android.R.drawable.ic_menu_view; // hoặc icon tùy chỉnh
            case ADMIN_MESSAGE:
                return android.R.drawable.ic_dialog_email;
            case PROMOTION:
                return android.R.drawable.ic_menu_agenda;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }
}

