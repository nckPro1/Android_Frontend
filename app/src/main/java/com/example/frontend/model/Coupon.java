package com.example.frontend.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Coupon {
    @SerializedName("couponId")
    private Long couponId;

    @SerializedName("couponCode")
    private String couponCode;

    @SerializedName("couponName")
    private String couponName;

    @SerializedName("description")
    private String description;

    @SerializedName("discountType")
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @SerializedName("discountValue")
    private BigDecimal discountValue;

    @SerializedName("minOrderAmount")
    private BigDecimal minOrderAmount;

    @SerializedName("maxDiscountAmount")
    private BigDecimal maxDiscountAmount;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("usageLimit")
    private Integer usageLimit;

    @SerializedName("usedCount")
    private Integer usedCount;

    // From backend CouponDTO validation response
    @SerializedName("canUse")
    private Boolean canUse;

    @SerializedName("message")
    private String message;

    // Constructors
    public Coupon() {}

    // Getters
    public Long getCouponId() { return couponId; }
    public String getCouponCode() { return couponCode; }
    public String getCouponName() { return couponName; }
    public String getDescription() { return description; }
    public String getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public Boolean getIsActive() { return isActive; }
    public Integer getUsageLimit() { return usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public Boolean getCanUse() { return canUse; }
    public boolean isCanUse() { return canUse != null && canUse; }
    public String getMessage() { return message; }

    // Setters
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public void setCanUse(Boolean canUse) { this.canUse = canUse; }
    public void setMessage(String message) { this.message = message; }

    // Helper methods
    public boolean isActive() {
        return isActive != null && isActive;
    }

    public boolean isPercentageDiscount() {
        return "PERCENTAGE".equals(discountType);
    }

    public String getFormattedDiscountValue() {
        if (discountValue == null) return "0";
        if (isPercentageDiscount()) {
            return discountValue.intValue() + "%";
        } else {
            return String.format("%,.0f VNĐ", discountValue.doubleValue());
        }
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "couponId=" + couponId +
                ", couponCode='" + couponCode + '\'' +
                ", couponName='" + couponName + '\'' +
                ", discountType='" + discountType + '\'' +
                ", discountValue=" + discountValue +
                ", isActive=" + isActive +
                ", canUse=" + canUse +
                ", message='" + message + '\'' +
                '}';
    }
}