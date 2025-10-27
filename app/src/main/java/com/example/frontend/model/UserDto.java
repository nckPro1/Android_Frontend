package com.example.frontend.model;

import java.math.BigDecimal;

public class UserDto {
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
    public UserDto() {}

    public UserDto(Long userId, String email, String fullName, String phoneNumber, 
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

    // Getters
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getAvatarUrl() { return avatarUrl; }
    public Integer getRoleId() { return roleId; }
    public String getAuthProvider() { return authProvider; }
    public String getUserCity() { return userCity; }
    public String getUserDistrict() { return userDistrict; }
    public String getUserWard() { return userWard; }
    public String getUserStreet() { return userStreet; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public void setUserCity(String userCity) { this.userCity = userCity; }
    public void setUserDistrict(String userDistrict) { this.userDistrict = userDistrict; }
    public void setUserWard(String userWard) { this.userWard = userWard; }
    public void setUserStreet(String userStreet) { this.userStreet = userStreet; }

    // Helper methods
    public boolean hasAddressComponents() {
        return userCity != null && !userCity.trim().isEmpty() &&
               userDistrict != null && !userDistrict.trim().isEmpty() &&
               userWard != null && !userWard.trim().isEmpty() &&
               userStreet != null && !userStreet.trim().isEmpty();
    }

    public String getFullAddress() {
        if (hasAddressComponents()) {
            return userStreet + ", " + userWard + ", " + userDistrict + ", " + userCity;
        }
        return address != null ? address : "";
    }

    public boolean isAdmin() {
        return roleId != null && roleId == 2; // Assuming roleId 2 is admin
    }
}