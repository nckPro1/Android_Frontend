package com.example.frontend.model;

public class OTPVerifyRequest {
    private String email;
    private String otp;

    public OTPVerifyRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}