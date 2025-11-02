package com.example.frontend.util;

import android.content.Context;
import android.util.Log;

import com.example.frontend.local.TokenManager;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FCMTokenManager {
    private static final String TAG = "FCMTokenManager";
    private static final String PREF_NAME = "FCM_PREFS";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_TOKEN_SENT = "token_sent_to_server";

    private Context context;
    private TokenManager tokenManager;
    private ApiService apiService;
    private android.content.SharedPreferences prefs;

    public FCMTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = new TokenManager(context);
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lấy FCM token và gửi lên server nếu user đã đăng nhập
     */
    public void getAndSendToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Lấy token thành công
                        String token = task.getResult();
                        Log.d(TAG, "FCM Registration Token: " + token);

                        // Lưu token vào SharedPreferences
                        saveTokenToPrefs(token);

                        // Nếu user đã đăng nhập, gửi token lên server
                        if (tokenManager.isLoggedIn()) {
                            sendTokenToServer(token);
                        } else {
                            Log.d(TAG, "User chưa đăng nhập, token sẽ được gửi sau khi login");
                        }
                    }
                });
    }

    /**
     * Gửi FCM token lên server
     */
    public void sendTokenToServer(String token) {
        // Kiểm tra user đã đăng nhập chưa
        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "User chưa đăng nhập, không thể gửi token");
            return;
        }

        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null) {
            Log.w(TAG, "Access token không tồn tại");
            return;
        }

        // Kiểm tra xem token này đã được gửi chưa
        String lastSentToken = prefs.getString(KEY_FCM_TOKEN, null);
        boolean tokenSent = prefs.getBoolean(KEY_TOKEN_SENT, false);
        
        if (token.equals(lastSentToken) && tokenSent) {
            Log.d(TAG, "Token đã được gửi trước đó, bỏ qua");
            return;
        }

        String authHeader = "Bearer " + accessToken;
        long userId = tokenManager.getUserId();

        Log.d(TAG, "Đang gửi FCM token lên server cho user ID: " + userId);

        apiService.updateFCMToken(authHeader, userId, token).enqueue(new Callback<com.example.frontend.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.frontend.model.ApiResponse> call, Response<com.example.frontend.model.ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ FCM token đã được gửi lên server thành công");
                    // Đánh dấu token đã được gửi
                    prefs.edit()
                            .putString(KEY_FCM_TOKEN, token)
                            .putBoolean(KEY_TOKEN_SENT, true)
                            .apply();
                } else {
                    Log.e(TAG, "❌ Lỗi khi gửi FCM token lên server. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.frontend.model.ApiResponse> call, Throwable t) {
                Log.e(TAG, "❌ Lỗi kết nối khi gửi FCM token", t);
            }
        });
    }

    /**
     * Lưu token vào SharedPreferences
     */
    private void saveTokenToPrefs(String token) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    /**
     * Lấy token đã lưu (nếu có)
     */
    public String getSavedToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Xóa token khi user logout
     */
    public void clearToken() {
        prefs.edit()
                .remove(KEY_FCM_TOKEN)
                .putBoolean(KEY_TOKEN_SENT, false)
                .apply();
    }

    /**
     * Đánh dấu cần gửi lại token (khi user login lại)
     */
    public void markTokenNeedsResend() {
        prefs.edit().putBoolean(KEY_TOKEN_SENT, false).apply();
    }
}

