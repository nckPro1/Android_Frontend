package com.example.frontend.remote;

// Import các model cần thiết
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.AuthResponse;
import com.example.frontend.model.GoogleLoginRequest;
import com.example.frontend.model.LoginRequest;
import com.example.frontend.model.OTPVerifyRequest;
import com.example.frontend.model.RegisterRequest; // Thêm import này
import com.example.frontend.model.ResendOTPRequest;
import com.example.frontend.model.UserDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {

    @POST("/api/auth/google")
    Call<AuthResponse> loginWithGoogle(@Body GoogleLoginRequest googleLoginRequest);



    // ✅ THÊM ENDPOINT MỚI CHO ĐĂNG NHẬP THƯỜNG
    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);
    @POST("/api/auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body OTPVerifyRequest otpVerifyRequest);



    @POST("/api/auth/resend-otp")
    Call<ApiResponse> resendOtp(@Body ResendOTPRequest resendOTPRequest);

    // ✅ THÊM ENDPOINT MỚI CHO ĐĂNG KÝ
    @POST("/api/auth/register")
    Call<ApiResponse> register(@Body RegisterRequest registerRequest);

    // ✅ THÊM ENDPOINT CHO PROFILE MANAGEMENT
    @GET("/api/user/profile")
    Call<UserDto> getUserProfile(@Header("Authorization") String token);

    @PUT("/api/user/profile")
    Call<UserDto> updateUserProfile(@Header("Authorization") String token, @Body UserDto userDto);

    // ✅ THÊM ENDPOINT CHO UPLOAD AVATAR
    @Multipart
    @POST("/api/user/upload-avatar")
    Call<ApiResponse> uploadAvatar(@Header("Authorization") String token, @Part MultipartBody.Part file);
}