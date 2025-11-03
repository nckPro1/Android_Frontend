package com.example.frontend.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
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
import com.example.frontend.ui.notification.NotificationListActivity;
import com.example.frontend.util.FCMTokenManager;
import com.example.frontend.util.JsonParser;
import com.example.frontend.util.NotificationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TokenManager tokenManager;
    private ApiService apiService;
    private FCMTokenManager fcmTokenManager;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories;

    private RecyclerView rvPopularDishes;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private SearchView searchView;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    // Notification widgets
    private TextView badgeNotificationCount;
    private ImageView ivNotifications;
    private NotificationManager notificationManager;
    private Long lastShownNotificationId = null; // Track last shown notification
    private Handler notificationPollHandler = new Handler(Looper.getMainLooper());
    private Runnable notificationPollRunnable;
    private static final long NOTIFICATION_POLL_INTERVAL = 30000; // 30 seconds
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

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

        // Khởi tạo FCM Token Manager và gửi token lên server nếu chưa gửi
        fcmTokenManager = new FCMTokenManager(this);
        fcmTokenManager.getAndSendToken();

        setupViews();
        loadUserInfo();
        loadCategories(); // Load categories
        loadPopularProducts(); // Load popular products
        requestNotificationPermission(); // Request notification permission
        setupNotifications(); // Setup notification widget
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
        searchView = findViewById(R.id.search_view);
        badgeNotificationCount = findViewById(R.id.badgeNotificationCount);
        ivNotifications = findViewById(R.id.ivNotifications);

        // Debug: Check if views are found
        if (badgeNotificationCount == null) {
            android.util.Log.e("HomeActivity", "badgeNotificationCount not found in layout!");
        } else {
            android.util.Log.d("HomeActivity", "badgeNotificationCount found successfully");
        }
        if (ivNotifications == null) {
            android.util.Log.e("HomeActivity", "ivNotifications not found in layout!");
        } else {
            android.util.Log.d("HomeActivity", "ivNotifications found successfully");
        }

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Initialize categories list and adapter
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categories, this::onCategoryClick);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Initialize products list and adapter (Popular Dishes - horizontal)
        products = new ArrayList<>();
        productAdapter = new ProductAdapter(products, null); // Không cần listener vì ProductAdapter tự xử lý
        rvPopularDishes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularDishes.setHasFixedSize(true);
        rvPopularDishes.setAdapter(productAdapter);

        // Search wiring -> open SearchResultsActivity on submit
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    openSearchResults(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // optional: could show suggestions here; do nothing
                    return true;
                }
            });
        }

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
                startActivity(new Intent(HomeActivity.this, com.example.frontend.ui.cart.CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, com.example.frontend.ui.profile.ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_about) {
                Toast.makeText(this, "About - Coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void openSearchResults(String rawQuery) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.isEmpty()) return;
        Intent intent = new Intent(this, com.example.frontend.ui.search.SearchResultsActivity.class);
        intent.putExtra("query", q);
        startActivity(intent);
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

    private void setupNotifications() {
        notificationManager = NotificationManager.getInstance(this);

        // Setup click listener for notification icon
        if (ivNotifications != null) {
            ivNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, NotificationListActivity.class);
                startActivity(intent);
            });
        }

        // Setup notification update listener
        notificationManager.setListener(new NotificationManager.NotificationUpdateListener() {
            @Override
            public void onNotificationsUpdated(List<com.example.frontend.model.Notification> notifications) {
                // Check for new notifications and show popup
                if (notifications != null && !notifications.isEmpty()) {
                    // Get the latest notification (first one should be the newest)
                    com.example.frontend.model.Notification latestNotification = notifications.get(0);
                    if (latestNotification != null &&
                            !latestNotification.isRead() &&
                            (lastShownNotificationId == null || !latestNotification.getNotificationId().equals(lastShownNotificationId))) {
                        // This is a new notification we haven't shown yet
                        lastShownNotificationId = latestNotification.getNotificationId();
                        runOnUiThread(() -> showNotificationPopup(latestNotification));
                    }
                }
            }

            @Override
            public void onUnreadCountUpdated(long count) {
                runOnUiThread(() -> {
                    updateNotificationBadge(count);
                    android.util.Log.d("HomeActivity", "Unread count updated: " + count);
                });
            }
        });

        // Load unread count immediately
        if (tokenManager.isLoggedIn()) {
            notificationManager.fetchUnreadCount();
            startNotificationPolling();
        }
    }

    private void updateNotificationBadge(long count) {
        android.util.Log.d("HomeActivity", "updateNotificationBadge called with count: " + count);
        if (badgeNotificationCount == null) {
            android.util.Log.e("HomeActivity", "badgeNotificationCount is null!");
            return;
        }

        if (count > 0) {
            badgeNotificationCount.setVisibility(View.VISIBLE);
            if (count > 99) {
                badgeNotificationCount.setText("99+");
            } else {
                badgeNotificationCount.setText(String.valueOf(count));
            }
            android.util.Log.d("HomeActivity", "Badge shown with text: " + badgeNotificationCount.getText());
            // Animate badge appearance
            badgeNotificationCount.setScaleX(0f);
            badgeNotificationCount.setScaleY(0f);
            badgeNotificationCount.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        } else {
            badgeNotificationCount.setVisibility(View.GONE);
            android.util.Log.d("HomeActivity", "Badge hidden");
        }
    }

    /**
     * Request notification permission for Android 13+ (API 33+)
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Show explanation dialog if needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Quyền thông báo")
                            .setMessage("Ứng dụng cần quyền thông báo để gửi thông báo về đơn hàng và tin nhắn. Bạn có muốn cấp quyền này không?")
                            .setPositiveButton("Đồng ý", (dialog, which) -> {
                                ActivityCompat.requestPermissions(
                                        HomeActivity.this,
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        NOTIFICATION_PERMISSION_REQUEST_CODE
                                );
                            })
                            .setNegativeButton("Không", (dialog, which) -> {
                                Toast.makeText(this, "Bạn có thể bật quyền thông báo trong Cài đặt sau", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                } else {
                    // Request permission directly
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                    );
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối. Bạn có thể bật lại trong Cài đặt", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showNotificationPopup(com.example.frontend.model.Notification notification) {
        // Hiển thị thông báo đơn giản
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Có thông báo mới",
                Snackbar.LENGTH_SHORT
        );
        snackbar.show();
    }

    private void startNotificationPolling() {
        // Stop existing polling if any
        stopNotificationPolling();

        // Start polling for new notifications
        notificationPollRunnable = new Runnable() {
            @Override
            public void run() {
                if (tokenManager.isLoggedIn() && notificationManager != null) {
                    // Fetch unread notifications to check for new ones
                    notificationManager.fetchUnreadNotifications();
                    // Also update badge count
                    notificationManager.fetchUnreadCount();
                }
                // Schedule next poll
                notificationPollHandler.postDelayed(this, NOTIFICATION_POLL_INTERVAL);
            }
        };
        notificationPollHandler.postDelayed(notificationPollRunnable, NOTIFICATION_POLL_INTERVAL);
    }

    private void stopNotificationPolling() {
        if (notificationPollRunnable != null) {
            notificationPollHandler.removeCallbacks(notificationPollRunnable);
            notificationPollRunnable = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notification count when returning to home
        if (tokenManager.isLoggedIn() && notificationManager != null) {
            notificationManager.fetchUnreadCount();
            // Also fetch notifications to check for new ones
            notificationManager.fetchUnreadNotifications();
            // Restart polling
            startNotificationPolling();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling when app goes to background
        stopNotificationPolling();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up polling
        stopNotificationPolling();
    }
}