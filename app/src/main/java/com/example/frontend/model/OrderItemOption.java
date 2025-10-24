package com.example.frontend.model;

import java.math.BigDecimal;

public class OrderItemOption {
    
    private Long optionId;
    private Long orderItemId;
    private String optionName;
    private BigDecimal additionalPrice;
    private String optionType; // SIZE, TOPPING, etc.

    // Constructors
    public OrderItemOption() {}

    public OrderItemOption(Long optionId, String optionName, BigDecimal additionalPrice, String optionType) {
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.optionType = optionType;
    }

    // Getters and Setters
    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }

    public BigDecimal getAdditionalPrice() { return additionalPrice; }
    public void setAdditionalPrice(BigDecimal additionalPrice) { this.additionalPrice = additionalPrice; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    // Helper methods
    public String getFormattedAdditionalPrice() {
        if (additionalPrice != null && additionalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("+%,.0f VNĐ", additionalPrice);
        }
        return "";
    }

    public String getDisplayText() {
        if (additionalPrice != null && additionalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return optionName + " " + getFormattedAdditionalPrice();
        }
        return optionName;
    }
}
