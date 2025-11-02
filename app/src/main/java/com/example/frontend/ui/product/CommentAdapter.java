package com.example.frontend.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.Comment;
import com.example.frontend.util.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment_bubble, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addComments(List<Comment> newComments) {
        if (newComments != null && !newComments.isEmpty()) {
            int startPosition = comments.size();
            comments.addAll(newComments);
            notifyItemRangeInserted(startPosition, newComments.size());
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvUserName;
        private TextView tvContent;
        private TextView tvTime;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(Comment comment) {
            // Set user name
            String userName = comment.getUserName();
            if (userName == null || userName.isEmpty()) {
                userName = "Người dùng";
            }
            tvUserName.setText(userName);

            // Set content
            tvContent.setText(comment.getContent());

            // Set avatar
            String avatarUrl = comment.getUserAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                String fullAvatarUrl = ImageUrlBuilder.buildFullUrl(avatarUrl);
                Picasso.get()
                        .load(fullAvatarUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_profile);
            }

            // Set time
            LocalDateTime createdAt = comment.getCreatedAt();
            if (createdAt != null) {
                String timeText = formatTime(createdAt);
                tvTime.setText(timeText);
            } else {
                tvTime.setText("");
            }
        }

        private String formatTime(LocalDateTime dateTime) {
            if (dateTime == null) return "";

            try {
                LocalDateTime now = LocalDateTime.now();
                long secondsDiff = java.time.Duration.between(dateTime, now).getSeconds();

                if (secondsDiff < 60) {
                    return "Vừa xong";
                } else if (secondsDiff < 3600) {
                    long minutes = secondsDiff / 60;
                    return minutes + " phút trước";
                } else if (secondsDiff < 86400) {
                    long hours = secondsDiff / 3600;
                    return hours + " giờ trước";
                } else if (secondsDiff < 604800) {
                    long days = secondsDiff / 86400;
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

