package com.example.frontend.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.Product;
import com.example.frontend.util.ImageUrlBuilder;
import com.example.frontend.ui.product.ProductDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import android.content.Intent;
import android.content.Context;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductDescription;
        private TextView tvProductPrice;
        private TextView tvOriginalPrice;
        private TextView tvProductCategory;
        private TextView tvPreparationTime;
        private View vFeaturedBadge;
        private TextView tvSaleBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvPreparationTime = itemView.findViewById(R.id.tvPreparationTime);
            vFeaturedBadge = itemView.findViewById(R.id.vFeaturedBadge);
            tvSaleBadge = itemView.findViewById(R.id.tvSaleBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    android.util.Log.d("ProductAdapter", "Clicked product: " + product.getName() + " (ID: " + product.getProductId() + ")");
                    // Navigate to ProductDetailActivity
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("productId", product.getProductId());
                    android.util.Log.d("ProductAdapter", "Starting ProductDetailActivity with productId: " + product.getProductId());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductDescription.setText(product.getDescription());

            // Handle sale price display
            if (product.isOnSale() && product.isSaleActive()) {
                // Show sale badge
                tvSaleBadge.setVisibility(View.VISIBLE);

                // Show original price (strikethrough)
                tvOriginalPrice.setText(product.getFormattedPrice());
                tvOriginalPrice.setVisibility(View.VISIBLE);

                // Show sale price in green
                tvProductPrice.setText(product.getFormattedCurrentPrice());
                tvProductPrice.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));

                // Show sale percentage if available
                if (product.getSalePercentage() != null) {
                    tvSaleBadge.setText("-" + product.getSalePercentage() + "%");
                } else {
                    tvSaleBadge.setText("SALE");
                }
            } else {
                // Hide sale badge and original price
                tvSaleBadge.setVisibility(View.GONE);
                tvOriginalPrice.setVisibility(View.GONE);

                // Show regular price
                tvProductPrice.setText(product.getFormattedPrice());
                tvProductPrice.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }

            tvPreparationTime.setText(product.getPreparationTime() + " phút");

            // Category
            if (product.getCategory() != null) {
                tvProductCategory.setText(product.getCategory().getCategoryName());
                tvProductCategory.setVisibility(View.VISIBLE);
            } else {
                tvProductCategory.setVisibility(View.GONE);
            }

            // Featured badge
            if (product.getIsFeatured() != null && product.getIsFeatured()) {
                vFeaturedBadge.setVisibility(View.VISIBLE);
            } else {
                vFeaturedBadge.setVisibility(View.GONE);
            }

            // Load image với Picasso
            String imageUrl = product.getMainImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String fullImageUrl = ImageUrlBuilder.buildFullUrl(imageUrl);

                Picasso.get()
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_food_placeholder)
                        .error(R.drawable.ic_food_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
    }
}
