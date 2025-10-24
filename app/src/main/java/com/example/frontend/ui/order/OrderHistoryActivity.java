package com.example.frontend.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.Order;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.util.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private TextView textViewTitle;
    private RecyclerView recyclerViewOrders;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private TokenManager tokenManager;
    private ApiService apiService;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        
        Log.d("OrderHistoryActivity", "onCreate called - Activity started successfully");

        // Check authentication
        tokenManager = new TokenManager(this);
        Log.d("OrderHistoryActivity", "TokenManager created");
        
        if (!tokenManager.isLoggedIn()) {
            Log.e("OrderHistoryActivity", "User not logged in, redirecting to login");
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        Log.d("OrderHistoryActivity", "User is logged in, proceeding with initialization");

        try {
            initViews();
            Log.d("OrderHistoryActivity", "initViews completed");
            
            initData();
            Log.d("OrderHistoryActivity", "initData completed");
            
            setupListeners();
            Log.d("OrderHistoryActivity", "setupListeners completed");
            
            loadOrderHistory();
            Log.d("OrderHistoryActivity", "loadOrderHistory completed");
        } catch (Exception e) {
            Log.e("OrderHistoryActivity", "Error in initialization: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        Log.d("OrderHistoryActivity", "initViews started");
        
        imageViewBack = findViewById(R.id.imageViewBack);
        Log.d("OrderHistoryActivity", "imageViewBack: " + (imageViewBack != null ? "found" : "null"));
        
        textViewTitle = findViewById(R.id.textViewTitle);
        Log.d("OrderHistoryActivity", "textViewTitle: " + (textViewTitle != null ? "found" : "null"));
        
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        Log.d("OrderHistoryActivity", "recyclerViewOrders: " + (recyclerViewOrders != null ? "found" : "null"));
        
        progressBar = findViewById(R.id.progressBar);
        Log.d("OrderHistoryActivity", "progressBar: " + (progressBar != null ? "found" : "null"));
        
        layoutEmpty = findViewById(R.id.layoutEmpty);
        Log.d("OrderHistoryActivity", "layoutEmpty: " + (layoutEmpty != null ? "found" : "null"));
        
        Log.d("OrderHistoryActivity", "initViews completed");
    }

    private void initData() {
        apiService = ApiClient.getApiService();
        
        // Setup RecyclerView
        orderHistoryAdapter = new OrderHistoryAdapter(orders, this::onOrderClick);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderHistoryAdapter);
    }

    private void setupListeners() {
        imageViewBack.setOnClickListener(v -> finish());
    }

    private void loadOrderHistory() {
        Log.d("OrderHistoryActivity", "loadOrderHistory called");
        
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        
        String token = tokenManager.getAccessToken();
        Log.d("OrderHistoryActivity", "Token: " + (token != null ? "Present" : "Null"));
        Log.d("OrderHistoryActivity", "Token length: " + (token != null ? token.length() : "null"));
        Log.d("OrderHistoryActivity", "Token preview: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        
        if (token == null || token.trim().isEmpty()) {
            Log.e("OrderHistoryActivity", "Token is null or empty, redirecting to login");
            Toast.makeText(this, "Token không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            // Don't clear tokens here, let LoginActivity handle it
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Get user ID from token or user info
        Long userId = getUserIdFromToken();
        Log.d("OrderHistoryActivity", "UserId: " + userId);
        
        if (userId == null) {
            Log.e("OrderHistoryActivity", "UserId is null, finishing activity");
            Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("OrderHistoryActivity", "Loading order history for userId: " + userId);
        
        apiService.getOrdersByUserId("Bearer " + token, userId).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                progressBar.setVisibility(View.GONE);
                
                Log.d("OrderHistoryActivity", "API Response code: " + response.code());
                Log.d("OrderHistoryActivity", "API Response body: " + (response.body() != null ? response.body().toString() : "null"));
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Order> orderList = response.body().getData();
                    Log.d("OrderHistoryActivity", "Orders received: " + (orderList != null ? orderList.size() : "null"));
                    
                    if (orderList != null && !orderList.isEmpty()) {
                        orders.clear();
                        orders.addAll(orderList);
                        orderHistoryAdapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerViewOrders.setVisibility(View.VISIBLE);
                        Log.d("OrderHistoryActivity", "Orders displayed successfully");
                    } else {
                        Log.d("OrderHistoryActivity", "No orders found, showing empty state");
                        showEmptyState();
                    }
                } else {
                    String errorMessage = "Không thể tải lịch sử đơn hàng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e("OrderHistoryActivity", "API Error: " + errorMessage);
                    Toast.makeText(OrderHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("OrderHistoryActivity", "API call failed: " + t.getMessage(), t);
                Toast.makeText(OrderHistoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        orders.clear();
        orderHistoryAdapter.notifyDataSetChanged();
        recyclerViewOrders.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private Long getUserIdFromToken() {
        // Try to get user ID from TokenManager first
        try {
            long userId = tokenManager.getUserId();
            String userEmail = tokenManager.getUserEmail();
            
            Log.d("OrderHistoryActivity", "User ID from TokenManager: " + userId);
            Log.d("OrderHistoryActivity", "User email from TokenManager: " + userEmail);
            
            if (userId > 0) {
                return userId;
            }
        } catch (Exception e) {
            Log.e("OrderHistoryActivity", "Error getting user ID from TokenManager: " + e.getMessage(), e);
        }
        
        // Fallback to default user ID
        Log.w("OrderHistoryActivity", "Could not get user ID, using default userId: 1");
        return 1L;
    }

    private void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        startActivity(intent);
    }
}
