package com.example.frontend.util;

public class ImageUrlBuilder {
    private static final String BASE_URL = "http://192.168.1.3:8080";

    public static String buildFullUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // Nếu URL đã là full URL, return as is
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }

        // Nếu URL bắt đầu bằng /, thêm BASE_URL
        if (imageUrl.startsWith("/")) {
            return BASE_URL + imageUrl;
        }

        // Nếu URL không bắt đầu bằng /, thêm BASE_URL + /
        return BASE_URL + "/" + imageUrl;
    }
}
