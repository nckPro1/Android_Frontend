package com.example.frontend.model;

import java.math.BigDecimal;

public class User {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Integer roleId;
    private String authProvider;
    private String userCity;
    private String userDistrict;
    private String userWard;
    private String userStreet;

    // Constructors
    public User() {}

    public User(Long userId, String email, String fullName, String phoneNumber,
                String address, String avatarUrl, Integer roleId, String authProvider,
                String userCity, String userDistrict, String userWard, String userStreet) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.roleId = roleId;
        this.authProvider = authProvider;
        this.userCity = userCity;
        this.userDistrict = userDistrict;
        this.userWard = userWard;
        this.userStreet = userStreet;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserDistrict() {
        return userDistrict;
    }

    public void setUserDistrict(String userDistrict) {
        this.userDistrict = userDistrict;
    }

    public String getUserWard() {
        return userWard;
    }

    public void setUserWard(String userWard) {
        this.userWard = userWard;
    }

    public String getUserStreet() {
        return userStreet;
    }

    public void setUserStreet(String userStreet) {
        this.userStreet = userStreet;
    }

    // Helper methods
    public boolean hasAddress() {
        return address != null && !address.trim().isEmpty();
    }

    public boolean hasAddressComponents() {
        return userCity != null && !userCity.trim().isEmpty() &&
               userDistrict != null && !userDistrict.trim().isEmpty() &&
               userWard != null && !userWard.trim().isEmpty() &&
               userStreet != null && !userStreet.trim().isEmpty();
    }

    public boolean isAdmin() {
        return roleId != null && roleId == 2; // Assuming roleId 2 is admin
    }

    public String getFullAddress() {
        if (hasAddressComponents()) {
            return userStreet + ", " + userWard + ", " + userDistrict + ", " + userCity;
        }
        return address != null ? address : "";
    }
}
