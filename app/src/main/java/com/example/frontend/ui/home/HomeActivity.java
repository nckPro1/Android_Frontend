package com.example.frontend.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.ui.profile.ProfileActivity;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvEmail;
    private TokenManager tokenManager;
    private MaterialCardView cardOrders, cardProfile, cardMenu, cardCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);

        // Check if user is logged in
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        setupViews();
        loadUserInfo();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmail = findViewById(R.id.tvEmail);

        cardOrders = findViewById(R.id.cardOrders);
        cardProfile = findViewById(R.id.cardProfile);
        cardMenu = findViewById(R.id.cardMenu);
        cardCart = findViewById(R.id.cardCart);

        cardOrders.setOnClickListener(v ->
                Toast.makeText(this, "Đơn hàng - Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        cardMenu.setOnClickListener(v ->
                Toast.makeText(this, "Thực đơn - Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        cardCart.setOnClickListener(v ->
                Toast.makeText(this, "Giỏ hàng - Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserInfo() {
        String userName = tokenManager.getUserName();
        String userEmail = tokenManager.getUserEmail();

        tvWelcome.setText("Xin chào, " + userName + "!");
        tvEmail.setText(userEmail);
    }

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

