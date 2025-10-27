package com.example.frontend.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.CartItem;
import com.example.frontend.util.PriceFormatter;
import com.example.frontend.util.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> cartItems;

    public CheckoutAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewProductPrice;
        private TextView textViewQuantity;
        private TextView textViewSubtotal;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
        }

        public void bind(CartItem cartItem) {
            // Product name with options
            String productName = cartItem.getProductName();
            if (cartItem.getSelectedOptions() != null && !cartItem.getSelectedOptions().isEmpty()) {
                StringBuilder optionsText = new StringBuilder();
                for (var option : cartItem.getSelectedOptions()) {
                    if (optionsText.length() > 0) optionsText.append(", ");
                    optionsText.append(option.getOptionName());
                }
                productName += " (" + optionsText.toString() + ")";
            }
            textViewProductName.setText(productName);
            
            textViewProductPrice.setText(PriceFormatter.format(cartItem.getUnitPrice()));
            textViewQuantity.setText("x" + cartItem.getQuantity());
            textViewSubtotal.setText(PriceFormatter.format(cartItem.getTotalPrice()));

            // Load product image with proper URL building
            String imageUrl = cartItem.getProductImageUrl();
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
