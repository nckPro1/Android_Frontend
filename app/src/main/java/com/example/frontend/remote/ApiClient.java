package com.example.frontend.remote; // Hoặc package phù hợp của bạn

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 🛑 QUAN TRỌNG:
    // - Dùng máy thật: Thay bằng địa chỉ IP của máy tính đang chạy backend.
    // - Dùng máy ảo (Emulator): Dùng "http://10.0.2.2:8080".
    public static final String BASE_URL = "http://192.168.1.7:8080"; // IP máy tính của bạn

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Dùng Gson để chuyển đổi JSON
                    .build();
        }
        return retrofit;
    }
}