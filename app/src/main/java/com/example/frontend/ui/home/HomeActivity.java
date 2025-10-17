package com.example.frontend.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TokenManager tokenManager;

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
        applyAnimations(); // Gọi hàm để chạy animation
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Tắt tiêu đề mặc định của Toolbar để dùng TextView tùy chỉnh
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        tvWelcome = findViewById(R.id.tvWelcome);

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Đã ở màn hình Home
                return true;
            } else if (itemId == R.id.nav_categories) {
                Toast.makeText(this, "Categories - Coming soon!", Toast.LENGTH_SHORT).show();
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
}