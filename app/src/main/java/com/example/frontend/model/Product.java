package com.example.frontend.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class Product {
    @SerializedName("productId")
    private Long productId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("galleryUrls")
    private List<String> galleryUrls;

    @SerializedName("hasOptions")
    private Boolean hasOptions;

    @SerializedName("isAvailable")
    private Boolean isAvailable;

    @SerializedName("isFeatured")
    private Boolean isFeatured;

    @SerializedName("preparationTime")
    private Integer preparationTime;

    // Sale fields
    @SerializedName("salePrice")
    private BigDecimal salePrice;

    @SerializedName("salePercentage")
    private Integer salePercentage;

    @SerializedName("isOnSale")
    private Boolean isOnSale;

    @SerializedName("saleStartDate")
    private String saleStartDate;

    @SerializedName("saleEndDate")
    private String saleEndDate;

    @SerializedName("category")
    private Category category;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("options")
    private List<ProductOption> options;

    // Constructors
    public Product() {}

    public Product(Long productId, String name, String description, BigDecimal price, String imageUrl, Boolean isAvailable, Boolean isFeatured, Category category) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.isFeatured = isFeatured;
        this.category = category;
    }

    // Getters
    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getGalleryUrls() { return galleryUrls; }
    public Boolean getHasOptions() { return hasOptions; }
    public Boolean getIsAvailable() { return isAvailable; }
    public Boolean getIsFeatured() { return isFeatured; }
    public Integer getPreparationTime() { return preparationTime; }
    public Category getCategory() { return category; }
    public String getCreatedAt() { return createdAt; }
    public List<ProductOption> getOptions() { return options; }

    // Sale getters
    public BigDecimal getSalePrice() { return salePrice; }
    public Integer getSalePercentage() { return salePercentage; }
    public Boolean getIsOnSale() { return isOnSale; }
    public String getSaleStartDate() { return saleStartDate; }
    public String getSaleEndDate() { return saleEndDate; }

    // Setters
    public void setProductId(Long productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setGalleryUrls(List<String> galleryUrls) { this.galleryUrls = galleryUrls; }
    public void setHasOptions(Boolean hasOptions) { this.hasOptions = hasOptions; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public void setSalePercentage(Integer salePercentage) { this.salePercentage = salePercentage; }
    public void setIsOnSale(Boolean isOnSale) { this.isOnSale = isOnSale; }
    public void setSaleStartDate(String saleStartDate) { this.saleStartDate = saleStartDate; }
    public void setSaleEndDate(String saleEndDate) { this.saleEndDate = saleEndDate; }
    public void setCategory(Category category) { this.category = category; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setOptions(List<ProductOption> options) { this.options = options; }

    // Helper methods
    public String getFormattedPrice() {
        if (price != null) {
            return String.format("%,.0f VNĐ", price.doubleValue());
        }
        return "0 VNĐ";
    }

    public String getMainImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        if (galleryUrls != null && !galleryUrls.isEmpty()) {
            return galleryUrls.get(0);
        }
        return null;
    }

    // Sale helper methods
    public BigDecimal getCurrentPrice() {
        if (isOnSale() && isSaleActive()) {
            if (salePrice != null) {
                return salePrice;
            } else if (salePercentage != null) {
                // FIX lỗi chia số không kết thúc:
                return price.multiply(BigDecimal.valueOf(100 - salePercentage))
                        .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.DOWN);
            }
        }
        return price;
    }

    public String getFormattedCurrentPrice() {
        BigDecimal currentPrice = getCurrentPrice();
        if (currentPrice != null) {
            return String.format("%,.0f VNĐ", currentPrice.doubleValue());
        }
        return "0 VNĐ";
    }

    public boolean isOnSale() {
        return isOnSale != null && isOnSale;
    }

    public boolean isSaleActive() {
        if (!isOnSale()) return false;

        // Check if sale dates are valid
        if (saleStartDate == null || saleEndDate == null) {
            return true; // If no dates specified, assume active
        }

        try {
            // Parse date strings (assuming format: "yyyy-MM-dd'T'HH:mm:ss" or "yyyy-MM-dd HH:mm:ss")
            java.time.LocalDateTime startDate = java.time.LocalDateTime.parse(saleStartDate.replace(" ", "T"));
            java.time.LocalDateTime endDate = java.time.LocalDateTime.parse(saleEndDate.replace(" ", "T"));
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            return now.isAfter(startDate) && now.isBefore(endDate);
        } catch (Exception e) {
            // If parsing fails, assume sale is active
            return true;
        }
    }

    public BigDecimal getDiscountAmount() {
        if (isOnSale() && isSaleActive()) {
            return price.subtract(getCurrentPrice());
        }
        return BigDecimal.ZERO;
    }

    public Integer getDiscountPercentage() {
        if (isOnSale() && isSaleActive()) {
            if (salePercentage != null) {
                return salePercentage;
            } else if (salePrice != null) {
                BigDecimal discount = getDiscountAmount();
                // FIX luôn truyền scale & rounding:
                return discount.multiply(BigDecimal.valueOf(100)).divide(price, 0, java.math.RoundingMode.DOWN).intValue();
            }
        }
        return 0;
    }

    public String getFormattedOriginalPrice() {
        if (price != null) {
            return String.format("%,.0f VNĐ", price.doubleValue());
        }
        return "0 VNĐ";
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", isAvailable=" + isAvailable +
                ", isFeatured=" + isFeatured +
                ", category=" + category +
                '}';
    }
}
