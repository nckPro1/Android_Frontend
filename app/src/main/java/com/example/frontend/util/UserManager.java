package com.example.frontend.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.frontend.model.User;
import com.example.frontend.model.UserDto;
import com.google.gson.Gson;

public class UserManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static UserManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    private User currentUser;

    private UserManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCurrentUser();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    // User management
    public void setCurrentUser(User user) {
        this.currentUser = user;
        saveCurrentUser(user);
        setLoggedIn(true);
    }

    public void setCurrentUser(UserDto userDto) {
        if (userDto != null) {
            this.currentUser = convertUserDtoToUser(userDto);
            saveCurrentUser(currentUser);
            setLoggedIn(true);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clearCurrentUser() {
        this.currentUser = null;
        prefs.edit().remove(KEY_CURRENT_USER).apply();
        setLoggedIn(false);
        clearTokens();
    }

    // Login status
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && currentUser != null;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    // Token management
    public void setAccessToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void setRefreshToken(String token) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clearTokens() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }

    // User profile updates
    public void updateUserProfile(String fullName, String phoneNumber) {
        if (currentUser != null) {
            currentUser.setFullName(fullName);
            currentUser.setPhoneNumber(phoneNumber);
            saveCurrentUser(currentUser);
        }
    }

    public void updateDeliveryAddress(String address, String city, String district, String ward, String street) {
        if (currentUser != null) {
            currentUser.setAddress(address);
            currentUser.setUserCity(city);
            currentUser.setUserDistrict(district);
            currentUser.setUserWard(ward);
            currentUser.setUserStreet(street);
            saveCurrentUser(currentUser);
        }
    }

    /**
     * Update user address components
     */
    public void updateUserAddressComponents(String city, String district, String ward, String street) {
        if (currentUser != null) {
            currentUser.setUserCity(city);
            currentUser.setUserDistrict(district);
            currentUser.setUserWard(ward);
            currentUser.setUserStreet(street);
            
            // Update full address
            String fullAddress = street + ", " + ward + ", " + district + ", " + city;
            currentUser.setAddress(fullAddress);
            
            saveCurrentUser(currentUser);
        }
    }

    /**
     * Refresh user data from server (call this after profile updates)
     */
    public void refreshUserData(UserDto updatedUserDto) {
        if (updatedUserDto != null) {
            // Convert UserDto to User
            User updatedUser = convertUserDtoToUser(updatedUserDto);
            this.currentUser = updatedUser;
            saveCurrentUser(currentUser);
        }
    }

    /**
     * Convert UserDto to User object
     */
    private User convertUserDtoToUser(UserDto userDto) {
        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setAvatarUrl(userDto.getAvatarUrl());
        user.setRoleId(userDto.getRoleId());
        user.setAuthProvider(userDto.getAuthProvider());
        user.setUserCity(userDto.getUserCity());
        user.setUserDistrict(userDto.getUserDistrict());
        user.setUserWard(userDto.getUserWard());
        user.setUserStreet(userDto.getUserStreet());
        return user;
    }

    public void updateUserAvatar(String avatarUrl) {
        if (currentUser != null) {
            currentUser.setAvatarUrl(avatarUrl);
            saveCurrentUser(currentUser);
        }
    }

    // Helper methods
    public boolean hasValidAddress() {
        return currentUser != null && currentUser.hasAddress();
    }

    public boolean hasValidAddressComponents() {
        return currentUser != null && currentUser.hasAddressComponents();
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public String getUserDisplayName() {
        if (currentUser != null && currentUser.getFullName() != null) {
            return currentUser.getFullName();
        }
        return "Khách";
    }

    public String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public String getUserPhone() {
        return currentUser != null ? currentUser.getPhoneNumber() : null;
    }

    public String getUserAddress() {
        return currentUser != null ? currentUser.getAddress() : null;
    }

    public String getUserCity() {
        return currentUser != null ? currentUser.getUserCity() : null;
    }

    public String getUserDistrict() {
        return currentUser != null ? currentUser.getUserDistrict() : null;
    }

    public String getUserWard() {
        return currentUser != null ? currentUser.getUserWard() : null;
    }

    public String getUserStreet() {
        return currentUser != null ? currentUser.getUserStreet() : null;
    }

    public String getUserFullAddress() {
        return currentUser != null ? currentUser.getFullAddress() : null;
    }

    public String getUserAvatarUrl() {
        return currentUser != null ? currentUser.getAvatarUrl() : null;
    }

    // Private methods
    private void saveCurrentUser(User user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            prefs.edit().putString(KEY_CURRENT_USER, userJson).apply();
        }
    }

    private void loadCurrentUser() {
        String userJson = prefs.getString(KEY_CURRENT_USER, null);
        if (userJson != null) {
            try {
                currentUser = gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                // If parsing fails, clear the stored data
                prefs.edit().remove(KEY_CURRENT_USER).apply();
                currentUser = null;
            }
        }
    }

    // Logout
    public void logout() {
        clearCurrentUser();
        clearTokens();
        setLoggedIn(false);
    }
}
