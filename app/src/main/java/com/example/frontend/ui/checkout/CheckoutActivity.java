package com.example.frontend.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;

import com.example.frontend.ui.order.OrderSuccessActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.CartItem;
import com.example.frontend.model.Order;
import com.example.frontend.model.ShippingCalculationResponse;
import com.example.frontend.model.ShippingCalculationRequest;
import com.example.frontend.model.UserDto;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.cart.CartActivity;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.util.CartManager;
import com.example.frontend.util.PriceFormatter;
import com.example.frontend.local.TokenManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    // UI Components
    private ImageView imageViewBack;
    private RecyclerView recyclerViewCartItems;
    private CheckoutAdapter checkoutAdapter;
    private EditText textViewDeliveryAddress;
    private EditText textViewDeliveryNotes;
    private RadioGroup radioGroupPaymentMethod;
    private RadioButton radioButtonCash;
    private RadioButton radioButtonCard;
    private RadioButton radioButtonBankTransfer;
    private RadioButton radioButtonEWallet;
    private TextView textViewSubtotal;
    private TextView textViewShippingFee;
    private TextView textViewDiscount;
    private TextView textViewTotal;
    private Button buttonPlaceOrder;
    private ProgressBar progressBar;

    // Data
    private List<CartItem> cartItems;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;
    private BigDecimal total;
    private TokenManager tokenManager;
    private CartManager cartManager;
    private ApiService apiService;
    private UserDto currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Check authentication first
        tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        initData();
        setupListeners();
        loadData();
    }

    private void initViews() {
        imageViewBack = findViewById(R.id.imageViewBack);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        textViewDeliveryAddress = findViewById(R.id.textViewDeliveryAddress);
        textViewDeliveryNotes = findViewById(R.id.textViewDeliveryNotes);
        radioGroupPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        radioButtonCash = findViewById(R.id.radioButtonCash);
        radioButtonCard = findViewById(R.id.radioButtonCard);
        radioButtonBankTransfer = findViewById(R.id.radioButtonBankTransfer);
        radioButtonEWallet = findViewById(R.id.radioButtonEWallet);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShippingFee = findViewById(R.id.textViewShippingFee);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        cartManager = CartManager.getInstance(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        cartItems = new ArrayList<>();
        subtotal = BigDecimal.ZERO;
        shippingFee = BigDecimal.ZERO;
        discount = BigDecimal.ZERO;
        total = BigDecimal.ZERO;
    }

    private void setupListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        // Set default payment method
        radioButtonCash.setChecked(true);

        // Add text watcher for delivery address to recalculate shipping
        textViewDeliveryAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Recalculate shipping fee when address changes
                if (s.length() > 10) { // Only calculate if address is meaningful
                    calculateShippingFee();
                }
            }
        });

        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadData() {
        // Load cart items
        cartItems = cartManager.getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load user profile to get address
        loadUserProfile();

        // Setup recycler view
        checkoutAdapter = new CheckoutAdapter(cartItems);
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCartItems.setAdapter(checkoutAdapter);

        // Calculate totals
        calculateSubtotal();
        updateUI();
    }

    private void loadUserProfile() {
        Call<UserDto> call = apiService.getUserProfile("Bearer " + tokenManager.getAccessToken());
        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();

                    // Set delivery address from user profile
                    if (currentUser.getAddress() != null && !currentUser.getAddress().isEmpty()) {
                        textViewDeliveryAddress.setText(currentUser.getAddress());
                    } else if (currentUser.hasAddressComponents()) {
                        textViewDeliveryAddress.setText(currentUser.getFullAddress());
                    }

                    // Calculate shipping fee with user's address
                    calculateShippingFee();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Log.e("CheckoutActivity", "Error loading user profile: " + t.getMessage(), t);
            }
        });
    }

    private void calculateSubtotal() {
        subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
    }

    private void calculateShippingFee() {
        if (currentUser == null || !currentUser.hasAddressComponents()) {
            // Fallback to default shipping fee
            shippingFee = BigDecimal.valueOf(15000);
            updateUI();
            return;
        }

        // Create shipping calculation request
        ShippingCalculationRequest request = new ShippingCalculationRequest();
        request.setOrderAmount(subtotal);
        request.setDeliveryCity(currentUser.getUserCity());
        request.setDeliveryDistrict(currentUser.getUserDistrict());
        request.setDeliveryWard(currentUser.getUserWard());
        request.setDeliveryStreet(currentUser.getUserStreet());

        Call<ApiResponse<ShippingCalculationResponse>> call = apiService.calculateShippingWithAddress(request);
        call.enqueue(new Callback<ApiResponse<ShippingCalculationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ShippingCalculationResponse>> call, Response<ApiResponse<ShippingCalculationResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ShippingCalculationResponse shippingResponse = response.body().getData();
                    shippingFee = shippingResponse.getShippingFee();
                    updateUI();
                } else {
                    // Fallback to default shipping fee
                    shippingFee = BigDecimal.valueOf(15000);
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ShippingCalculationResponse>> call, Throwable t) {
                Log.e("CheckoutActivity", "Error calculating shipping fee: " + t.getMessage(), t);
                // Fallback to default shipping fee
                shippingFee = BigDecimal.valueOf(15000);
                updateUI();
            }
        });
    }

    private void updateUI() {
        textViewSubtotal.setText(PriceFormatter.format(subtotal));
        textViewShippingFee.setText(PriceFormatter.format(shippingFee));
        textViewDiscount.setText(PriceFormatter.format(discount));

        total = subtotal.add(shippingFee).subtract(discount);
        textViewTotal.setText(PriceFormatter.format(total));
    }

    private void placeOrder() {
        // Validate input
        String deliveryAddress = textViewDeliveryAddress.getText().toString().trim();
        if (deliveryAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected payment method
        String paymentMethod = getSelectedPaymentMethod();
        if (paymentMethod == null) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order request
        Order.CreateOrderRequest orderRequest = new Order.CreateOrderRequest();
        orderRequest.setDeliveryAddress(deliveryAddress);
        orderRequest.setDeliveryNotes(textViewDeliveryNotes.getText().toString().trim());
        orderRequest.setPaymentMethod(paymentMethod);

        // Add address components if available
        if (currentUser != null && currentUser.hasAddressComponents()) {
            orderRequest.setDeliveryCity(currentUser.getUserCity());
            orderRequest.setDeliveryDistrict(currentUser.getUserDistrict());
            orderRequest.setDeliveryWard(currentUser.getUserWard());
            orderRequest.setDeliveryStreet(currentUser.getUserStreet());
        }

        // Convert cart items to order items
        List<Order.OrderItemRequest> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Order.OrderItemRequest orderItem = new Order.OrderItemRequest();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);
        }
        orderRequest.setOrderItems(orderItems);

        // Show loading
        showLoading(true);

        // Call API to create order
        Call<ApiResponse<Order>> call = apiService.createOrder("Bearer " + tokenManager.getAccessToken(), orderRequest);
        call.enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Order order = response.body().getData();

                    // Clear cart
                    cartManager.clearCart();

                    // Navigate to success page
                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("order", order);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = response.body() != null ? response.body().getMessage() : "Lỗi tạo đơn hàng";
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                showLoading(false);
                Log.e("CheckoutActivity", "Error creating order: " + t.getMessage(), t);
                Toast.makeText(CheckoutActivity.this, "Lỗi tạo đơn hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedPaymentMethod() {
        int selectedId = radioGroupPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == radioButtonCash.getId()) {
            return "CASH";
        } else if (selectedId == radioButtonCard.getId()) {
            return "CARD";
        } else if (selectedId == radioButtonBankTransfer.getId()) {
            return "BANK_TRANSFER";
        } else if (selectedId == radioButtonEWallet.getId()) {
            return "E_WALLET";
        }
        return null;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonPlaceOrder.setEnabled(!show);
    }
}