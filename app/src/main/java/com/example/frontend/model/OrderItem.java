package com.example.frontend.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class OrderItem {

    @SerializedName("orderItemId")
    private Long orderItemId;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("productImageUrl")
    private String productImage;

    @SerializedName("unitPrice")
    private BigDecimal unitPrice;

    @SerializedName("salePrice")
    private BigDecimal salePrice;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("totalPrice")
    private BigDecimal subtotal;

    @SerializedName("specialInstructions")
    private String specialInstructions;

    @SerializedName("orderItemOptions")
    private List<OrderItemOption> selectedOptions;

    // Constructors
    public OrderItem() {}

    public OrderItem(Long productId, String productName, String productImage,
                     BigDecimal productPrice, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.unitPrice = productPrice;
        this.quantity = quantity;
        if (productPrice != null && quantity != null) {
            this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
        }
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

    /**
     * Lấy giá sản phẩm (ưu tiên salePrice, fallback unitPrice)
     */
    public BigDecimal getProductPrice() {
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0) {
            return salePrice;
        }
        return unitPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.unitPrice = productPrice;
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        BigDecimal price = getProductPrice();
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
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
        BigDecimal price = getProductPrice();
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public String getFormattedSubtotal() {
        if (subtotal != null) {
            return String.format("%,.0f VNĐ", subtotal);
        }
        return "0 VNĐ";
    }

    public String getFormattedProductPrice() {
        BigDecimal price = getProductPrice();
        if (price != null) {
            return String.format("%,.0f VNĐ", price);
        }
        return "0 VNĐ";
    }
}
