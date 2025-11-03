package com.example.frontend.util;

import android.content.Context;
import android.util.Log;

import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.Notification;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager để quản lý notifications cho user
 */
public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static NotificationManager instance;
    private ApiService apiService;
    private TokenManager tokenManager;
    private List<Notification> notifications;
    private long unreadCount = 0;
    private NotificationUpdateListener listener;

    public interface NotificationUpdateListener {
        void onNotificationsUpdated(List<Notification> notifications);
        void onUnreadCountUpdated(long count);
    }

    private NotificationManager(Context context) {
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.tokenManager = new TokenManager(context);
        this.notifications = new ArrayList<>();
    }

    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setListener(NotificationUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Lấy tất cả notifications của user
     */
    public void fetchNotifications() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            Log.w(TAG, "No token available");
            return;
        }

        Call<ApiResponse<List<Notification>>> call = apiService.getUserNotifications("Bearer " + token);
        call.enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Notification>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        notifications = apiResponse.getData();
                        if (listener != null) {
                            listener.onNotificationsUpdated(notifications);
                        }
                        Log.d(TAG, "Fetched " + notifications.size() + " notifications");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch notifications: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                Log.e(TAG, "Error fetching notifications", t);
            }
        });
    }

    /**
     * Lấy notifications chưa đọc
     */
    public void fetchUnreadNotifications() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            Log.w(TAG, "No token available");
            return;
        }

        Call<ApiResponse<List<Notification>>> call = apiService.getUnreadNotifications("Bearer " + token);
        call.enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Notification>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        notifications = apiResponse.getData();
                        if (listener != null) {
                            listener.onNotificationsUpdated(notifications);
                        }
                        Log.d(TAG, "Fetched " + notifications.size() + " unread notifications");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch unread notifications: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                Log.e(TAG, "Error fetching unread notifications", t);
            }
        });
    }

    /**
     * Lấy số lượng notifications chưa đọc
     */
    public void fetchUnreadCount() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            Log.w(TAG, "No token available");
            return;
        }

        Call<ApiResponse<Long>> call = apiService.getUnreadCount("Bearer " + token);
        call.enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Long> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        unreadCount = apiResponse.getData();
                        if (listener != null) {
                            listener.onUnreadCountUpdated(unreadCount);
                        }
                        Log.d(TAG, "Unread count: " + unreadCount);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch unread count: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                Log.e(TAG, "Error fetching unread count", t);
            }
        });
    }

    /**
     * Đánh dấu notification là đã đọc
     */
    public void markAsRead(Long notificationId, Runnable onSuccess) {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            Log.w(TAG, "No token available");
            return;
        }

        Call<ApiResponse> call = apiService.markNotificationAsRead("Bearer " + token, notificationId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Cập nhật local notification
                    for (Notification notification : notifications) {
                        if (notification.getNotificationId().equals(notificationId)) {
                            notification.setIsRead(true);
                            break;
                        }
                    }
                    // Refresh unread count
                    fetchUnreadCount();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    Log.d(TAG, "Marked notification as read: " + notificationId);
                } else {
                    Log.e(TAG, "Failed to mark notification as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Error marking notification as read", t);
            }
        });
    }

    /**
     * Đánh dấu tất cả notifications là đã đọc
     */
    public void markAllAsRead(Runnable onSuccess) {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            Log.w(TAG, "No token available");
            return;
        }

        Call<ApiResponse> call = apiService.markAllNotificationsAsRead("Bearer " + token);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Cập nhật tất cả notifications là đã đọc
                    for (Notification notification : notifications) {
                        notification.setIsRead(true);
                    }
                    unreadCount = 0;
                    if (listener != null) {
                        listener.onNotificationsUpdated(notifications);
                        listener.onUnreadCountUpdated(0);
                    }
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    Log.d(TAG, "Marked all notifications as read");
                } else {
                    Log.e(TAG, "Failed to mark all notifications as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Error marking all notifications as read", t);
            }
        });
    }

    // Getters
    public List<Notification> getNotifications() {
        return notifications;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    /**
     * Thêm notification mới (từ WebSocket hoặc FCM)
     */
    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Thêm vào đầu list
        if (!notification.isRead()) {
            unreadCount++;
        }
        if (listener != null) {
            listener.onNotificationsUpdated(notifications);
            listener.onUnreadCountUpdated(unreadCount);
        }
    }
}

