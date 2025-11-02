package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.R;
import com.example.frontend.model.ConversationDTO;
import com.example.frontend.model.MessageDTO;
import com.example.frontend.util.ImageUrlBuilder;
import com.google.android.material.chip.Chip;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<ConversationDTO> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(ConversationDTO conversation);
    }

    public ConversationAdapter(List<ConversationDTO> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationDTO conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    public void updateConversations(List<ConversationDTO> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvSubject;
        private TextView tvLastMessage;
        private TextView tvTime;
        private Chip chipUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            chipUnread = itemView.findViewById(R.id.chipUnread);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onConversationClick(conversations.get(position));
                }
            });
        }

        void bind(ConversationDTO conversation) {
            // Subject
            if (conversation.getSubject() != null && !conversation.getSubject().isEmpty()) {
                tvSubject.setText(conversation.getSubject());
            } else {
                tvSubject.setText("Chat với Admin");
            }

            // Last message
            MessageDTO lastMessage = conversation.getLastMessage();
            if (lastMessage != null && lastMessage.getContent() != null) {
                tvLastMessage.setText(lastMessage.getContent());
            } else {
                tvLastMessage.setText("Chưa có tin nhắn");
            }

            // Time
            if (conversation.getUpdatedAt() != null) {
                tvTime.setText(formatTime(conversation.getUpdatedAt()));
            }

            // Avatar - lấy từ participants (admin hoặc user khác)
            if (conversation.getParticipants() != null && !conversation.getParticipants().isEmpty()) {
                String avatarUrl = conversation.getCreatedByAvatarUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Picasso.get().load(ImageUrlBuilder.buildFullUrl(avatarUrl))
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            // Unread count
            Long unreadCount = conversation.getUnreadCount();
            if (unreadCount != null && unreadCount > 0) {
                chipUnread.setVisibility(View.VISIBLE);
                chipUnread.setText(String.valueOf(unreadCount));
            } else {
                chipUnread.setVisibility(View.GONE);
            }
        }

        private String formatTime(java.time.LocalDateTime dateTime) {
            if (dateTime == null) return "";
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
                return dateTime.format(formatter);
            } catch (Exception e) {
                return "";
            }
        }
    }
}

