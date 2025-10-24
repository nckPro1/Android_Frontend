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
    private TextView tvTotalPrice;
    private TextView tvProductCategory;
    private TextView tvPreparationTime;
    private TextView tvAvailability;
    private TextView tvFeatured;
    private Button btnAddToCart;
    private Button btnAddToFavorites;
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
            tvTotalPrice = findViewById(R.id.tvTotalPrice);
            tvProductCategory = findViewById(R.id.tvProductCategory);
            tvPreparationTime = findViewById(R.id.tvPreparationTime);
            tvAvailability = findViewById(R.id.tvAvailability);
            tvFeatured = findViewById(R.id.tvFeatured);
            btnAddToCart = findViewById(R.id.btnAddToCart);
            btnAddToFavorites = findViewById(R.id.btnAddToFavorites);
            rvProductOptions = findViewById(R.id.rvProductOptions);
            optionsCardView = findViewById(R.id.optionsCardView);
            android.util.Log.d("ProductDetailActivity", "All views initialized successfully");

            // Setup RecyclerView for gallery - Horizontal scroll
            rvGalleryImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            // Setup RecyclerView for product options - Vertical scroll
            rvProductOptions.setLayoutManager(new LinearLayoutManager(this));
            android.util.Log.d("ProductDetailActivity", "RecyclerView layouts set successfully");

            // Button click listeners
            btnAddToCart.setOnClickListener(v -> addToCart());
            btnAddToFavorites.setOnClickListener(v -> addToFavorites());
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
                                displayProductDetail();
                                // Load options after product is loaded successfully
                                loadProductOptions();
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

    private void loadProductOptions() {
        if (productId == null || productId == -1) {
            android.util.Log.d("ProductDetailActivity", "No valid productId, skipping options load");
            return;
        }

        android.util.Log.d("ProductDetailActivity", "Loading product options for productId: " + productId);

        try {
            apiService.getProductOptions(productId).enqueue(new Callback<com.example.frontend.model.ApiResponse>() {
                @Override
                public void onResponse(Call<com.example.frontend.model.ApiResponse> call, Response<com.example.frontend.model.ApiResponse> response) {
                    android.util.Log.d("ProductDetailActivity", "Options API response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        com.example.frontend.model.ApiResponse apiResponse = response.body();
                        android.util.Log.d("ProductDetailActivity", "Options API success: " + apiResponse.isSuccess());

                        if (apiResponse.isSuccess()) {
                            try {
                                // Log raw data first
                                android.util.Log.d("ProductDetailActivity", "Raw options data: " + apiResponse.getData());

                                List<ProductOption> options = JsonParser.parseProductOptions(apiResponse.getData());
                                android.util.Log.d("ProductDetailActivity", "Parsed options count: " + (options != null ? options.size() : 0));

                                if (options != null && !options.isEmpty()) {
                                    // Log each option details
                                    for (int i = 0; i < options.size(); i++) {
                                        ProductOption option = options.get(i);
                                        android.util.Log.d("ProductDetailActivity", "Option " + i + ": " + option.toString());
                                    }

                                    productOptions = options;
                                    setupProductOptions();
                                } else {
                                    android.util.Log.d("ProductDetailActivity", "No options found for product");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ProductDetailActivity", "Error parsing options: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            android.util.Log.w("ProductDetailActivity", "Options API returned error: " + apiResponse.getMessage());
                        }
                    } else {
                        android.util.Log.w("ProductDetailActivity", "Options API failed with code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<com.example.frontend.model.ApiResponse> call, Throwable t) {
                    android.util.Log.e("ProductDetailActivity", "Error loading options: " + t.getMessage());
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ProductDetailActivity", "Exception in loadProductOptions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupProductOptions() {
        android.util.Log.d("ProductDetailActivity", "=== setupProductOptions called ===");
        android.util.Log.d("ProductDetailActivity", "productOptions size: " + (productOptions != null ? productOptions.size() : "null"));

        if (productOptions == null || productOptions.isEmpty()) {
            android.util.Log.d("ProductDetailActivity", "No options, hiding options card");
            optionsCardView.setVisibility(View.GONE);
            return;
        }

        android.util.Log.d("ProductDetailActivity", "Showing options card with " + productOptions.size() + " options");
        optionsCardView.setVisibility(View.VISIBLE);
        rvProductOptions.setVisibility(View.VISIBLE);

        ProductOptionsAdapter adapter = new ProductOptionsAdapter(productOptions, new ProductOptionsAdapter.OnOptionClickListener() {
            @Override
            public void onOptionClick(ProductOption option, boolean isSelected) {
                android.util.Log.d("ProductDetailActivity", "Option clicked: " + option.getOptionName() + " - Selected: " + isSelected);
                updateTotalPrice();
            }
        });
        rvProductOptions.setAdapter(adapter);
        productOptionsAdapter = adapter; // Store adapter reference
        android.util.Log.d("ProductDetailActivity", "ProductOptionsAdapter set successfully");
    }

    private void updateTotalPrice() {
        if (product == null) {
            android.util.Log.d("ProductDetailActivity", "updateTotalPrice: product is null");
            return;
        }

        java.math.BigDecimal basePrice = product.getPrice();
        android.util.Log.d("ProductDetailActivity", "updateTotalPrice: basePrice = " + basePrice);

        java.math.BigDecimal totalPrice = basePrice;
        android.util.Log.d("ProductDetailActivity", "updateTotalPrice: initial totalPrice = " + totalPrice);

        if (productOptionsAdapter != null) {
            List<ProductOption> selectedOptions = productOptionsAdapter.getSelectedOptions();
            android.util.Log.d("ProductDetailActivity", "updateTotalPrice: selectedOptions count = " + selectedOptions.size());

            for (ProductOption option : selectedOptions) {
                android.util.Log.d("ProductDetailActivity", "updateTotalPrice: processing option = " + option.getOptionName() + ", extraPrice = " + option.getExtraPrice());
                if (option.getExtraPrice() != null) {
                    totalPrice = totalPrice.add(option.getExtraPrice());
                    android.util.Log.d("ProductDetailActivity", "updateTotalPrice: added " + option.getExtraPrice() + ", new total = " + totalPrice);
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
        tvProductPrice.setText(product.getFormattedPrice());
        tvTotalPrice.setText(product.getFormattedPrice()); // Set initial total price
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

    private void addToFavorites() {
        Toast.makeText(this, "Đã thêm " + product.getName() + " vào yêu thích", Toast.LENGTH_SHORT).show();
        // TODO: Implement add to favorites functionality
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
        btnAddToFavorites.startAnimation(slideInButtons);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
