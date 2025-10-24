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

    @SerializedName("category")
    private Category category;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

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
    public String getUpdatedAt() { return updatedAt; }

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
    public void setCategory(Category category) { this.category = category; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

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
