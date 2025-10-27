package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.CartItem;
import com.example.frontend.model.ProductOption;
import com.example.frontend.util.ImageUrlBuilder;
import com.example.frontend.util.PriceFormatter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(int position, int newQuantity);
        void onItemRemoved(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
        setHasStableIds(true); // giúp RecyclerView ổn định animation khi xoá/sửa
    }

    @Override
    public long getItemId(int position) {
        // Sử dụng productId + hashCode của options để tạo stable ID
        if (position < 0 || position >= cartItems.size()) return RecyclerView.NO_ID;
        CartItem item = cartItems.get(position);
        if (item == null) return RecyclerView.NO_ID;

        // Tạo ID ổn định dựa trên productId và options
        long baseId = item.getProductId() != null ? item.getProductId() : 0;
        if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
            baseId = baseId * 1000 + item.getSelectedOptions().hashCode();
        }
        return baseId;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item); // ❗️Không truyền position; luôn lấy position khi click
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductDescription, textViewOptions;
        TextView textViewQuantity, textViewUnitPrice, textViewTotalPrice;
        View buttonDecrease, buttonIncrease, buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewOptions = itemView.findViewById(R.id.textViewOptions);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewUnitPrice = itemView.findViewById(R.id.textViewUnitPrice);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }

        public void bind(CartItem item) {
            // Ảnh sản phẩm — fix không hiển thị:
            // - Sử dụng ImageUrlBuilder để tạo URL đầy đủ
            // - Có placeholder + error
            // - fit/centerCrop để hiển thị đẹp
            // - Callback để có thể log nếu lỗi
            String url = item.getProductImageUrl();
            if (url != null && !url.trim().isEmpty()) {
                String fullImageUrl = ImageUrlBuilder.buildFullUrl(url);
                Picasso.get()
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .fit()
                        .centerCrop()
                        .into(imageViewProduct, new Callback() {
                            @Override
                            public void onSuccess() { /* no-op */ }

                            @Override
                            public void onError(Exception e) {
                                // Có thể ghi log nếu muốn
                                // Log.e("CartAdapter", "Picasso load error: " + e.getMessage());
                            }
                        });
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_food_placeholder);
            }

            // Tên và mô tả
            textViewProductName.setText(item.getProductName());
            if (item.getProductDescription() != null && !item.getProductDescription().isEmpty()) {
                textViewProductDescription.setText(item.getProductDescription());
                textViewProductDescription.setVisibility(View.VISIBLE);
            } else {
                textViewProductDescription.setVisibility(View.GONE);
            }

            // Tuỳ chọn
            if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
                StringBuilder optionsText = new StringBuilder();
                for (ProductOption option : item.getSelectedOptions()) {
                    if (optionsText.length() > 0) optionsText.append(", ");
                    optionsText.append(option.getOptionName());
                    if (option.getPrice() != null && option.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                        optionsText.append(" (+").append(PriceFormatter.format(option.getPrice())).append(")");
                    }
                }
                textViewOptions.setText(optionsText.toString());
                textViewOptions.setVisibility(View.VISIBLE);
            } else {
                textViewOptions.setVisibility(View.GONE);
            }

            // Số lượng & giá
            textViewQuantity.setText(String.valueOf(item.getQuantity()));
            textViewUnitPrice.setText(PriceFormatter.format(item.getCurrentPrice()));

            BigDecimal totalPrice = (item.getCurrentPrice() != null ? item.getCurrentPrice() : BigDecimal.ZERO)
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            if (item.getSelectedOptions() != null) {
                for (ProductOption option : item.getSelectedOptions()) {
                    if (option.getPrice() != null) {
                        totalPrice = totalPrice.add(option.getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }
            textViewTotalPrice.setText(PriceFormatter.format(totalPrice));

            // Nút trừ
            buttonDecrease.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                android.util.Log.d("CartAdapter", "Decrease button clicked - binding position: " + pos);
                if (pos == RecyclerView.NO_POSITION || pos < 0 || pos >= cartItems.size()) {
                    android.util.Log.w("CartAdapter", "Invalid binding position for decrease: " + pos);
                    return;
                }
                CartItem cur = cartItems.get(pos);
                if (cur == null) {
                    android.util.Log.w("CartAdapter", "CartItem is null at binding position: " + pos);
                    return;
                }
                int newQuantity = cur.getQuantity() - 1;
                if (newQuantity >= 0) {
                    android.util.Log.d("CartAdapter", "Decreasing quantity for: " + cur.getProductName() + " (pos: " + pos + ") to: " + newQuantity);
                    listener.onQuantityChanged(pos, newQuantity);
                }
            });

            // Nút cộng
            buttonIncrease.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                android.util.Log.d("CartAdapter", "Increase button clicked - binding position: " + pos);
                if (pos == RecyclerView.NO_POSITION || pos < 0 || pos >= cartItems.size()) {
                    android.util.Log.w("CartAdapter", "Invalid binding position for increase: " + pos);
                    return;
                }
                CartItem cur = cartItems.get(pos);
                if (cur == null) {
                    android.util.Log.w("CartAdapter", "CartItem is null at binding position: " + pos);
                    return;
                }
                int newQuantity = cur.getQuantity() + 1;
                android.util.Log.d("CartAdapter", "Increasing quantity for: " + cur.getProductName() + " (pos: " + pos + ") to: " + newQuantity);
                listener.onQuantityChanged(pos, newQuantity);
            });

            // Nút xoá
            buttonRemove.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                android.util.Log.d("CartAdapter", "Remove button clicked - binding position: " + pos);
                if (pos == RecyclerView.NO_POSITION || pos < 0 || pos >= cartItems.size()) {
                    android.util.Log.w("CartAdapter", "Invalid binding position for remove: " + pos);
                    return;
                }
                CartItem cur = cartItems.get(pos);
                if (cur == null) {
                    android.util.Log.w("CartAdapter", "CartItem is null at binding position: " + pos);
                    return;
                }
                android.util.Log.d("CartAdapter", "Removing item: " + cur.getProductName() + " (pos: " + pos + ")");
                listener.onItemRemoved(pos);
            });
        }
    }
}
