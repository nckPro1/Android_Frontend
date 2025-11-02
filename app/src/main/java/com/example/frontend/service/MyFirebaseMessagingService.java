package com.example.frontend.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.frontend.R;
import com.example.frontend.ui.home.HomeActivity;
import com.example.frontend.util.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Kiểm tra xem message có chứa data payload không
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Kiểm tra xem message có chứa notification payload không
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Hiển thị notification
            sendNotification(title, body, remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Gửi token mới lên server
        sendTokenToServer(token);
    }

    /**
     * Hiển thị notification khi nhận được message từ FCM
     */
    private void sendNotification(String title, String messageBody, java.util.Map<String, String> data) {
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
        notificationHelper.showNotification(title, messageBody, data);
    }

    /**
     * Gửi FCM token lên server
     * Sẽ được gọi tự động khi token được refresh
     */
    private void sendTokenToServer(String token) {
        // Token sẽ được gửi lên server thông qua FCMTokenManager
        // Method này được gọi tự động khi token refresh
        // Token sẽ được lưu và gửi khi user đăng nhập
    }
}

