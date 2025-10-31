package com.example.frontend.model;

public class PaymentMethod {
    private String methodId;
    private String methodName;
    private String description;
    private String iconUrl;
    private boolean isActive;
    private boolean isOnline;

    // Constructor không tham số
    public PaymentMethod() {
    }

    // Constructor có tham số
    public PaymentMethod(String methodId, String methodName, String description, String iconUrl, boolean isActive, boolean isOnline) {
        this.methodId = methodId;
        this.methodName = methodName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
        this.isOnline = isOnline;
    }

    // Getters và Setters
    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    // toString()
    @Override
    public String toString() {
        return "PaymentMethod{" +
                "methodId='" + methodId + '\'' +
                ", methodName='" + methodName + '\'' +
                ", description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", isActive=" + isActive +
                ", isOnline=" + isOnline +
                '}';
    }
}






















