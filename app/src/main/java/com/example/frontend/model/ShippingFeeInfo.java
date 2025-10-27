package com.example.frontend.model;

import java.math.BigDecimal;

public class ShippingFeeInfo {
    private BigDecimal defaultShippingFee;
    private BigDecimal freeShippingThreshold;
    private BigDecimal minOrderAmount;

    public ShippingFeeInfo() {}

    public ShippingFeeInfo(BigDecimal defaultShippingFee, BigDecimal freeShippingThreshold, BigDecimal minOrderAmount) {
        this.defaultShippingFee = defaultShippingFee;
        this.freeShippingThreshold = freeShippingThreshold;
        this.minOrderAmount = minOrderAmount;
    }

    // Getters and Setters
    public BigDecimal getDefaultShippingFee() {
        return defaultShippingFee;
    }

    public void setDefaultShippingFee(BigDecimal defaultShippingFee) {
        this.defaultShippingFee = defaultShippingFee;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    @Override
    public String toString() {
        return "ShippingFeeInfo{" +
                "defaultShippingFee=" + defaultShippingFee +
                ", freeShippingThreshold=" + freeShippingThreshold +
                ", minOrderAmount=" + minOrderAmount +
                '}';
    }
}



