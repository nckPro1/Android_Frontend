package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.R;
import com.example.frontend.model.ProfileMenuItem;
import java.util.List;

public class ProfileMenuAdapter extends RecyclerView.Adapter<ProfileMenuAdapter.MenuViewHolder> {

    private final List<ProfileMenuItem> menuItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ProfileMenuItem item);
    }

    public ProfileMenuAdapter(List<ProfileMenuItem> menuItems, OnItemClickListener listener) {
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        ProfileMenuItem item = menuItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuIcon;
        TextView tvMenuLabel;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuIcon = itemView.findViewById(R.id.ivMenuIcon);
            tvMenuLabel = itemView.findViewById(R.id.tvMenuLabel);
        }

        public void bind(final ProfileMenuItem item, final OnItemClickListener listener) {
            ivMenuIcon.setImageResource(item.getIconResId());
            tvMenuLabel.setText(item.getLabel());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}