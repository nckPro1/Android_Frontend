package com.example.frontend.model;

import java.math.BigDecimal;

public class ShippingCalculationRequest {
    private BigDecimal orderAmount;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryWard;
    private String deliveryStreet;

    public ShippingCalculationRequest() {}

    public ShippingCalculationRequest(BigDecimal orderAmount, String deliveryCity, 
                                      String deliveryDistrict, String deliveryWard, String deliveryStreet) {
        this.orderAmount = orderAmount;
        this.deliveryCity = deliveryCity;
        this.deliveryDistrict = deliveryDistrict;
        this.deliveryWard = deliveryWard;
        this.deliveryStreet = deliveryStreet;
    }

    // Getters and Setters
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }

    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }

    public String getDeliveryDistrict() { return deliveryDistrict; }
    public void setDeliveryDistrict(String deliveryDistrict) { this.deliveryDistrict = deliveryDistrict; }

    public String getDeliveryWard() { return deliveryWard; }
    public void setDeliveryWard(String deliveryWard) { this.deliveryWard = deliveryWard; }

    public String getDeliveryStreet() { return deliveryStreet; }
    public void setDeliveryStreet(String deliveryStreet) { this.deliveryStreet = deliveryStreet; }
}
