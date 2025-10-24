package com.example.frontend.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class ProductOption {
    @SerializedName("optionId")
    private Long optionId;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("optionName")
    private String optionName;

    @SerializedName("optionType")
    private String optionType;

    @SerializedName("price")
    private BigDecimal extraPrice;

    @SerializedName("isRequired")
    private Boolean isRequired;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("maxSelections")
    private Integer maxSelections;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public ProductOption() {}

    public ProductOption(Long optionId, Long productId, String optionName, String optionType,
                         BigDecimal extraPrice, Boolean isRequired, Boolean isActive, Integer maxSelections) {
        this.optionId = optionId;
        this.productId = productId;
        this.optionName = optionName;
        this.optionType = optionType;
        this.extraPrice = extraPrice;
        this.isRequired = isRequired;
        this.isActive = isActive;
        this.maxSelections = maxSelections;
    }

    // Getters and Setters
    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public BigDecimal getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(BigDecimal extraPrice) {
        this.extraPrice = extraPrice;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getMaxSelections() {
        return maxSelections;
    }

    public void setMaxSelections(Integer maxSelections) {
        this.maxSelections = maxSelections;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isRequired() {
        return isRequired != null && isRequired;
    }

    public boolean isPaid() {
        return extraPrice != null && extraPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getFormattedPrice() {
        android.util.Log.d("ProductOption", "getFormattedPrice: extraPrice = " + extraPrice);

        if (extraPrice == null) {
            android.util.Log.d("ProductOption", "getFormattedPrice: extraPrice is null, returning 'Miễn phí'");
            return "Miễn phí";
        }

        if (extraPrice.compareTo(BigDecimal.ZERO) == 0) {
            android.util.Log.d("ProductOption", "getFormattedPrice: extraPrice is 0, returning 'Miễn phí'");
            return "Miễn phí";
        }

        String formatted = "+" + extraPrice + "đ";
        android.util.Log.d("ProductOption", "getFormattedPrice: returning '" + formatted + "'");
        return formatted;
    }

    @Override
    public String toString() {
        return "ProductOption{" +
                "optionId=" + optionId +
                ", productId=" + productId +
                ", optionName='" + optionName + '\'' +
                ", optionType='" + optionType + '\'' +
                ", extraPrice=" + extraPrice +
                ", isRequired=" + isRequired +
                ", isActive=" + isActive +
                ", maxSelections=" + maxSelections +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductOption that = (ProductOption) obj;
        return optionId != null ? optionId.equals(that.optionId) : that.optionId == null;
    }

    @Override
    public int hashCode() {
        return optionId != null ? optionId.hashCode() : 0;
    }
}
