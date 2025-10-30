package com.example.frontend.model;

public class CreateVnpayPaymentRequest {
    private Long orderId;
    private Long amount;
    private String orderInfo;
    private String ipAddr;

    public CreateVnpayPaymentRequest() {}

    public CreateVnpayPaymentRequest(Long orderId, Long amount, String orderInfo, String ipAddr) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.ipAddr = ipAddr;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }

    public String getIpAddr() { return ipAddr; }
    public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
}


