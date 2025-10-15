package com.example.frontend.model;

// Dùng để gửi ID Token lên server
public class GoogleLoginRequest {
    private String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }
}