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
                    // Nếu Unauthorized do access token hết hạn, thử refresh và gửi lại một lần
                    if (response.code() == 401) {
                        Log.w(TAG, "Access token hết hạn (401), đang thử refresh token...");
                        String refreshToken = tokenManager.getRefreshToken();
                        if (refreshToken != null && !refreshToken.isEmpty()) {
                            Log.d(TAG, "Refresh token có sẵn, đang gọi API refresh...");
                            java.util.Map<String, String> body = new java.util.HashMap<>();
                            body.put("refreshToken", refreshToken);
                            apiService.refreshToken(body).enqueue(new Callback<com.example.frontend.model.AuthResponse>() {
                                @Override
                                public void onResponse(Call<com.example.frontend.model.AuthResponse> call2, Response<com.example.frontend.model.AuthResponse> resp2) {
                                    Log.d(TAG, "Refresh token response code: " + resp2.code());
                                    if (resp2.isSuccessful() && resp2.body() != null && resp2.body().isSuccess()) {
                                        String newAccess = resp2.body().getAccessToken();
                                        String newRefresh = resp2.body().getRefreshToken();
                                        if (newAccess != null) {
                                            Log.i(TAG, "✅ Refresh token thành công, lưu token mới và gửi lại FCM token");
                                            tokenManager.saveTokens(newAccess, newRefresh != null ? newRefresh : refreshToken);
                                            // Thử gửi lại một lần với token mới
                                            apiService.updateFCMToken("Bearer " + newAccess, userId, token)
                                                    .enqueue(new Callback<com.example.frontend.model.ApiResponse>() {
                                                        @Override
                                                        public void onResponse(Call<com.example.frontend.model.ApiResponse> call3, Response<com.example.frontend.model.ApiResponse> resp3) {
                                                            if (resp3.isSuccessful()) {
                                                                Log.i(TAG, "✅ FCM token đã được gửi sau khi refresh token");
                                                                prefs.edit()
                                                                        .putString(KEY_FCM_TOKEN, token)
                                                                        .putBoolean(KEY_TOKEN_SENT, true)
                                                                        .apply();
                                                            } else {
                                                                Log.e(TAG, "❌ Gửi lại FCM token thất bại. Code: " + resp3.code());
                                                                if (resp3.errorBody() != null) {
                                                                    try {
                                                                        Log.e(TAG, "Error body: " + resp3.errorBody().string());
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        @Override
                                                        public void onFailure(Call<com.example.frontend.model.ApiResponse> call3, Throwable t3) {
                                                            Log.e(TAG, "❌ Lỗi kết nối khi gửi lại FCM token", t3);
                                                        }
                                                    });
                                        } else {
                                            Log.e(TAG, "❌ Refresh token response không chứa access token mới");
                                        }
                                    } else {
                                        String errorMsg = "code=" + resp2.code();
                                        if (resp2.body() != null) {
                                            errorMsg = resp2.body().getMessage();
                                        } else if (resp2.errorBody() != null) {
                                            try {
                                                errorMsg = resp2.errorBody().string();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Log.e(TAG, "❌ Refresh token thất bại: " + errorMsg);
                                    }
                                }
                                @Override
                                public void onFailure(Call<com.example.frontend.model.AuthResponse> call2, Throwable t2) {
                                    Log.e(TAG, "❌ Lỗi kết nối khi refresh token", t2);
                                    if (t2.getMessage() != null) {
                                        Log.e(TAG, "Error message: " + t2.getMessage());
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "Không có refresh token để làm mới access token");
                        }
                    }
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

