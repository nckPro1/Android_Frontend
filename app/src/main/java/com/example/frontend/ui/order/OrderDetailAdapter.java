package com.example.frontend.ui.order;

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
            // Product name with options
            String productName = orderItem.getProductName();
            if (orderItem.getSelectedOptions() != null && !orderItem.getSelectedOptions().isEmpty()) {
                StringBuilder optionsText = new StringBuilder();
                for (var option : orderItem.getSelectedOptions()) {
                    if (optionsText.length() > 0) optionsText.append(", ");
                    optionsText.append(option.getOptionName());
                }
                productName += " (" + optionsText.toString() + ")";
            }
            textViewProductName.setText(productName);
            
            textViewProductPrice.setText(PriceFormatter.format(orderItem.getProductPrice()));
            textViewQuantity.setText("x" + orderItem.getQuantity());
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
                Picasso.get()
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .fit()
                        .centerCrop()
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
    }
}
