package com.example.frontend.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Sale {
    @SerializedName("saleId")
    private Long saleId;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("saleName")
    private String saleName;

    @SerializedName("saleDescription")
    private String saleDescription;

    @SerializedName("discountType")
    private String discountType;

    @SerializedName("discountValue")
    private BigDecimal discountValue;

    @SerializedName("salePrice")
    private BigDecimal salePrice;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public Sale() {}

    // Getters
    public Long getSaleId() { return saleId; }
    public Long getProductId() { return productId; }
    public String getSaleName() { return saleName; }
    public String getSaleDescription() { return saleDescription; }
    public String getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getSalePrice() { return salePrice; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public Boolean getIsActive() { return isActive; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setSaleName(String saleName) { this.saleName = saleName; }
    public void setSaleDescription(String saleDescription) { this.saleDescription = saleDescription; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isActive() {
        return isActive != null && isActive;
    }

    public boolean isPercentageDiscount() {
        return "PERCENTAGE".equals(discountType);
    }

    public boolean isFixedDiscount() {
        return "FIXED".equals(discountType);
    }

    public String getFormattedDiscountValue() {
        if (discountValue != null) {
            if (isPercentageDiscount()) {
                return discountValue.intValue() + "%";
            } else {
                return String.format("%,.0f VNĐ", discountValue.doubleValue());
            }
        }
        return "0";
    }

    public String getFormattedSalePrice() {
        if (salePrice != null) {
            return String.format("%,.0f VNĐ", salePrice.doubleValue());
        }
        return "0 VNĐ";
    }

    @Override
    public String toString() {
        return "Sale{" +
                "saleId=" + saleId +
                ", productId=" + productId +
                ", saleName='" + saleName + '\'' +
                ", saleDescription='" + saleDescription + '\'' +
                ", discountType='" + discountType + '\'' +
                ", discountValue=" + discountValue +
                ", salePrice=" + salePrice +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
