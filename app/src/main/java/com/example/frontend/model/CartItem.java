package com.example.frontend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private BigDecimal originalPrice; // Giá gốc
    private BigDecimal salePrice; // Giá sale
    private Boolean isOnSale; // Có đang sale không
    private Integer discountPercentage; // Phần trăm giảm giá
    private int quantity;
    private List<ProductOption> selectedOptions;
    private BigDecimal totalPrice;

    // Constructors
    public CartItem() {}

    public CartItem(Long productId, String productName, String productDescription,
                    String productImageUrl, BigDecimal unitPrice, int quantity,
                    List<ProductOption> selectedOptions) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productImageUrl = productImageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.selectedOptions = selectedOptions;
        calculateTotalPrice();
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Sale getters and setters
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public Boolean getIsOnSale() { return isOnSale; }
    public void setIsOnSale(Boolean isOnSale) { this.isOnSale = isOnSale; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public List<ProductOption> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(List<ProductOption> selectedOptions) {
        this.selectedOptions = selectedOptions;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Sale helper methods
    public boolean isOnSale() {
        return isOnSale != null && isOnSale;
    }

    public BigDecimal getCurrentPrice() {
        if (isOnSale() && salePrice != null) {
            return salePrice;
        }
        return unitPrice;
    }

    public String getFormattedCurrentPrice() {
        BigDecimal currentPrice = getCurrentPrice();
        if (currentPrice != null) {
            return String.format("%,.0f VNĐ", currentPrice.doubleValue());
        }
        return "0 VNĐ";
    }

    public String getFormattedOriginalPrice() {
        if (originalPrice != null) {
            return String.format("%,.0f VNĐ", originalPrice.doubleValue());
        }
        return getFormattedCurrentPrice();
    }

    // Helper methods
    private void calculateTotalPrice() {
        if (unitPrice == null || quantity <= 0) {
            this.totalPrice = BigDecimal.ZERO;
            return;
        }

        BigDecimal currentPrice = getCurrentPrice();
        BigDecimal total = currentPrice.multiply(BigDecimal.valueOf(quantity));

        // Add option prices
        if (selectedOptions != null) {
            for (ProductOption option : selectedOptions) {
                if (option.getPrice() != null) {
                    total = total.add(option.getPrice().multiply(BigDecimal.valueOf(quantity)));
                }
            }
        }

        this.totalPrice = total;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CartItem cartItem = (CartItem) obj;

        // Compare productId
        if (productId == null ? cartItem.productId != null : !productId.equals(cartItem.productId)) {
            return false;
        }

        // Compare selectedOptions
        if (selectedOptions == null) {
            return cartItem.selectedOptions == null;
        }
        if (cartItem.selectedOptions == null) {
            return false;
        }

        // Compare options by optionId and extraPrice
        if (selectedOptions.size() != cartItem.selectedOptions.size()) {
            return false;
        }

        // Create sorted lists for comparison
        List<ProductOption> thisOptions = new ArrayList<>(selectedOptions);
        List<ProductOption> otherOptions = new ArrayList<>(cartItem.selectedOptions);

        // Sort by optionId for consistent comparison
        thisOptions.sort((a, b) -> {
            if (a.getOptionId() == null && b.getOptionId() == null) return 0;
            if (a.getOptionId() == null) return -1;
            if (b.getOptionId() == null) return 1;
            return a.getOptionId().compareTo(b.getOptionId());
        });

        otherOptions.sort((a, b) -> {
            if (a.getOptionId() == null && b.getOptionId() == null) return 0;
            if (a.getOptionId() == null) return -1;
            if (b.getOptionId() == null) return 1;
            return a.getOptionId().compareTo(b.getOptionId());
        });

        for (int i = 0; i < thisOptions.size(); i++) {
            ProductOption thisOption = thisOptions.get(i);
            ProductOption otherOption = otherOptions.get(i);

            if (!thisOption.equals(otherOption)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;

        if (selectedOptions != null) {
            // Sort options by optionId for consistent hash
            List<ProductOption> sortedOptions = new ArrayList<>(selectedOptions);
            sortedOptions.sort((a, b) -> {
                if (a.getOptionId() == null && b.getOptionId() == null) return 0;
                if (a.getOptionId() == null) return -1;
                if (b.getOptionId() == null) return 1;
                return a.getOptionId().compareTo(b.getOptionId());
            });

            for (ProductOption option : sortedOptions) {
                result = 31 * result + option.hashCode();
            }
        }

        return result;
    }
}
