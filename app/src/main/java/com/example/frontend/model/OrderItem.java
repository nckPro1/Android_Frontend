package com.example.frontend.model;

import java.math.BigDecimal;
import java.util.List;

public class OrderItem {
    
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String specialInstructions;
    private List<OrderItemOption> selectedOptions;

    // Constructors
    public OrderItem() {}

    public OrderItem(Long productId, String productName, String productImage, 
                    BigDecimal productPrice, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        if (productPrice != null) {
            this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public List<OrderItemOption> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(List<OrderItemOption> selectedOptions) { this.selectedOptions = selectedOptions; }

    // Helper methods
    public void calculateSubtotal() {
        if (productPrice != null && quantity != null) {
            this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public String getFormattedSubtotal() {
        if (subtotal != null) {
            return String.format("%,.0f VNĐ", subtotal);
        }
        return "0 VNĐ";
    }

    public String getFormattedProductPrice() {
        if (productPrice != null) {
            return String.format("%,.0f VNĐ", productPrice);
        }
        return "0 VNĐ";
    }
}
