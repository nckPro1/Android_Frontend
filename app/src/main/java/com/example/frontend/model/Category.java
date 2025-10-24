package com.example.frontend.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("categoryId")
    private Long categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("categoryImageUrl")
    private String categoryImageUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("sortOrder")
    private Integer sortOrder;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public Category() {}

    public Category(Long categoryId, String categoryName, String categoryImageUrl, String description, Boolean isActive, Integer sortOrder) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryImageUrl = categoryImageUrl;
        this.description = description;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
    }

    // Getters
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryImageUrl() { return categoryImageUrl; }
    public String getDescription() { return description; }
    public Boolean getIsActive() { return isActive; }
    public Integer getSortOrder() { return sortOrder; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setCategoryImageUrl(String categoryImageUrl) { this.categoryImageUrl = categoryImageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", categoryImageUrl='" + categoryImageUrl + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
