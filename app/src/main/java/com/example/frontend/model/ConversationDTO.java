package com.example.frontend.model;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDTO {
    private Long conversationId;
    private String subject;
    private Long createdByUserId;
    private String createdByName;
    private String createdByAvatarUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long unreadCount;
    private MessageDTO lastMessage;
    private List<ParticipantDTO> participants;

    // Getters and Setters
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getCreatedByAvatarUrl() { return createdByAvatarUrl; }
    public void setCreatedByAvatarUrl(String createdByAvatarUrl) { this.createdByAvatarUrl = createdByAvatarUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

    public MessageDTO getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageDTO lastMessage) { this.lastMessage = lastMessage; }

    public List<ParticipantDTO> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantDTO> participants) { this.participants = participants; }
}

