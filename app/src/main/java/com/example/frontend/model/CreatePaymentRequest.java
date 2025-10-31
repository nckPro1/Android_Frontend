package com.example.frontend.model;

public class CreatePaymentRequest {
    private Long orderId;
    private String paymentMethod;
    private java.math.BigDecimal amount;
    private String bankCode;
    private String language;
    private String ipAddress;
    private String description;

    public CreatePaymentRequest() {}

    public CreatePaymentRequest(Long orderId, String paymentMethod, java.math.BigDecimal amount, 
                               String bankCode, String language, String ipAddress) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.bankCode = bankCode;
        this.language = language;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
