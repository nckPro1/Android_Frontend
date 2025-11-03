package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.Notification;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTime;
        private Chip chipUnread;
        private View viewUnreadIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            chipUnread = itemView.findViewById(R.id.chipUnread);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        void bind(Notification notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            // Set icon based on type
            int iconResId = getIconForType(notification.getType());
            ivIcon.setImageResource(iconResId);

            // Format time
            if (notification.getCreatedAt() != null) {
                tvTime.setText(formatTime(notification.getCreatedAt()));
            } else {
                tvTime.setText("");
            }

            // Show/hide unread indicator
            boolean isRead = notification.isRead();
            if (chipUnread != null) {
                chipUnread.setVisibility(isRead ? View.GONE : View.VISIBLE);
            }
            if (viewUnreadIndicator != null) {
                viewUnreadIndicator.setVisibility(isRead ? View.GONE : View.VISIBLE);
            }

            // Set background color for unread notifications
            if (!isRead) {
                itemView.setAlpha(1.0f);
            } else {
                itemView.setAlpha(0.7f);
            }
        }

        private int getIconForType(Notification.NotificationType type) {
            if (type == null) return R.drawable.ic_notifications;

            switch (type) {
                case ORDER_STATUS_UPDATED:
                    return android.R.drawable.ic_menu_view;
                case ADMIN_MESSAGE:
                    return android.R.drawable.ic_dialog_email;
                case PROMOTION:
                    return android.R.drawable.ic_menu_agenda;
                case NEW_ORDER:
                    return android.R.drawable.ic_menu_recent_history;
                default:
                    return R.drawable.ic_notifications;
            }
        }

        private String formatTime(LocalDateTime dateTime) {
            try {
                LocalDateTime now = LocalDateTime.now();
                java.time.Duration duration = java.time.Duration.between(dateTime, now);

                long seconds = duration.getSeconds();
                if (seconds < 60) {
                    return "Vừa xong";
                } else if (seconds < 3600) {
                    long minutes = seconds / 60;
                    return minutes + " phút trước";
                } else if (seconds < 86400) {
                    long hours = seconds / 3600;
                    return hours + " giờ trước";
                } else if (seconds < 604800) {
                    long days = seconds / 86400;
                    return days + " ngày trước";
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
                    return dateTime.format(formatter);
                }
            } catch (Exception e) {
                return "";
            }
        }
    }
}

