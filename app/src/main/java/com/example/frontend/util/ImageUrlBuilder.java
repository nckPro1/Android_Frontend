package com.example.frontend.util;

public class ImageUrlBuilder {
    private static final String BASE_URL = "http://10.33.71.21:8080";

    public static String buildFullUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // Nếu URL đã là full URL, rewrite toàn bộ host http sang BASE_URL và giữ nguyên path
        if (imageUrl.startsWith("http")) {
            String url = imageUrl.trim();
            // Chỉ rewrite các URL http (cleartext) sang host public
            if (url.startsWith("http://")) {
                int slashIndex = url.indexOf('/', "http://".length());
                String path = slashIndex >= 0 ? url.substring(slashIndex) : "";
                // Đảm bảo ghép path đúng
                if (path.startsWith("/")) {
                    return BASE_URL + path;
                } else if (path.isEmpty()) {
                    return BASE_URL;
                } else {
                    return BASE_URL + "/" + path;
                }
            }
            // Giữ nguyên nếu đã là https với host hợp lệ
            return url;
        }

        // Nếu URL bắt đầu bằng /, thêm BASE_URL
        if (imageUrl.startsWith("/")) {
            return BASE_URL + imageUrl;
        }

        // Nếu URL không bắt đầu bằng /, thêm BASE_URL + /
        return BASE_URL + "/" + imageUrl;
    }
}
