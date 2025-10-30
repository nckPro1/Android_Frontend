package com.example.frontend.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.frontend.model.ShippingFeeInfo;
import com.example.frontend.model.UserDto;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.cart.CartActivity;
import com.example.frontend.ui.auth.login.LoginActivity;
import com.example.frontend.util.CartManager;
import com.example.frontend.util.PriceFormatter;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.CreateVnpayPaymentRequest;
import com.example.frontend.model.VnpayPaymentResponse;
import android.net.Uri;

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
    private EditText editTextCouponCode;
    private Button buttonApplyCoupon;
    private LinearLayout layoutAppliedCoupon;
    private TextView textViewCouponDiscount;
    private ImageView imageViewRemoveCoupon;
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
    private List<String> appliedCouponCodes = new ArrayList<>();
    private List<com.example.frontend.model.Coupon> appliedCouponObjs = new ArrayList<>();
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
        editTextCouponCode = findViewById(R.id.editTextCouponCode);
        buttonApplyCoupon = findViewById(R.id.buttonApplyCoupon);
        layoutAppliedCoupon = findViewById(R.id.layoutAppliedCoupon);
        textViewCouponDiscount = findViewById(R.id.textViewCouponDiscount);
        imageViewRemoveCoupon = findViewById(R.id.imageViewRemoveCoupon);
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

        // Coupon listeners
        buttonApplyCoupon.setOnClickListener(v -> applyCoupon());
        imageViewRemoveCoupon.setOnClickListener(v -> removeCoupon());
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

                    // Set delivery address from user profile if available
                    if (currentUser.getAddress() != null && !currentUser.getAddress().isEmpty()) {
                        textViewDeliveryAddress.setText(currentUser.getAddress());
                    }

                    // Calculate shipping fee (simplified - just use default fee)
                    calculateShippingFee();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Log.e("CheckoutActivity", "Error loading user profile: " + t.getMessage(), t);
                // Still calculate shipping fee even if user profile fails
                calculateShippingFee();
            }
        });
    }

    private BigDecimal calculateSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            // Use sale price if available; otherwise unit price
            BigDecimal priceToUse = item.getSalePrice() != null && item.getSalePrice().compareTo(BigDecimal.ZERO) > 0
                    ? item.getSalePrice()
                    : (item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);

            // Base price for quantity
            BigDecimal lineTotal = priceToUse.multiply(BigDecimal.valueOf(item.getQuantity()));

            // Add selected options price per unit times quantity
            if (item.getSelectedOptions() != null) {
                BigDecimal optionsPerUnit = BigDecimal.ZERO;
                for (com.example.frontend.model.ProductOption option : item.getSelectedOptions()) {
                    if (option.getPrice() != null) {
                        optionsPerUnit = optionsPerUnit.add(option.getPrice());
                    }
                }
                lineTotal = lineTotal.add(optionsPerUnit.multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            total = total.add(lineTotal);
        }
        subtotal = total; // Update field for other uses
        return total;
    }

    // Subtotal based on original unit prices (for coupon validation/calculation)
    private BigDecimal calculateBaseSubtotalOriginal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal priceToUse = (item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
            BigDecimal lineTotal = priceToUse.multiply(BigDecimal.valueOf(item.getQuantity()));

            if (item.getSelectedOptions() != null) {
                BigDecimal optionsPerUnit = BigDecimal.ZERO;
                for (com.example.frontend.model.ProductOption option : item.getSelectedOptions()) {
                    if (option.getPrice() != null) {
                        optionsPerUnit = optionsPerUnit.add(option.getPrice());
                    }
                }
                lineTotal = lineTotal.add(optionsPerUnit.multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            total = total.add(lineTotal);
        }
        return total;
    }

    private void calculateShippingFee() {
        String deliveryAddress = textViewDeliveryAddress.getText().toString().trim();

        Log.d("CheckoutActivity", "🔍 calculateShippingFee called. Address length: " + deliveryAddress.length());

        // Always call API to get shipping fee from database
        // Don't return early even if address is empty

        if (deliveryAddress.isEmpty()) {
            Log.d("CheckoutActivity", "⚠️ Address is empty, but still calling API to get default shipping fee");
        } else {
            Log.d("CheckoutActivity", "✓ Address exists: " + deliveryAddress.substring(0, Math.min(20, deliveryAddress.length())) + "...");
        }

        // Call backend API to get shipping fee info
        Log.d("CheckoutActivity", "Calling API: GET /api/orders/shipping-fee");
        Call<ApiResponse<ShippingFeeInfo>> call = apiService.getShippingFeeInfo();
        call.enqueue(new Callback<ApiResponse<ShippingFeeInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<ShippingFeeInfo>> call, Response<ApiResponse<ShippingFeeInfo>> response) {
                Log.d("CheckoutActivity", "API Response received. isSuccessful=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ShippingFeeInfo shippingInfo = response.body().getData();

                    // Logic: Nếu subtotal >= freeShippingThreshold thì free ship
                    BigDecimal freeShippingThreshold = shippingInfo.getFreeShippingThreshold();
                    BigDecimal defaultShippingFee = shippingInfo.getDefaultShippingFee();

                    if (freeShippingThreshold != null && subtotal.compareTo(freeShippingThreshold) >= 0) {
                        // Subtotal >= threshold → FREE SHIP
                        shippingFee = BigDecimal.ZERO;
                        Log.d("CheckoutActivity", "✓ Free shipping! Subtotal=" + subtotal + " >= threshold=" + freeShippingThreshold);
                    } else {
                        // Subtotal < threshold → pay shipping
                        shippingFee = defaultShippingFee != null ? defaultShippingFee : BigDecimal.ZERO;
                        Log.d("CheckoutActivity", "✓ Shipping fee: " + shippingFee + " (subtotal=" + subtotal + ", threshold=" + freeShippingThreshold + ")");
                    }

                    updateUI();
                } else {
                    Log.w("CheckoutActivity", "API response failed or unsuccessful. Body: " +
                            (response.body() != null ? response.body().getMessage() : "null"));
                    // Fallback to 0 if API fails
                    shippingFee = BigDecimal.ZERO;
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ShippingFeeInfo>> call, Throwable t) {
                Log.e("CheckoutActivity", "Error calling shipping fee API: " + t.getMessage(), t);
                // Fallback to 0 if API call fails
                shippingFee = BigDecimal.ZERO;
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

        // Add coupon code if applied
        if (!appliedCouponCodes.isEmpty()) {
            orderRequest.setCouponCodes(appliedCouponCodes);
        }

        // Convert cart items to order items
        List<Order.OrderItemRequest> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Order.OrderItemRequest orderItem = new Order.OrderItemRequest();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());

            // Add selected option IDs if any
            if (cartItem.getSelectedOptions() != null && !cartItem.getSelectedOptions().isEmpty()) {
                List<Long> optionIds = new ArrayList<>();
                for (var option : cartItem.getSelectedOptions()) {
                    optionIds.add(option.getOptionId());
                }
                orderItem.setSelectedOptionIds(optionIds);
            }

            orderItems.add(orderItem);
        }
        orderRequest.setOrderItems(orderItems);

        Log.d("CheckoutActivity", "📤 Sending order request with couponCodes: " + (orderRequest.getCouponCodes() != null ? orderRequest.getCouponCodes().toString() : "[]"));

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

                    // Route based on payment method
                    // Cart sẽ được clear SAU KHI thanh toán thành công
                    String selectedPaymentMethod = getSelectedPaymentMethod();
                    if ("CASH".equals(selectedPaymentMethod)) {
                        // For cash payment, go directly to success page
                        handleCashPayment(order);
                    } else if ("E_WALLET".equals(selectedPaymentMethod)) {
                        // Treat E_WALLET as VNPay for demo sandbox
                        handleVnpayPayment(order);
                    } else {
                        // For other payment methods, show error
                        Toast.makeText(CheckoutActivity.this, "Chỉ hỗ trợ thanh toán tiền mặt", Toast.LENGTH_SHORT).show();
                    }
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

    // Coupon methods
    private void applyCoupon() {
        String couponCode = editTextCouponCode.getText().toString().trim();
        if (couponCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (appliedCouponCodes.contains(couponCode)) {
            Toast.makeText(this, "Mã này đã được áp dụng rồi!", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        // Validate against original-price subtotal (not sale/current price)
        BigDecimal baseSubtotal = calculateBaseSubtotalOriginal();
        Call<com.example.frontend.model.Coupon> call = apiService.validateCoupon(couponCode, baseSubtotal);
        call.enqueue(new Callback<com.example.frontend.model.Coupon>() {
            @Override
            public void onResponse(Call<com.example.frontend.model.Coupon> call, Response<com.example.frontend.model.Coupon> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    com.example.frontend.model.Coupon coupon = response.body();
                    if (!coupon.isCanUse()) {
                        Toast.makeText(CheckoutActivity.this, coupon.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    appliedCouponCodes.add(couponCode);
                    appliedCouponObjs.add(coupon);
                    updateDiscountTotal();
                    updateCouponsUI();
                    editTextCouponCode.setText("");
                    Toast.makeText(CheckoutActivity.this, "Áp dụng mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<com.example.frontend.model.Coupon> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, "Lỗi kiểm tra mã giảm giá: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCoupon() {
        if (!appliedCouponCodes.isEmpty()) {
            appliedCouponCodes.clear();
            appliedCouponObjs.clear();
            updateDiscountTotal();
            updateCouponsUI();
        }
        Toast.makeText(this, "Đã xóa mã giảm giá", Toast.LENGTH_SHORT).show();
    }

    private void updateDiscountTotal() {
        // Display subtotal uses sale price; coupon discount must be computed on original-price subtotal
        BigDecimal displaySubtotal = calculateSubtotal();
        BigDecimal baseSubtotal = calculateBaseSubtotalOriginal();

        discount = BigDecimal.ZERO;
        for (com.example.frontend.model.Coupon c : appliedCouponObjs) {
            BigDecimal thisDiscount;
            if (c.isPercentageDiscount()) {
                thisDiscount = baseSubtotal.multiply(c.getDiscountValue()).divide(BigDecimal.valueOf(100));
            } else {
                thisDiscount = (c.getDiscountValue() != null ? c.getDiscountValue() : BigDecimal.ZERO);
            }
            if (thisDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.add(thisDiscount);
            }
        }
        if (discount.compareTo(baseSubtotal) > 0) discount = baseSubtotal;
        textViewDiscount.setText(PriceFormatter.format(discount));
        total = displaySubtotal.add(shippingFee).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        textViewTotal.setText(PriceFormatter.format(total));
    }

    private void updateCouponsUI() {
        layoutAppliedCoupon.removeAllViews();
        for (int i = 0; i < appliedCouponCodes.size(); i++) {
            String code = appliedCouponCodes.get(i);
            TextView tv = new TextView(this);
            tv.setText("- " + code);
            tv.setOnClickListener(v -> {
                int idx = appliedCouponCodes.indexOf(code);
                if (idx >= 0) {
                    appliedCouponCodes.remove(idx);
                    appliedCouponObjs.remove(idx);
                    updateDiscountTotal();
                    updateCouponsUI();
                }
            });
            layoutAppliedCoupon.addView(tv);
        }
        if (appliedCouponCodes.isEmpty()) {
            layoutAppliedCoupon.setVisibility(View.GONE);
        } else {
            layoutAppliedCoupon.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Xử lý thanh toán tiền mặt - chuyển thẳng đến trang thành công
     */
    private void handleCashPayment(Order order) {
        // Tạo request thanh toán tiền mặt
        com.example.frontend.model.CreatePaymentRequest paymentRequest = new com.example.frontend.model.CreatePaymentRequest();
        paymentRequest.setOrderId(order.getOrderId());
        paymentRequest.setPaymentMethod("CASH"); // Đổi từ setPaymentMethodCode -> setPaymentMethod
        paymentRequest.setAmount(order.getFinalAmount());
        paymentRequest.setDescription("Thanh toán tiền mặt cho đơn hàng " + order.getOrderNumber());

        // Gọi API tạo thanh toán tiền mặt
        Call<ApiResponse<Object>> call = apiService.createPayment("Bearer " + tokenManager.getAccessToken(), paymentRequest);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Clear cart sau khi thanh toán tiền mặt thành công
                    cartManager.clearCart();

                    // Chuyển đến trang đặt hàng thành công
                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("orderNumber", order.getOrderNumber());
                    intent.putExtra("totalAmount", order.getFinalAmount().toString());
                    intent.putExtra("paymentMethod", "Tiền mặt khi nhận hàng");
                    intent.putExtra("orderId", order.getOrderId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = response.body() != null ? response.body().getMessage() : "Lỗi tạo thanh toán";
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e("CheckoutActivity", "Error creating cash payment: " + t.getMessage(), t);
                Toast.makeText(CheckoutActivity.this, "Lỗi tạo thanh toán: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Khởi tạo thanh toán VNPay (sandbox): gọi backend lấy paymentUrl và mở trình duyệt
     */
    private void handleVnpayPayment(Order order) {
        // Build request
        long amountVnd = order.getFinalAmount() != null ? order.getFinalAmount().longValue() : 0L;
        CreateVnpayPaymentRequest req = new CreateVnpayPaymentRequest();
        req.setOrderId(order.getOrderId());
        req.setAmount(amountVnd);
        req.setOrderInfo("Thanh toan don hang " + order.getOrderNumber());
        req.setIpAddr(null); // backend sẽ tự lấy IP từ request

        showLoading(true);
        Call<ApiResponse<VnpayPaymentResponse>> call = apiService.createVnpayPayment(
                "Bearer " + tokenManager.getAccessToken(),
                req);
        call.enqueue(new Callback<ApiResponse<VnpayPaymentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VnpayPaymentResponse>> call, Response<ApiResponse<VnpayPaymentResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    String paymentUrl = response.body().getData().getPaymentUrl();
                    Intent intent = new Intent(CheckoutActivity.this, VnpayWebViewActivity.class);
                    intent.putExtra(VnpayWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl);
                    intent.putExtra(VnpayWebViewActivity.EXTRA_ORDER_ID, order.getOrderId());
                    intent.putExtra(VnpayWebViewActivity.EXTRA_ORDER_NUMBER, order.getOrderNumber());
                    intent.putExtra(VnpayWebViewActivity.EXTRA_TOTAL_AMOUNT, order.getFinalAmount() != null ? order.getFinalAmount().toString() : null);
                    startActivity(intent);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi tạo thanh toán VNPay";
                    Toast.makeText(CheckoutActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VnpayPaymentResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, "Lỗi gọi VNPay: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
