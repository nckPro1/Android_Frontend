package com.example.frontend.model;

public class CreateConversationRequest {
    private String subject;
    private String firstMessage;

    public CreateConversationRequest() {}

    public CreateConversationRequest(String subject, String firstMessage) {
        this.subject = subject;
        this.firstMessage = firstMessage;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getFirstMessage() { return firstMessage; }
    public void setFirstMessage(String firstMessage) { this.firstMessage = firstMessage; }
}

