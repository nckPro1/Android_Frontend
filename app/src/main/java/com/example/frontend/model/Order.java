package com.example.frontend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String deliveryAddress;
    private String deliveryNotes;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryWard;
    private String deliveryStreet;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actualDeliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private List<OrderItem> orderItems;

    // Constructors
    public Order() {}

    public Order(Long orderId, String orderNumber, Long userId, OrderStatus orderStatus,
                 BigDecimal totalAmount, BigDecimal shippingFee, BigDecimal finalAmount,
                 PaymentMethod paymentMethod, String deliveryAddress, String deliveryNotes) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.shippingFee = shippingFee;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.deliveryAddress = deliveryAddress;
        this.deliveryNotes = deliveryNotes;
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

    public String getDeliveryDistrict() { return deliveryDistrict; }
    public void setDeliveryDistrict(String deliveryDistrict) { this.deliveryDistrict = deliveryDistrict; }

    public String getDeliveryWard() { return deliveryWard; }
    public void setDeliveryWard(String deliveryWard) { this.deliveryWard = deliveryWard; }

    public String getDeliveryStreet() { return deliveryStreet; }
    public void setDeliveryStreet(String deliveryStreet) { this.deliveryStreet = deliveryStreet; }

    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    // Helper methods
    public String getOrderStatusDisplay() {
        if (orderStatus == null) return "UNKNOWN";

        switch (orderStatus) {
            case PENDING: return "Chờ xử lý";
            case CONFIRMED: return "Đã nhận";
            case DELIVERING: return "Đang giao";
            case DONE: return "Thanh toán thành công";
            default: return orderStatus.toString();
        }
    }

    public String getPaymentStatusDisplay() {
        if (paymentStatus == null) return "UNKNOWN";

        switch (paymentStatus) {
            case PENDING: return "Chờ thanh toán";
            case COMPLETED: return "Đã thanh toán";
            case FAILED: return "Thanh toán thất bại";
            case REFUNDED: return "Đã hoàn tiền";
            default: return paymentStatus.toString();
        }
    }

    public String getPaymentMethodDisplay() {
        if (paymentMethod == null) return "UNKNOWN";

        switch (paymentMethod) {
            case CASH: return "Tiền mặt";
            case CARD: return "Thẻ";
            case BANK_TRANSFER: return "Chuyển khoản";
            case E_WALLET: return "Ví điện tử";
            default: return paymentMethod.toString();
        }
    }

    public boolean isDelivered() {
        return orderStatus == OrderStatus.DONE;
    }

    public boolean isCancelled() {
        // Since we removed CANCELLED status, check if delivery notes contain cancellation note
        return deliveryNotes != null && deliveryNotes.contains("[ĐÃ HỦY]");
    }

    public boolean canBeCancelled() {
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED;
    }

    // Enums
    public enum OrderStatus {
        PENDING,        // Chờ xử lý
        CONFIRMED,      // Đã nhận
        DELIVERING,     // Đang giao
        DONE            // Hoàn thành (thay vì COMPLETED)
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    public enum PaymentMethod {
        CASH("CASH", "Tiền mặt"),
        CARD("CARD", "Thẻ"),
        BANK_TRANSFER("BANK_TRANSFER", "Chuyển khoản"),
        E_WALLET("E_WALLET", "Ví điện tử");

        private final String value;
        private final String displayName;

        PaymentMethod(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }

        public static PaymentMethod fromValue(String value) {
            for (PaymentMethod method : values()) {
                if (method.value.equals(value)) {
                    return method;
                }
            }
            return CASH; // Default
        }
    }

    // CreateOrderRequest class for API calls
    public static class CreateOrderRequest {
        private String deliveryAddress;
        private String deliveryNotes;
        private String deliveryCity;
        private String deliveryDistrict;
        private String deliveryWard;
        private String deliveryStreet;
        private String paymentMethod;
        private List<OrderItemRequest> orderItems;
        private String couponCode;

        public CreateOrderRequest() {}

        public CreateOrderRequest(String deliveryAddress, String deliveryNotes,
                                  String paymentMethod, List<OrderItemRequest> orderItems) {
            this.deliveryAddress = deliveryAddress;
            this.deliveryNotes = deliveryNotes;
            this.paymentMethod = paymentMethod;
            this.orderItems = orderItems;
        }

        // Getters and Setters
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

        public String getDeliveryNotes() { return deliveryNotes; }
        public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

        public String getDeliveryCity() { return deliveryCity; }
        public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

        public String getDeliveryDistrict() { return deliveryDistrict; }
        public void setDeliveryDistrict(String deliveryDistrict) { this.deliveryDistrict = deliveryDistrict; }

        public String getDeliveryWard() { return deliveryWard; }
        public void setDeliveryWard(String deliveryWard) { this.deliveryWard = deliveryWard; }

        public String getDeliveryStreet() { return deliveryStreet; }
        public void setDeliveryStreet(String deliveryStreet) { this.deliveryStreet = deliveryStreet; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public List<OrderItemRequest> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }

        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    }

    // OrderItemRequest class
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private String specialInstructions;
        private List<Long> selectedOptionIds;

        public OrderItemRequest() {}

        public OrderItemRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

        public List<Long> getSelectedOptionIds() { return selectedOptionIds; }
        public void setSelectedOptionIds(List<Long> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }
    }
}
