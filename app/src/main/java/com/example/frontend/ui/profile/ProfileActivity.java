package com.example.frontend.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ProfileMenuItem;
import com.example.frontend.model.UserDto;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.adapter.ProfileMenuAdapter;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.ui.home.HomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;
    private ImageView imgAvatar;
    private TextView tvUserName, tvUserEmail;
    private RecyclerView rvProfileMenu;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        setupViews();
        setupBottomNavigation();
        setupRecyclerView();
        loadUserProfile();
    }

    private void setupViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        rvProfileMenu = findViewById(R.id.rvProfileMenu);
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupRecyclerView() {
        rvProfileMenu.setLayoutManager(new LinearLayoutManager(this));
        List<ProfileMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new ProfileMenuItem("SETTINGS", "Settings", R.drawable.ic_settings));
        menuItems.add(new ProfileMenuItem("ORDERS", "My Orders", R.drawable.ic_orders));
        menuItems.add(new ProfileMenuItem("FAVORITES", "Favorites", R.drawable.ic_favorites));
        menuItems.add(new ProfileMenuItem("CHANGE_PASSWORD", "Change Password", R.drawable.ic_settings));
        menuItems.add(new ProfileMenuItem("PAYMENT", "Payment Methods", R.drawable.ic_payment));
        menuItems.add(new ProfileMenuItem("ADDRESS", "Delivery Address", R.drawable.ic_address));
        ProfileMenuAdapter adapter = new ProfileMenuAdapter(menuItems, this::handleMenuClick);
        rvProfileMenu.setAdapter(adapter);
    }

    private void handleMenuClick(ProfileMenuItem item) {
        if ("SETTINGS".equals(item.getId())) {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        } else if ("CHANGE_PASSWORD".equals(item.getId())) {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, item.getLabel() + " - Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        tvUserName.setText(tokenManager.getUserName());
        tvUserEmail.setText(tokenManager.getUserEmail());
        Call<UserDto> call = apiService.getUserProfile("Bearer " + tokenManager.getAccessToken());
        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayUserInfo(response.body());
                }
            }
            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {}
        });
    }

    private void displayUserInfo(UserDto user) {
        tvUserName.setText(user.getFullName());
        tvUserEmail.setText(user.getEmail());
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl.startsWith("/")) {
                avatarUrl = ApiClient.BASE_URL + avatarUrl;
            }
            Picasso.get().load(avatarUrl)
                    .transform(new com.squareup.picasso.CircleTransform())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(imgAvatar);
        }
    }

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