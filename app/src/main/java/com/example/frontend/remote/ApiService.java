package com.example.frontend.remote;

// Import các model cần thiết
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.AuthResponse;
import com.example.frontend.model.Category;
import com.example.frontend.model.ChangePasswordRequest;
import com.example.frontend.model.ForgotPasswordRequest;
import com.example.frontend.model.GoogleLoginRequest;
import com.example.frontend.model.LoginRequest;
import com.example.frontend.model.OTPVerifyRequest;
import com.example.frontend.model.Product;
import com.example.frontend.model.ProductOption;
import com.example.frontend.model.RegisterRequest; // Thêm import này
import com.example.frontend.model.ResendOTPRequest;
import com.example.frontend.model.ResetPasswordRequest;
import com.example.frontend.model.Order;
import com.example.frontend.model.ShippingCalculationResponse;
import com.example.frontend.model.ShippingCalculationRequest;
import com.example.frontend.model.StoreInfo;
import com.example.frontend.model.UserDto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/auth/google-login")
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

    // ✅ THÊM ENDPOINT CHO CHANGE PASSWORD
    @POST("/api/user/change-password")
    Call<ApiResponse> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest changePasswordRequest);

    // ✅ THÊM ENDPOINT CHO FORGOT PASSWORD
    @POST("/api/auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    // ✅ THÊM ENDPOINT CHO RESET PASSWORD
    @POST("/api/auth/reset-password")
    Call<ApiResponse> resetPassword(@Body ResetPasswordRequest resetPasswordRequest);

    // ===============================
    // PRODUCT & CATEGORY ENDPOINTS
    // ===============================

    // ✅ Lấy tất cả categories
    @GET("/api/products/categories")
    Call<ApiResponse> getAllCategories();

    // ✅ Lấy categories có sản phẩm
    @GET("/api/products/categories/with-products")
    Call<ApiResponse> getCategoriesWithProducts();

    // ✅ Lấy tất cả products với pagination
    @GET("/api/products")
    Call<ApiResponse> getAllProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    // ✅ Lấy product theo ID
    @GET("/api/products/{productId}")
    Call<ApiResponse> getProductById(@Path("productId") Long productId);

    // ✅ Lấy products theo category
    @GET("/api/products/category/{categoryId}")
    Call<ApiResponse> getProductsByCategory(@Path("categoryId") Long categoryId);

    // ✅ Lấy featured products
    @GET("/api/products/featured")
    Call<ApiResponse> getFeaturedProducts();

    // ✅ Tìm kiếm products
    @GET("/api/products/search")
    Call<ApiResponse> searchProducts(@Query("query") String query);

    // ✅ Lấy product options
    @GET("/admin/products/{productId}/options")
    Call<ApiResponse> getProductOptions(@Path("productId") Long productId);

    // ===============================
    // ORDER ENDPOINTS
    // ===============================

    /**
     * Tạo đơn hàng mới
     */
    @POST("api/orders")
    Call<ApiResponse<Order>> createOrder(@Header("Authorization") String token, @Body Order.CreateOrderRequest request);

    /**
     * Tính phí ship
     */
    @GET("api/orders/calculate-shipping")
    Call<ApiResponse<com.example.frontend.model.ShippingCalculationResponse>> calculateShipping(
            @Query("customerLat") double customerLat,
            @Query("customerLng") double customerLng,
            @Query("restaurantLat") double restaurantLat,
            @Query("restaurantLng") double restaurantLng,
            @Query("orderAmount") double orderAmount
    );

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    @GET("api/orders/{orderId}")
    Call<ApiResponse<Order>> getOrderById(@Header("Authorization") String token, @Path("orderId") Long orderId);

    /**
     * Lấy danh sách đơn hàng của user
     */
    @GET("api/orders/user/{userId}")
    Call<ApiResponse<List<Order>>> getOrdersByUserId(@Header("Authorization") String token, @Path("userId") Long userId);

    /**
     * Tính phí ship dựa trên địa chỉ text
     */
    @GET("api/shipping-fee")
    Call<ApiResponse<ShippingCalculationResponse>> calculateShippingFee(@Query("address") String address);

    // ===============================
    // ADDRESS & LOCATION ENDPOINTS
    // ===============================

    /**
     * Lấy danh sách các thành phố có sẵn
     */
    @GET("/api/app/cities")
    Call<ApiResponse<List<String>>> getAvailableCities();

    /**
     * Lấy danh sách các quận/huyện theo thành phố
     */
    @GET("/api/app/districts")
    Call<ApiResponse<List<String>>> getDistrictsByCity(@Query("city") String city);

    /**
     * Lấy danh sách các phường/xã theo quận/huyện
     */
    @GET("/api/app/wards")
    Call<ApiResponse<List<String>>> getWardsByDistrict(@Query("city") String city, @Query("district") String district);

    /**
     * Tính phí ship dựa trên address components
     */
    @POST("/api/app/calculate-shipping")
    Call<ApiResponse<ShippingCalculationResponse>> calculateShippingWithAddress(@Body ShippingCalculationRequest request);

    /**
     * Kiểm tra khả năng giao hàng
     */
    @POST("/api/app/check-delivery-availability")
    Call<ApiResponse<DeliveryAvailabilityResponse>> checkDeliveryAvailability(@Body DeliveryLocationRequest request);

    /**
     * Lấy thông tin cửa hàng
     */
    @GET("/api/app/store-info")
    Call<ApiResponse<StoreInfo>> getStoreInfo();

    // DTOs for API requests
    class DeliveryLocationRequest {
        private String city;
        private String district;
        private String ward;
        private String street;

        public DeliveryLocationRequest() {}

        public DeliveryLocationRequest(String city, String district, String ward, String street) {
            this.city = city;
            this.district = district;
            this.ward = ward;
            this.street = street;
        }

        // Getters and Setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }

    class DeliveryAvailabilityResponse {
        private boolean isDeliverable;
        private java.math.BigDecimal shippingFee;
        private Integer estimatedDurationMinutes;
        private String message;

        public DeliveryAvailabilityResponse() {}

        // Getters and Setters
        public boolean isDeliverable() { return isDeliverable; }
        public void setDeliverable(boolean deliverable) { isDeliverable = deliverable; }

        public java.math.BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(java.math.BigDecimal shippingFee) { this.shippingFee = shippingFee; }

        public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
        public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}