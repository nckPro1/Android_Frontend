package com.example.frontend.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.Notification;
import com.example.frontend.ui.adapter.NotificationAdapter;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.ui.chat.ChatActivity;
import com.example.frontend.ui.order.OrderDetailActivity;
import com.example.frontend.util.NotificationManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationListActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private TextView textViewTitle;
    private TextView textViewMarkAllRead;
    private RecyclerView recyclerViewNotifications;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private TokenManager tokenManager;
    private NotificationManager notificationManager;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        // Check authentication
        tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        initData();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        imageViewBack = findViewById(R.id.imageViewBack);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewMarkAllRead = findViewById(R.id.textViewMarkAllRead);
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        // Setup RecyclerView
        adapter = new NotificationAdapter(notifications, this::onNotificationClick);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(adapter);
    }

    private void initData() {
        notificationManager = NotificationManager.getInstance(this);
        notificationManager.setListener(new NotificationManager.NotificationUpdateListener() {
            @Override
            public void onNotificationsUpdated(List<Notification> updatedNotifications) {
                runOnUiThread(() -> {
                    notifications.clear();
                    notifications.addAll(updatedNotifications);
                    adapter.updateNotifications(notifications);
                    updateEmptyState();
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onUnreadCountUpdated(long count) {
                // Can update badge if needed
            }
        });
    }

    private void setupListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        textViewMarkAllRead.setOnClickListener(v -> {
            notificationManager.markAllAsRead(() -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã đánh dấu tất cả đã đọc", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
            });
        });
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        notificationManager.fetchNotifications();
        notificationManager.fetchUnreadCount();
    }

    private void onNotificationClick(Notification notification) {
        // Mark as read
        notificationManager.markAsRead(notification.getNotificationId(), () -> {
            runOnUiThread(() -> {
                // Navigate based on notification type
                navigateFromNotification(notification);
            });
        });
    }

    private void navigateFromNotification(Notification notification) {
        if (notification.getRelatedType() == null || notification.getRelatedId() == null) {
            return;
        }

        Intent intent = null;

        switch (notification.getRelatedType()) {
            case "ORDER":
                intent = new Intent(this, OrderDetailActivity.class);
                intent.putExtra("orderId", notification.getRelatedId());
                break;
            case "CONVERSATION":
                intent = new Intent(this, ChatActivity.class);
                intent.putExtra("conversationId", notification.getRelatedId());
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void updateEmptyState() {
        if (notifications.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewNotifications.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notifications when returning to this activity
        if (tokenManager.isLoggedIn()) {
            loadNotifications();
        }
    }
}

