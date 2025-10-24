package com.example.frontend.model;

import java.math.BigDecimal;

public class ShippingCalculationResponse {
    private BigDecimal shippingFee;
    private BigDecimal distanceKm;
    private Integer estimatedDurationMinutes;
    private boolean fromCache;
    private String description;

    public ShippingCalculationResponse() {}

    public ShippingCalculationResponse(BigDecimal shippingFee, BigDecimal distanceKm,
                                       Integer estimatedDurationMinutes, boolean fromCache, String description) {
        this.shippingFee = shippingFee;
        this.distanceKm = distanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.fromCache = fromCache;
        this.description = description;
    }

    // Getters and Setters
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public boolean isFromCache() { return fromCache; }
    public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Helper methods for backward compatibility
    public BigDecimal getDistance() { return distanceKm; }
    public void setDistance(BigDecimal distance) { this.distanceKm = distance; }

    public String getShippingFeeDescription() { return description; }
    public void setShippingFeeDescription(String shippingFeeDescription) { this.description = shippingFeeDescription; }

    public boolean isDeliverable() { 
        return shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0; 
    }
    public void setDeliverable(boolean deliverable) { 
        // This is now calculated based on shippingFee
    }

    public String getMessage() { return description; }
    public void setMessage(String message) { this.description = message; }
}
