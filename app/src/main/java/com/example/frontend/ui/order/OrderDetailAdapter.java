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
            textViewProductName.setText(orderItem.getProductName());
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

            // Product image
            if (orderItem.getProductImage() != null && !orderItem.getProductImage().isEmpty()) {
                Picasso.get()
                        .load(orderItem.getProductImage())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
