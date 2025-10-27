package com.example.frontend.model;

import java.math.BigDecimal;

public class StoreInfo {
    private String storeName;
    private String storePhone;
    private String storeEmail;
    private String storeCity;
    private String storeDistrict;
    private String storeWard;
    private String storeStreet;
    private String storeDescription;

    public StoreInfo() {}

    public StoreInfo(String storeName, String storePhone, String storeEmail,
                     String storeCity, String storeDistrict, String storeWard, 
                     String storeStreet, String storeDescription) {
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.storeEmail = storeEmail;
        this.storeCity = storeCity;
        this.storeDistrict = storeDistrict;
        this.storeWard = storeWard;
        this.storeStreet = storeStreet;
        this.storeDescription = storeDescription;
    }

    // Getters and Setters
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }

    public String getStoreEmail() { return storeEmail; }
    public void setStoreEmail(String storeEmail) { this.storeEmail = storeEmail; }

    public String getStoreCity() { return storeCity; }
    public void setStoreCity(String storeCity) { this.storeCity = storeCity; }

    public String getStoreDistrict() { return storeDistrict; }
    public void setStoreDistrict(String storeDistrict) { this.storeDistrict = storeDistrict; }

    public String getStoreWard() { return storeWard; }
    public void setStoreWard(String storeWard) { this.storeWard = storeWard; }

    public String getStoreStreet() { return storeStreet; }
    public void setStoreStreet(String storeStreet) { this.storeStreet = storeStreet; }

    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

    public String getFullAddress() {
        return storeStreet + ", " + storeWard + ", " + storeDistrict + ", " + storeCity;
    }
}






