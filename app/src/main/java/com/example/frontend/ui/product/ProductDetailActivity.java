package com.example.frontend.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.Product;
import com.example.frontend.model.ProductOption;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.checkout.CheckoutActivity;
import com.example.frontend.util.CartManager;
import com.example.frontend.util.ImageUrlBuilder;
import com.example.frontend.util.JsonParser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ApiService apiService;
    private Product product;
    private Long productId;
    private List<ProductOption> productOptions;
    private CartManager cartManager;

    // Views
    private ImageView ivMainImage;
    private RecyclerView rvGalleryImages;
    private TextView tvProductName;
    private TextView tvProductDescription;
    private TextView tvProductPrice;
    private TextView tvOriginalPrice;
    private TextView tvDiscountPercent;
    private TextView tvSaleBadge;
    private TextView tvTotalPrice;
    private TextView tvProductCategory;
    private TextView tvPreparationTime;
    private TextView tvAvailability;
    private TextView tvFeatured;
    private Button btnAddToCart;
    private Button btnBuyNow;
    private RecyclerView rvProductOptions;
    private ProductOptionsAdapter productOptionsAdapter;
    private com.google.android.material.card.MaterialCardView optionsCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_product_detail);

            android.util.Log.d("ProductDetailActivity", "=== ProductDetailActivity onCreate ===");
            android.util.Log.d("ProductDetailActivity", "About to get product ID from intent...");

            // Get product ID from intent
            productId = getIntent().getLongExtra("productId", -1);
            android.util.Log.d("ProductDetailActivity", "Product ID from intent: " + productId);
            android.util.Log.d("ProductDetailActivity", "About to check if productId is valid...");

            if (productId == -1) {
                Toast.makeText(this, "Product ID không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            android.util.Log.d("ProductDetailActivity", "About to initialize apiService...");
            apiService = ApiClient.getApiService();
            android.util.Log.d("ProductDetailActivity", "ApiService initialized successfully");

            android.util.Log.d("ProductDetailActivity", "About to initialize cartManager...");
            cartManager = CartManager.getInstance(this);
            android.util.Log.d("ProductDetailActivity", "CartManager initialized successfully");

            android.util.Log.d("ProductDetailActivity", "About to call setupViews...");
            setupViews();
            android.util.Log.d("ProductDetailActivity", "setupViews completed successfully");

            android.util.Log.d("ProductDetailActivity", "About to call loadProductDetail...");
            loadProductDetail();
            android.util.Log.d("ProductDetailActivity", "loadProductDetail called successfully");
        } catch (Exception e) {
            android.util.Log.e("ProductDetailActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        applyAnimations();
    }

    private void setupViews() {
        android.util.Log.d("ProductDetailActivity", "=== setupViews called ===");
        try {
            // Setup Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            android.util.Log.d("ProductDetailActivity", "Toolbar setup completed");

            // Initialize views
            ivMainImage = findViewById(R.id.ivMainImage);
            rvGalleryImages = findViewById(R.id.rvGalleryImages);
            tvProductName = findViewById(R.id.tvProductName);
            tvProductDescription = findViewById(R.id.tvProductDescription);
            tvProductPrice = findViewById(R.id.tvProductPrice);
            tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
            tvDiscountPercent = findViewById(R.id.tvDiscountPercent);
            tvSaleBadge = findViewById(R.id.tvSaleBadge);
            tvTotalPrice = findViewById(R.id.tvTotalPrice);
            tvProductCategory = findViewById(R.id.tvProductCategory);
            tvPreparationTime = findViewById(R.id.tvPreparationTime);
            tvAvailability = findViewById(R.id.tvAvailability);
            tvFeatured = findViewById(R.id.tvFeatured);
            btnAddToCart = findViewById(R.id.btnAddToCart);
            btnBuyNow = findViewById(R.id.btnBuyNow);
            rvProductOptions = findViewById(R.id.rvProductOptions);
            optionsCardView = findViewById(R.id.optionsCardView);
            android.util.Log.d("ProductDetailActivity", "rvProductOptions: " + (rvProductOptions != null ? "FOUND" : "NOT FOUND"));
            android.util.Log.d("ProductDetailActivity", "optionsCardView: " + (optionsCardView != null ? "FOUND" : "NOT FOUND"));
            android.util.Log.d("ProductDetailActivity", "All views initialized successfully");

            // Setup RecyclerView for gallery - Horizontal scroll
            rvGalleryImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            // Setup RecyclerView for product options - Vertical scroll
            rvProductOptions.setLayoutManager(new LinearLayoutManager(this));
            android.util.Log.d("ProductDetailActivity", "RecyclerView layouts set successfully");

            // Button click listeners
            btnAddToCart.setOnClickListener(v -> addToCart());
            btnBuyNow.setOnClickListener(v -> buyNow());
            android.util.Log.d("ProductDetailActivity", "Button click listeners set successfully");
        } catch (Exception e) {
            android.util.Log.e("ProductDetailActivity", "Error in setupViews: " + e.getMessage(), e);
            throw e;
        }
    }

    private void loadProductDetail() {
        android.util.Log.d("ProductDetailActivity", "=== loadProductDetail called with productId: " + productId + " ===");
        apiService.getProductById(productId).enqueue(new Callback<com.example.frontend.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.frontend.model.ApiResponse> call, Response<com.example.frontend.model.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.frontend.model.ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        try {
                            // Parse product data
                            product = com.example.frontend.util.JsonParser.parseProduct(apiResponse.getData());
                            if (product != null) {
                                // Debug log for sale fields
                                android.util.Log.d("ProductDetailActivity", "Product sale info:");
                                android.util.Log.d("ProductDetailActivity", "isOnSale: " + product.isOnSale());
                                android.util.Log.d("ProductDetailActivity", "salePrice: " + product.getSalePrice());
                                android.util.Log.d("ProductDetailActivity", "salePercentage: " + product.getSalePercentage());
                                android.util.Log.d("ProductDetailActivity", "saleStartDate: " + product.getSaleStartDate());
                                android.util.Log.d("ProductDetailActivity", "saleEndDate: " + product.getSaleEndDate());
                                android.util.Log.d("ProductDetailActivity", "isSaleActive(): " + product.isSaleActive());

                                displayProductDetail();
                                // Load options from product data instead of separate API call
                                loadProductOptionsFromProduct();
                            } else {
                                showError("Không thể parse dữ liệu sản phẩm");
                            }
                        } catch (Exception e) {
                            showError("Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    } else {
                        showError("Không thể tải chi tiết sản phẩm: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.frontend.model.ApiResponse> call, Throwable t) {
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void loadProductOptionsFromProduct() {
        android.util.Log.d("ProductDetailActivity", "Loading product options from product data");

        if (product != null && product.getOptions() != null && !product.getOptions().isEmpty()) {
            productOptions = product.getOptions();
            android.util.Log.d("ProductDetailActivity", "Found " + productOptions.size() + " options in product data");

            // Update UI with options
            updateOptionsUI();
        } else {
            android.util.Log.d("ProductDetailActivity", "No options found in product data");
            productOptions = new ArrayList<>();
            updateOptionsUI();
        }
    }

    private void updateOptionsUI() {
        android.util.Log.d("ProductDetailActivity", "Updating options UI with " + (productOptions != null ? productOptions.size() : 0) + " options");

        if (productOptions != null && !productOptions.isEmpty()) {
            // Show options section
            android.util.Log.d("ProductDetailActivity", "Setting optionsCardView visibility to VISIBLE");
            optionsCardView.setVisibility(View.VISIBLE);

            // Setup adapter
            if (productOptionsAdapter == null) {
                try {
                    android.util.Log.d("ProductDetailActivity", "Creating new ProductOptionsAdapter with " + productOptions.size() + " options");

                    // Create callback manually instead of method reference
                    ProductOptionsAdapter.OnOptionClickListener callback = new ProductOptionsAdapter.OnOptionClickListener() {
                        @Override
                        public void onOptionClick(ProductOption option, boolean isSelected) {
                            ProductDetailActivity.this.onOptionClick(option, isSelected);
                        }
                    };

                    android.util.Log.d("ProductDetailActivity", "Callback created successfully");
                    productOptionsAdapter = new ProductOptionsAdapter(productOptions, callback);
                    android.util.Log.d("ProductDetailActivity", "Adapter created successfully");

                    android.util.Log.d("ProductDetailActivity", "Checking rvProductOptions: " + (rvProductOptions != null ? "NOT NULL" : "NULL"));
                    if (rvProductOptions != null) {
                        android.util.Log.d("ProductDetailActivity", "Setting adapter to RecyclerView");
                        rvProductOptions.setAdapter(productOptionsAdapter);
                        android.util.Log.d("ProductDetailActivity", "Adapter set successfully");
                    } else {
                        android.util.Log.e("ProductDetailActivity", "rvProductOptions is NULL!");
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProductDetailActivity", "Error creating/setting adapter: " + e.getMessage(), e);
                }
            } else {
                try {
                    android.util.Log.d("ProductDetailActivity", "Updating existing ProductOptionsAdapter with " + productOptions.size() + " options");
                    productOptionsAdapter.updateOptions(productOptions);
                    android.util.Log.d("ProductDetailActivity", "Adapter updated successfully");
                } catch (Exception e) {
                    android.util.Log.e("ProductDetailActivity", "Error updating adapter: " + e.getMessage(), e);
                }
            }

            // Check RecyclerView state
            android.util.Log.d("ProductDetailActivity", "RecyclerView visibility: " + rvProductOptions.getVisibility());
            android.util.Log.d("ProductDetailActivity", "RecyclerView adapter: " + (rvProductOptions.getAdapter() != null ? "SET" : "NULL"));

            android.util.Log.d("ProductDetailActivity", "Options UI updated successfully");
        } else {
            // Hide options section
            android.util.Log.d("ProductDetailActivity", "Setting optionsCardView visibility to GONE");
            optionsCardView.setVisibility(View.GONE);
            android.util.Log.d("ProductDetailActivity", "Options section hidden - no options available");
        }
    }

    private void updateTotalPrice() {
        if (product == null) {
            android.util.Log.d("ProductDetailActivity", "updateTotalPrice: product is null");
            return;
        }

        // Use current price (sale price if on sale, otherwise regular price)
        java.math.BigDecimal basePrice = product.getCurrentPrice();
        android.util.Log.d("ProductDetailActivity", "updateTotalPrice: basePrice = " + basePrice + " (isOnSale: " + product.isOnSale() + ")");

        java.math.BigDecimal totalPrice = basePrice;
        android.util.Log.d("ProductDetailActivity", "updateTotalPrice: initial totalPrice = " + totalPrice);

        if (productOptionsAdapter != null) {
            List<ProductOption> selectedOptions = productOptionsAdapter.getSelectedOptions();
            android.util.Log.d("ProductDetailActivity", "updateTotalPrice: selectedOptions count = " + selectedOptions.size());

            for (ProductOption option : selectedOptions) {
                android.util.Log.d("ProductDetailActivity", "updateTotalPrice: processing option = " + option.getOptionName() + ", price = " + option.getPrice());
                if (option.getPrice() != null) {
                    totalPrice = totalPrice.add(option.getPrice());
                    android.util.Log.d("ProductDetailActivity", "updateTotalPrice: added " + option.getPrice() + ", new total = " + totalPrice);
                }
            }
        } else {
            android.util.Log.d("ProductDetailActivity", "updateTotalPrice: productOptionsAdapter is null");
        }

        android.util.Log.d("ProductDetailActivity", "updateTotalPrice: final totalPrice = " + totalPrice);
        tvTotalPrice.setText(formatPrice(totalPrice));

        // Show price breakdown if there are selected options
        if (productOptionsAdapter != null && !productOptionsAdapter.getSelectedOptions().isEmpty()) {
            tvProductPrice.setTextColor(getResources().getColor(R.color.primary_color));
        } else {
            tvProductPrice.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) {
            android.util.Log.d("ProductDetailActivity", "formatPrice: price is null");
            return "0đ";
        }

        String formatted = String.format("%,.0fđ", price.floatValue());
        android.util.Log.d("ProductDetailActivity", "formatPrice: " + price + " -> " + formatted);
        return formatted;
    }

    private void displayProductDetail() {
        if (product == null) return;

        android.util.Log.d("ProductDetailActivity", "Displaying product: " + product.getName());
        android.util.Log.d("ProductDetailActivity", "Product imageUrl: " + product.getImageUrl());
        android.util.Log.d("ProductDetailActivity", "Product galleryUrls: " + product.getGalleryUrls());

        // Set basic info
        tvProductName.setText(product.getName());
        tvProductDescription.setText(product.getDescription());

        // Display price with sale information
        if (product.isOnSale() && product.isSaleActive()) {
            // Show sale badge
            tvSaleBadge.setVisibility(View.VISIBLE);

            // Show original price with strikethrough
            tvOriginalPrice.setText(product.getFormattedOriginalPrice());
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

            // Show current sale price
            tvProductPrice.setText(product.getFormattedCurrentPrice());
            tvProductPrice.setTextColor(getResources().getColor(R.color.sale_color));

            // Show discount percentage
            String discountPercent = "-" + product.getDiscountPercentage() + "%";
            tvDiscountPercent.setText(discountPercent);
            tvDiscountPercent.setVisibility(View.VISIBLE);

        } else {
            // Hide sale elements
            tvSaleBadge.setVisibility(View.GONE);
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountPercent.setVisibility(View.GONE);

            // Show normal price
            tvProductPrice.setText(product.getFormattedPrice());
            tvProductPrice.setTextColor(getResources().getColor(R.color.primary_color));
        }

        tvTotalPrice.setText(product.getFormattedCurrentPrice()); // Use current price (sale or original)
        tvPreparationTime.setText(product.getPreparationTime() + " phút");

        // Category
        if (product.getCategory() != null) {
            tvProductCategory.setText(product.getCategory().getCategoryName());
            tvProductCategory.setVisibility(View.VISIBLE);
        } else {
            tvProductCategory.setVisibility(View.GONE);
        }

        // Availability
        if (product.getIsAvailable() != null && product.getIsAvailable()) {
            tvAvailability.setText("Có sẵn");
            tvAvailability.setTextColor(getResources().getColor(R.color.success_color));
            btnAddToCart.setEnabled(true);
        } else {
            tvAvailability.setText("Hết hàng");
            tvAvailability.setTextColor(getResources().getColor(R.color.error_color));
            btnAddToCart.setEnabled(false);
        }

        // Featured
        if (product.getIsFeatured() != null && product.getIsFeatured()) {
            tvFeatured.setText("Sản phẩm nổi bật");
            tvFeatured.setVisibility(View.VISIBLE);
        } else {
            tvFeatured.setVisibility(View.GONE);
        }

        // Load main image
        String mainImageUrl = product.getImageUrl();
        android.util.Log.d("ProductDetailActivity", "Main image URL: " + mainImageUrl);

        if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
            String fullImageUrl = ImageUrlBuilder.buildFullUrl(mainImageUrl);
            android.util.Log.d("ProductDetailActivity", "Full main image URL: " + fullImageUrl);
            android.util.Log.d("ProductDetailActivity", "Loading main image with Picasso...");

            Picasso.get()
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .resize(400, 300) // Smaller size to reduce load time
                    .centerCrop()
                    .into(ivMainImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            android.util.Log.d("ProductDetailActivity", "Main image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            android.util.Log.e("ProductDetailActivity", "Failed to load main image: " + e.getMessage());
                            // Retry with smaller size
                            android.util.Log.d("ProductDetailActivity", "Retrying with smaller size...");
                            Picasso.get()
                                    .load(fullImageUrl)
                                    .placeholder(R.drawable.ic_food_placeholder)
                                    .error(R.drawable.ic_food_placeholder)
                                    .resize(200, 150)
                                    .centerCrop()
                                    .into(ivMainImage);
                        }
                    });
        } else {
            android.util.Log.d("ProductDetailActivity", "No main image URL, using placeholder");
            Picasso.get()
                    .load(R.drawable.ic_food_placeholder)
                    .into(ivMainImage);
        }

        // Setup gallery images
        setupGalleryImages();
    }

    private void setupGalleryImages() {
        android.util.Log.d("ProductDetailActivity", "setupGalleryImages called");
        android.util.Log.d("ProductDetailActivity", "Gallery URLs: " + product.getGalleryUrls());

        if (product.getGalleryUrls() != null && !product.getGalleryUrls().isEmpty()) {
            android.util.Log.d("ProductDetailActivity", "Gallery URLs size: " + product.getGalleryUrls().size());

            // Fix double array brackets issue
            List<String> cleanGalleryUrls = new ArrayList<>();
            for (String url : product.getGalleryUrls()) {
                if (url != null && !url.isEmpty()) {
                    // Remove extra brackets if present
                    String cleanUrl = url.replaceAll("^\\[|\\]$", "");
                    cleanUrl = cleanUrl.replaceAll("^\"|\"$", "");
                    cleanGalleryUrls.add(cleanUrl);
                    android.util.Log.d("ProductDetailActivity", "Cleaned URL: " + cleanUrl);
                }
            }

            if (!cleanGalleryUrls.isEmpty()) {
                // Create adapter for gallery images with click listener for fullscreen
                GalleryImageAdapter adapter = new GalleryImageAdapter(cleanGalleryUrls, this::onGalleryImageClick);
                rvGalleryImages.setAdapter(adapter);
                rvGalleryImages.setVisibility(View.VISIBLE);
                android.util.Log.d("ProductDetailActivity", "Gallery RecyclerView visibility set to VISIBLE");
            } else {
                android.util.Log.d("ProductDetailActivity", "No valid gallery URLs after cleaning, hiding RecyclerView");
                rvGalleryImages.setVisibility(View.GONE);
            }
        } else {
            android.util.Log.d("ProductDetailActivity", "No gallery URLs, hiding RecyclerView");
            rvGalleryImages.setVisibility(View.GONE);
        }
    }

    private void onGalleryImageClick(String imageUrl) {
        // Open fullscreen image viewer
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        startActivity(intent);
    }

    private void addToCart() {
        if (product == null) {
            Toast.makeText(this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected options
        List<ProductOption> selectedOptions = new ArrayList<>();
        if (productOptionsAdapter != null) {
            selectedOptions = productOptionsAdapter.getSelectedOptions();
        }

        // Add to cart
        try {
            cartManager.addToCart(product, 1, selectedOptions);
            Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow() {
        if (product == null) {
            Toast.makeText(this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to cart first
        addToCart();

        // Then navigate to CheckoutActivity
        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    private void applyAnimations() {
        // Load animation
        Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        // Apply animations with delays
        ivMainImage.startAnimation(slideInBottom);

        Animation slideInInfo = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideInInfo.setStartOffset(100);
        tvProductName.startAnimation(slideInInfo);

        Animation slideInDesc = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideInDesc.setStartOffset(200);
        tvProductDescription.startAnimation(slideInDesc);

        Animation slideInButtons = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideInButtons.setStartOffset(300);
        btnAddToCart.startAnimation(slideInButtons);
        btnBuyNow.startAnimation(slideInButtons);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Callback for ProductOptionsAdapter
    public void onOptionClick(ProductOption option, boolean isSelected) {
        android.util.Log.d("ProductDetailActivity", "onOptionClick: " + option.getOptionName() + " selected: " + isSelected);
        updateTotalPrice();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
