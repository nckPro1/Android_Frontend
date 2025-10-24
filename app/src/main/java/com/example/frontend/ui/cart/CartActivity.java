package com.example.frontend.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;
import com.example.frontend.model.CartItem;
import com.example.frontend.model.User;
import com.example.frontend.ui.adapter.CartAdapter;
import com.example.frontend.ui.checkout.CheckoutActivity;
import com.example.frontend.ui.profile.DeliveryAddressActivity;
import com.example.frontend.util.CartManager;
import com.example.frontend.util.UserManager;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.model.ApiResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private TextView textViewSubtotal;
    private TextView textViewShippingFee;
    private TextView textViewDiscount;
    private TextView textViewTotal;
    private TextView textViewItemCount;
    private TextView textViewDeliveryAddress;
    private Button buttonCheckout;
    private Button buttonContinueShopping;

    private View emptyState;

    private List<CartItem> cartItems;
    private CartManager cartManager;
    private UserManager userManager;
    private CartAdapter cartAdapter;
    private ApiService apiService;

    // Pricing
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    private final NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        try {
            initViews();
            initCartManager();
            loadCartItems();
            setupRecyclerView();
            updateCartSummary();
            toggleEmptyState(cartItems.isEmpty());
        } catch (Exception e) {
            android.util.Log.e("CartActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo giỏ hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShippingFee = findViewById(R.id.textViewShippingFee);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewItemCount = findViewById(R.id.textViewItemCount);
        textViewDeliveryAddress = findViewById(R.id.textViewDeliveryAddress);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonContinueShopping = findViewById(R.id.buttonContinueShopping);
        emptyState = findViewById(R.id.emptyState);

        // Toolbar
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        if (textViewTitle != null) textViewTitle.setText("Giỏ hàng");
        if (imageViewBack != null) imageViewBack.setOnClickListener(v -> finish());

        // Buttons
        if (buttonCheckout != null) {
            buttonCheckout.setOnClickListener(v -> proceedToCheckout());
        }
        if (buttonContinueShopping != null) {
            buttonContinueShopping.setOnClickListener(v -> finish());
        }
        
        // Delivery address click listener
        if (textViewDeliveryAddress != null) {
            textViewDeliveryAddress.setOnClickListener(v -> {
                User user = userManager.getCurrentUser();
                if (user == null || user.getAddress() == null || user.getAddress().trim().isEmpty()) {
                    // Navigate to delivery address activity
                    Intent intent = new Intent(this, DeliveryAddressActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void initCartManager() {
        cartManager = CartManager.getInstance(this);
        userManager = UserManager.getInstance(this);
        apiService = ApiClient.getApiService();
    }

    private void loadCartItems() {
        // Load real cart items from CartManager
        cartItems = cartManager.getCartItems();
        if (cartItems == null) cartItems = new ArrayList<>();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                if (position < 0 || position >= cartItems.size()) return;
                CartItem item = cartItems.get(position);

                // Cập nhật nguồn dữ liệu chính
                cartManager.updateQuantity(item.getProductId(), newQuantity);

                // Đồng bộ lại list với CartManager để tránh lệch tham chiếu
                List<CartItem> latest = cartManager.getCartItems();
                cartItems.clear();
                if (latest != null) cartItems.addAll(latest);

                // Nếu quantity về 0, có thể đã bị remove trong CartManager
                if (newQuantity <= 0 && position < cartItems.size()) {
                    cartAdapter.notifyItemRemoved(position);
                } else {
                    cartAdapter.notifyItemChanged(position);
                }

                updateCartSummary();
                toggleEmptyState(cartItems.isEmpty());
            }

            @Override
            public void onItemRemoved(int position) {
                if (position < 0 || position >= cartItems.size()) return;
                CartItem item = cartItems.get(position);

                // Cập nhật nguồn dữ liệu trước
                cartManager.removeFromCart(item.getProductId());

                // Sync lại list từ CartManager
                List<CartItem> latest = cartManager.getCartItems();
                cartItems.clear();
                if (latest != null) cartItems.addAll(latest);

                cartAdapter.notifyItemRemoved(position);

                updateCartSummary();
                toggleEmptyState(cartItems.isEmpty());
            }
        });

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void updateCartSummary() {
        subtotal = BigDecimal.ZERO;

        int totalItemsCount = 0; // nếu muốn hiển thị tổng số món (số lượng), thay vì số dòng
        for (CartItem item : cartItems) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // cộng thêm extra của options
            if (item.getSelectedOptions() != null) {
                for (var option : item.getSelectedOptions()) {
                    if (option.getExtraPrice() != null) {
                        itemTotal = itemTotal.add(option.getExtraPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }

            subtotal = subtotal.add(itemTotal);
            totalItemsCount += item.getQuantity();
        }

        // Shipping: miễn phí nếu >= 200k, nếu không thì dùng giá mặc định từ admin
        if (subtotal.compareTo(new BigDecimal("200000")) >= 0) {
            shippingFee = BigDecimal.ZERO;
        } else {
            // Sử dụng giá ship mặc định từ admin (15k-20k)
            // Có thể lấy từ API hoặc SharedPreferences
            shippingFee = getDefaultShippingFee();
        }

        discount = BigDecimal.ZERO;
        total = subtotal.add(shippingFee).subtract(discount);

        // Update UI
        if (textViewSubtotal != null) textViewSubtotal.setText(formatPrice(subtotal));
        if (textViewShippingFee != null) textViewShippingFee.setText(formatPrice(shippingFee));
        if (textViewDiscount != null) textViewDiscount.setText(formatPrice(discount));
        if (textViewTotal != null) textViewTotal.setText(formatPrice(total));

        // Hiển thị tổng số mặt hàng (theo requirement của bạn có thể là số dòng hoặc số lượng)
        if (textViewItemCount != null) {
            textViewItemCount.setText(totalItemsCount + " sản phẩm");
        }

        if (buttonCheckout != null) {
            buttonCheckout.setEnabled(!cartItems.isEmpty());
        }

        // Update delivery address display
        updateDeliveryAddressDisplay();
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (emptyState != null) emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (recyclerViewCart != null) recyclerViewCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (buttonCheckout != null) buttonCheckout.setEnabled(!isEmpty);
    }

    private void proceedToCheckout() {
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("subtotal", subtotal.toPlainString());
        intent.putExtra("shippingFee", shippingFee.toPlainString());
        intent.putExtra("discount", discount.toPlainString());
        intent.putExtra("total", total.toPlainString());
        startActivity(intent);
    }

    private void updateDeliveryAddressDisplay() {
        User user = userManager.getCurrentUser();
        if (user != null && user.getAddress() != null && !user.getAddress().trim().isEmpty()) {
            if (textViewDeliveryAddress != null) {
                textViewDeliveryAddress.setText("📍 " + user.getAddress());
            }
        } else {
            if (textViewDeliveryAddress != null) {
                textViewDeliveryAddress.setText("📍 Chưa có địa chỉ giao hàng");
            }
        }
    }

    private BigDecimal getDefaultShippingFee() {
        // Giá ship mặc định từ admin (15k-20k)
        // Có thể lấy từ API hoặc SharedPreferences
        // Tạm thời dùng giá cố định 15k
        return new BigDecimal("15000");
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return vnd.format(0);
        // NumberFormat VND sẽ có ký hiệu "₫"
        return vnd.format(price);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cartManager != null) {
            List<CartItem> latest = cartManager.getCartItems();
            cartItems.clear();
            if (latest != null) cartItems.addAll(latest);
            if (cartAdapter != null) cartAdapter.notifyDataSetChanged();
            updateCartSummary();
            toggleEmptyState(cartItems.isEmpty());
        }
        
        // Refresh delivery address display when returning from profile
        updateDeliveryAddressDisplay();
    }
}
