package com.example.frontend.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.Category;
import com.example.frontend.model.Product;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.adapter.CategoryAdapter;
import com.example.frontend.ui.adapter.ProductAdapter;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.ui.cart.CartActivity;
import com.example.frontend.ui.profile.ProfileActivity;
import com.example.frontend.util.JsonParser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TokenManager tokenManager;
    private ApiService apiService;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories;

    private RecyclerView rvPopularDishes;
    private ProductAdapter productAdapter;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);

        // Check if user is logged in
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return; // Quan trọng: dừng thực thi nếu chưa đăng nhập
        }

        setupViews();
        loadUserInfo();
        loadCategories(); // Load categories
        loadPopularProducts(); // Load popular products
        applyAnimations(); // Gọi hàm để chạy animation

        // Check if should show categories fragment
        if (getIntent().getBooleanExtra("show_categories", false)) {
            CategoriesFragment categoriesFragment = new CategoriesFragment();
            showFragmentFromChild(categoriesFragment);
        }
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Tắt tiêu đề mặc định của Toolbar để dùng TextView tùy chỉnh
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        rvCategories = findViewById(R.id.rv_categories);
        TextView tvSeeAllCategories = findViewById(R.id.tvSeeAllCategories);
        TextView tvSeeAllProducts = findViewById(R.id.tvSeeAllProducts);
        rvPopularDishes = findViewById(R.id.rv_popular_dishes);

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Initialize categories list and adapter
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categories, this::onCategoryClick);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Initialize products list and adapter
        products = new ArrayList<>();
        productAdapter = new ProductAdapter(products, null); // Không cần listener vì ProductAdapter tự xử lý
        rvPopularDishes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPopularDishes.setAdapter(productAdapter);

        // Setup See All click listeners
        tvSeeAllCategories.setOnClickListener(v -> {
            CategoriesFragment categoriesFragment = new CategoriesFragment();
            showFragmentFromChild(categoriesFragment);
        });

        tvSeeAllProducts.setOnClickListener(v -> {
            ProductsFragment productsFragment = new ProductsFragment();
            showFragmentFromChild(productsFragment);
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Show home content, hide fragment
                showHomeContent();
                return true;
            } else if (itemId == R.id.nav_categories) {
                // Load Categories Fragment
                showFragment(new CategoriesFragment());
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Navigate to Cart Activity
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_about) {
                Toast.makeText(this, "About - Coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void loadUserInfo() {
        String userName = tokenManager.getUserName();
        // Cập nhật TextView theo thiết kế mới
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Hi, " + userName);
        } else {
            tvWelcome.setText("Welcome back");
        }
    }

    private void loadCategories() {
        apiService.getAllCategories().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        try {
                            // Parse data từ Object về List<Category>
                            List<Category> categoryList = JsonParser.parseCategories(apiResponse.getData());
                            categories.clear();
                            categories.addAll(categoryList);
                            categoryAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            showError("Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    } else {
                        showError("Không thể tải danh mục: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void loadPopularProducts() {
        // Thử getFeaturedProducts trước, nếu không có thì dùng getAllProducts
        apiService.getFeaturedProducts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        try {
                            // Parse data từ Object về List<Product>
                            List<Product> productList = JsonParser.parseProducts(apiResponse.getData());
                            products.clear();
                            products.addAll(productList);
                            productAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            showError("Lỗi parse dữ liệu sản phẩm: " + e.getMessage());
                        }
                    } else {
                        showError("Không thể tải sản phẩm: " + apiResponse.getMessage());
                        // Fallback: thử getAllProducts nếu getFeaturedProducts không có data
                        loadAllProductsFallback();
                    }
                } else {
                    showError("Lỗi kết nối sản phẩm: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showError("Lỗi mạng sản phẩm: " + t.getMessage());
            }
        });
    }

    private void loadAllProductsFallback() {
        apiService.getAllProducts(0, 10, "createdAt", "desc").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        try {
                            // Parse data từ Object về List<Product>
                            List<Product> productList = JsonParser.parseProducts(apiResponse.getData());
                            products.clear();
                            products.addAll(productList);
                            productAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            showError("Lỗi parse dữ liệu sản phẩm fallback: " + e.getMessage());
                        }
                    } else {
                        showError("Không thể tải sản phẩm fallback: " + apiResponse.getMessage());
                    }
                } else {
                    showError("Lỗi kết nối sản phẩm fallback: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showError("Lỗi mạng sản phẩm fallback: " + t.getMessage());
            }
        });
    }

    private void onCategoryClick(Category category) {
        // Navigate to products by category
        ProductsFragment productsFragment = ProductsFragment.newInstance(category.getCategoryId(), category.getCategoryName());
        showFragmentFromChild(productsFragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showHomeContent() {
        findViewById(R.id.home_content).setVisibility(View.VISIBLE);
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
    }

    private void showFragment(Fragment fragment) {
        findViewById(R.id.home_content).setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        loadFragment(fragment);
    }

    // Public method để Fragment có thể gọi
    public void showFragmentFromChild(Fragment fragment) {
        showFragment(fragment);
    }

    private void applyAnimations() {
        // Ánh xạ các view cần animation
        MaterialCardView searchCard = findViewById(R.id.search_card);
        MaterialCardView specialOfferCard = findViewById(R.id.card_special_offer);
        RecyclerView rvCategories = findViewById(R.id.rv_categories);
        RecyclerView rvPopularDishes = findViewById(R.id.rv_popular_dishes);

        // Tải animation
        Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        // Thiết lập độ trễ cho từng animation để tạo hiệu ứng nối tiếp
        searchCard.startAnimation(slideInBottom);

        Animation slideInOffer = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideInOffer.setStartOffset(100);
        specialOfferCard.startAnimation(slideInOffer);

        Animation slideInCategories = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideInCategories.setStartOffset(200);
        rvCategories.startAnimation(slideInCategories);

        Animation slideInDishes = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        slideInDishes.setStartOffset(300);
        rvPopularDishes.startAnimation(slideInDishes);
    }

    // Các hàm logout và goToLogin giữ nguyên như code cũ của bạn
    // (Lưu ý: Bạn cần tạo file menu res/menu/home_menu.xml cho nút logout)
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */
    private void logout() {
        tokenManager.clearTokens();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}