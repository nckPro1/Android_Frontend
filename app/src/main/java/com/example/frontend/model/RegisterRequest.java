package com.example.frontend.model;

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;

    public RegisterRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
}