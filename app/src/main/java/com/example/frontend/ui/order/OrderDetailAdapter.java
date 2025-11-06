package com.example.frontend.ui.order;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.OrderItem;
import com.example.frontend.util.PriceFormatter;
import com.example.frontend.util.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private List<OrderItem> orderItems;

    public OrderDetailAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_product, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "OrderDetailAdapter";
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewProductPrice;
        private TextView textViewQuantity;
        private TextView textViewSubtotal;
        private TextView textViewSpecialInstructions;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
            textViewSpecialInstructions = itemView.findViewById(R.id.textViewSpecialInstructions);
        }

        public void bind(OrderItem orderItem) {
            Log.d(TAG, "Binding OrderItem: " + orderItem.getProductName());
            Log.d(TAG, "  - ProductPrice: " + orderItem.getProductPrice());
            Log.d(TAG, "  - Subtotal: " + orderItem.getSubtotal());
            Log.d(TAG, "  - Quantity: " + orderItem.getQuantity());
            Log.d(TAG, "  - ProductImage: " + orderItem.getProductImage());

            // Product name with options
            String productName = orderItem.getProductName();
            if (productName == null || productName.trim().isEmpty()) {
                productName = "Không có tên";
            }
            if (orderItem.getSelectedOptions() != null && !orderItem.getSelectedOptions().isEmpty()) {
                StringBuilder optionsText = new StringBuilder();
                for (var option : orderItem.getSelectedOptions()) {
                    if (optionsText.length() > 0) optionsText.append(", ");
                    optionsText.append(option.getOptionName());
                }
                productName += " (" + optionsText.toString() + ")";
            }
            textViewProductName.setText(productName);

            // Hiển thị giá: nếu thiếu productPrice, suy ra từ subtotal/quantity
            java.math.BigDecimal productPrice = orderItem.getProductPrice();
            if (productPrice == null) {
                Log.d(TAG, "ProductPrice is null, calculating from subtotal/quantity");
                if (orderItem.getSubtotal() != null && orderItem.getQuantity() != null && orderItem.getQuantity() > 0) {
                    try {
                        productPrice = orderItem.getSubtotal().divide(new java.math.BigDecimal(orderItem.getQuantity()), java.math.RoundingMode.HALF_UP);
                        Log.d(TAG, "Calculated price: " + productPrice);
                    } catch (Exception e) {
                        Log.e(TAG, "Error calculating price: " + e.getMessage());
                        productPrice = java.math.BigDecimal.ZERO;
                    }
                } else {
                    Log.w(TAG, "Cannot calculate price: subtotal=" + orderItem.getSubtotal() + ", quantity=" + orderItem.getQuantity());
                    productPrice = java.math.BigDecimal.ZERO;
                }
            }
            textViewProductPrice.setText(PriceFormatter.format(productPrice));

            // Quantity
            if (orderItem.getQuantity() != null) {
                textViewQuantity.setText("x" + orderItem.getQuantity());
            } else {
                textViewQuantity.setText("x0");
            }

            // Subtotal
            textViewSubtotal.setText(PriceFormatter.format(orderItem.getSubtotal()));

            // Special instructions
            if (orderItem.getSpecialInstructions() != null && !orderItem.getSpecialInstructions().isEmpty()) {
                textViewSpecialInstructions.setText("Ghi chú: " + orderItem.getSpecialInstructions());
                textViewSpecialInstructions.setVisibility(View.VISIBLE);
            } else {
                textViewSpecialInstructions.setVisibility(View.GONE);
            }

            // Product image with proper URL building
            String imageUrl = orderItem.getProductImage();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                String fullImageUrl = ImageUrlBuilder.buildFullUrl(imageUrl);
                Log.d(TAG, "Loading image from URL: " + fullImageUrl);
                Picasso.get()
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .fit()
                        .centerCrop()
                        .into(imageViewProduct, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Image loaded successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error loading image: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
            } else {
                Log.w(TAG, "Image URL is null or empty, using placeholder");
                imageViewProduct.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
    }
}
