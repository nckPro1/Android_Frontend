package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.R;
import com.example.frontend.model.MessageDTO;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<MessageDTO> messages;

    public MessageAdapter(List<MessageDTO> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("MessageAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (messages == null || position >= messages.size()) {
            return;
        }
        MessageDTO message = messages.get(position);
        if (message != null) {
            holder.bind(message);
        }
    }

    @Override
    public int getItemCount() {
        int count = messages != null ? messages.size() : 0;
        android.util.Log.d("MessageAdapter", "getItemCount called: " + count);
        return count;
    }

    public void updateMessages(List<MessageDTO> newMessages) {
        android.util.Log.d("MessageAdapter", "updateMessages called with " + (newMessages != null ? newMessages.size() : 0) + " messages");
        this.messages = newMessages != null ? new ArrayList<>(newMessages) : new ArrayList<>();
        notifyDataSetChanged();
        android.util.Log.d("MessageAdapter", "After notifyDataSetChanged, itemCount: " + getItemCount());
    }

    public void addMessage(MessageDTO message) {
        if (messages != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    public List<MessageDTO> getMessages() {
        return messages != null ? new ArrayList<>(messages) : new ArrayList<>();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardMessage;
        private TextView tvMessageContent;
        private TextView tvTime;
        private View spacerStart;
        private View spacerEnd;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMessage = itemView.findViewById(R.id.cardMessage);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            spacerStart = itemView.findViewById(R.id.spacerStart);
            spacerEnd = itemView.findViewById(R.id.spacerEnd);
        }

        void bind(MessageDTO message) {
            android.util.Log.d("MessageAdapter", "Binding message: " + message.getMessageId() + " - " + message.getContent());

            if (message.getContent() != null) {
                tvMessageContent.setText(message.getContent());
            } else {
                tvMessageContent.setText("");
            }

            // Format time
            if (message.getCreatedAt() != null) {
                tvTime.setText(formatTime(message.getCreatedAt()));
            } else {
                tvTime.setText("");
            }

            // Layout based on isOwnMessage
            if (message.getIsOwnMessage() != null && message.getIsOwnMessage()) {
                // Own message - align right
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    cardMessage.setCardBackgroundColor(itemView.getContext().getColor(R.color.colorPrimary));
                    tvMessageContent.setTextColor(itemView.getContext().getColor(android.R.color.white));
                    tvTime.setTextColor(itemView.getContext().getColor(android.R.color.white));
                } else {
                    cardMessage.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                    tvMessageContent.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                    tvTime.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                }
                spacerStart.setVisibility(View.VISIBLE);
                spacerEnd.setVisibility(View.GONE);
            } else {
                // Received message - align left
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    cardMessage.setCardBackgroundColor(itemView.getContext().getColor(R.color.message_received_bg));
                    tvMessageContent.setTextColor(itemView.getContext().getColor(android.R.color.black));
                    tvTime.setTextColor(itemView.getContext().getColor(R.color.message_time_color));
                } else {
                    cardMessage.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.message_received_bg));
                    tvMessageContent.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
                    tvTime.setTextColor(itemView.getContext().getResources().getColor(R.color.message_time_color));
                }
                spacerStart.setVisibility(View.GONE);
                spacerEnd.setVisibility(View.VISIBLE);
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

