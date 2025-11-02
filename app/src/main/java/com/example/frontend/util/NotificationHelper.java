package com.example.frontend.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.frontend.R;
import com.example.frontend.ui.home.HomeActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "food_app_notifications";
    private static final String CHANNEL_NAME = "Thông báo ứng dụng";
    private static final String CHANNEL_DESCRIPTION = "Nhận thông báo về đơn hàng, khuyến mãi và các thông tin quan trọng khác";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Tạo notification channel cho Android 8.0+ (API 26+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Hiển thị notification với title và message
     */
    public void showNotification(String title, String message, java.util.Map<String, String> data) {
        if (title == null) {
            title = context.getString(R.string.app_name);
        }
        if (message == null) {
            message = "Bạn có thông báo mới";
        }

        // Intent để mở app khi click vào notification
        Intent intent = new Intent(context, HomeActivity.class);
        
        // Thêm data từ FCM vào intent nếu có
        if (data != null) {
            for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Sound mặc định cho notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Tạo notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications) // Icon notification
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Hiển thị notification
        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    /**
     * Hiển thị notification đơn giản
     */
    public void showSimpleNotification(String title, String message) {
        showNotification(title, message, null);
    }
}

