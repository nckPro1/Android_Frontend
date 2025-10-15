package com.example.frontend.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.frontend.model.ApiResponse;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUploadUtil {

    public interface UploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadError(String error);
    }

    private static final String TAG = "ImageUploadUtil";

    public static void uploadImage(Context context, Uri imageUri, String token, UploadCallback callback) {
        try {
            // Convert URI to File
            File imageFile = createFileFromUri(context, imageUri);
            if (imageFile == null) {
                callback.onUploadError("Không thể tạo file từ ảnh");
                return;
            }

            // Create request body
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    imageFile.getName(),
                    requestFile
            );

            // Make API call
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<ApiResponse> call = apiService.uploadAvatar("Bearer " + token, body);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            // Extract image URL from response
                            String imageUrl = extractImageUrlFromResponse(response.body());
                            callback.onUploadSuccess(imageUrl);
                        } else {
                            callback.onUploadError(response.body().getMessage());
                        }
                    } else {
                        callback.onUploadError("Upload thất bại: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e(TAG, "Upload error", t);
                    callback.onUploadError("Lỗi kết nối: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Upload exception", e);
            callback.onUploadError("Lỗi upload: " + e.getMessage());
        }
    }

    private static File createFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Compress image to reduce file size
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            // Resize bitmap to max 800x800 pixels
            int maxSize = 800;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxSize || height > maxSize) {
                float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            // Save compressed bitmap
            File file = new File(context.getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream); // 80% quality
            outputStream.close();
            bitmap.recycle();

            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    private static String extractImageUrlFromResponse(ApiResponse response) {
        // Extract image URL from response data
        if (response.getData() != null) {
            String url = response.getData().toString();
            Log.d(TAG, "Extracted URL from response: " + url);
            return url;
        }
        // Fallback URL
        Log.w(TAG, "No data in response, using fallback URL");
        return "https://via.placeholder.com/150/FF5722/FFFFFF?text=Avatar";
    }
}
