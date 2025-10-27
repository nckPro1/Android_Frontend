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
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.CheckoutItemViewHolder> {

    private List<CartItem> cartItems;

    public CheckoutItemAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CheckoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout, parent, false);
        return new CheckoutItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutItemViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CheckoutItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewOptions;
        private TextView textViewQuantity;
        private TextView textViewTotalPrice;

        public CheckoutItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewOptions = itemView.findViewById(R.id.textViewOptions);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
        }

        public void bind(CartItem item) {
            // Product image - sử dụng ImageUrlBuilder và Picasso
            String url = item.getProductImageUrl();
            if (url != null && !url.trim().isEmpty()) {
                String fullImageUrl = ImageUrlBuilder.buildFullUrl(url);
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

            // Product name
            textViewProductName.setText(item.getProductName());

            // Selected options
            if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
                StringBuilder optionsText = new StringBuilder();
                for (ProductOption option : item.getSelectedOptions()) {
                    if (optionsText.length() > 0) {
                        optionsText.append(", ");
                    }
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

            // Quantity
            textViewQuantity.setText("x" + item.getQuantity());

            // Total price
            BigDecimal unitPrice = item.getCurrentPrice();
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // Add option prices
            if (item.getSelectedOptions() != null) {
                for (ProductOption option : item.getSelectedOptions()) {
                    if (option.getPrice() != null) {
                        totalPrice = totalPrice.add(option.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }

            textViewTotalPrice.setText(PriceFormatter.format(totalPrice));
        }
    }
}
