package com.example.frontend.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.frontend.model.OrderItem;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.util.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private TextView textViewOrderNumber;
    private TextView textViewOrderStatus;
    private TextView textViewPaymentStatus;
    private TextView textViewPaymentMethod;
    private TextView textViewDeliveryAddress;
    private TextView textViewDeliveryNotes;
    private TextView textViewEstimatedDeliveryTime;
    private TextView textViewSubtotal;
    private TextView textViewShippingFee;
    private TextView textViewDiscount;
    private TextView textViewTotalAmount;
    private RecyclerView recyclerViewOrderItems;
    private Button buttonBackToHome;
    private Button buttonCancelOrder;
    private ProgressBar progressBar;

    private TokenManager tokenManager;
    private ApiService apiService;
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderItem> orderItems = new ArrayList<>();
    private Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Check authentication
        tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        initData();
        setupListeners();
        loadOrderDetail();
    }

    private void initViews() {
        imageViewBack = findViewById(R.id.imageViewBack);
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        textViewOrderStatus = findViewById(R.id.textViewOrderStatus);
        textViewPaymentStatus = findViewById(R.id.textViewPaymentStatus);
        textViewPaymentMethod = findViewById(R.id.textViewPaymentMethod);
        textViewDeliveryAddress = findViewById(R.id.textViewDeliveryAddress);
        textViewDeliveryNotes = findViewById(R.id.textViewDeliveryNotes);
        textViewEstimatedDeliveryTime = findViewById(R.id.textViewEstimatedDeliveryTime);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShippingFee = findViewById(R.id.textViewShippingFee);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
        buttonCancelOrder = findViewById(R.id.buttonCancelOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        apiService = ApiClient.getApiService();

        // Setup RecyclerView
        orderDetailAdapter = new OrderDetailAdapter(orderItems);
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrderItems.setAdapter(orderDetailAdapter);
    }

    private void setupListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.frontend.ui.home.HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        if (buttonCancelOrder != null) {
            buttonCancelOrder.setOnClickListener(v -> performCancelOrder());
        }
    }

    private void loadOrderDetail() {
        Intent intent = getIntent();
        orderId = intent.getLongExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String token = tokenManager.getAccessToken();
        if (token == null) {
            Toast.makeText(this, "Token không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            tokenManager.clearTokens();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        Log.d("OrderDetailActivity", "Loading order detail for orderId: " + orderId);

        apiService.getOrderById("Bearer " + token, orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                progressBar.setVisibility(View.GONE);

                Log.d("OrderDetailActivity", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Order order = response.body().getData();
                    Log.d("OrderDetailActivity", "Order received: " + order.getOrderNumber());
                    if (order.getOrderItems() != null) {
                        Log.d("OrderDetailActivity", "Order items count: " + order.getOrderItems().size());
                        for (int i = 0; i < order.getOrderItems().size(); i++) {
                            OrderItem item = order.getOrderItems().get(i);
                            Log.d("OrderDetailActivity", "Item " + i + ": name=" + item.getProductName()
                                    + ", price=" + item.getProductPrice()
                                    + ", image=" + item.getProductImage()
                                    + ", subtotal=" + item.getSubtotal()
                                    + ", quantity=" + item.getQuantity());
                        }
                    } else {
                        Log.w("OrderDetailActivity", "Order items is null!");
                    }
                    displayOrderDetail(order);
                } else {
                    String errorMessage = "Không thể tải chi tiết đơn hàng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Toast.makeText(OrderDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("OrderDetailActivity", "API call failed: " + t.getMessage(), t);
                Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayOrderDetail(Order order) {
        // Basic order info
        textViewOrderNumber.setText(order.getOrderNumber());
        textViewOrderStatus.setText(getOrderStatusDisplay(order.getOrderStatus()));
        textViewPaymentStatus.setText(getPaymentStatusDisplay(order.getPaymentStatus()));
        textViewPaymentMethod.setText(getPaymentMethodDisplay(order.getPaymentMethod()));

        // Delivery info
        textViewDeliveryAddress.setText(order.getDeliveryAddress());
        if (order.getDeliveryNotes() != null && !order.getDeliveryNotes().isEmpty()) {
            textViewDeliveryNotes.setText(order.getDeliveryNotes());
            textViewDeliveryNotes.setVisibility(View.VISIBLE);
        } else {
            textViewDeliveryNotes.setVisibility(View.GONE);
        }

        // Estimated delivery time
        if (order.getEstimatedDeliveryTime() != null) {
            textViewEstimatedDeliveryTime.setText("Dự kiến giao hàng: " +
                    order.getEstimatedDeliveryTime().toString().replace("T", " "));
        } else {
            textViewEstimatedDeliveryTime.setVisibility(View.GONE);
        }

        // Amounts
        textViewSubtotal.setText(PriceFormatter.format(order.getTotalAmount()));
        textViewShippingFee.setText(PriceFormatter.format(order.getShippingFee()));
        textViewDiscount.setText(PriceFormatter.format(order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO));
        textViewTotalAmount.setText(PriceFormatter.format(order.getFinalAmount()));

        // Toggle cancel button visibility
        if (buttonCancelOrder != null) {
            boolean canCancel = order != null && (order.getOrderStatus() == Order.OrderStatus.PENDING || order.getOrderStatus() == Order.OrderStatus.CONFIRMED);
            buttonCancelOrder.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        }

        // Order items
        if (order.getOrderItems() != null) {
            Log.d("OrderDetailActivity", "Displaying " + order.getOrderItems().size() + " order items");
            orderItems.clear();
            orderItems.addAll(order.getOrderItems());
            Log.d("OrderDetailActivity", "Adapter item count: " + orderDetailAdapter.getItemCount());
            orderDetailAdapter.notifyDataSetChanged();
        } else {
            Log.w("OrderDetailActivity", "Order items is null, cannot display");
        }
    }

    private void performCancelOrder() {
        if (orderId == null) return;
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        apiService.cancelOrder("Bearer " + token, orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(OrderDetailActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    // Reload details
                    loadOrderDetail();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Hủy đơn thất bại";
                    Toast.makeText(OrderDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, "Lỗi hủy đơn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getOrderStatusDisplay(Order.OrderStatus status) {
        if (status == null) return "Không xác định";

        switch (status) {
            case PENDING: return "Chờ xử lý";
            case CONFIRMED: return "Đã nhận";
            case DELIVERING: return "Đang giao";
            case DONE: return "Thanh toán thành công";
            default: return status.toString();
        }
    }

    private String getPaymentStatusDisplay(Order.PaymentStatus status) {
        if (status == null) return "Không xác định";

        switch (status) {
            case PENDING: return "Chờ thanh toán";
            case COMPLETED: return "Đã thanh toán";
            case FAILED: return "Thanh toán thất bại";
            case REFUNDED: return "Đã hoàn tiền";
            default: return status.toString();
        }
    }

    private String getPaymentMethodDisplay(Order.PaymentMethod method) {
        if (method == null) return "Không xác định";

        switch (method) {
            case CASH: return "Tiền mặt";
            case CARD: return "Thẻ";
            case BANK_TRANSFER: return "Chuyển khoản";
            case E_WALLET: return "Ví điện tử";
            default: return method.toString();
        }
    }
}
