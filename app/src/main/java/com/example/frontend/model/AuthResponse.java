package com.example.frontend.model;

// Dùng để nhận kết quả trả về từ server
public class AuthResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private UserDto user;

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UserDto getUser() { return user; }
}