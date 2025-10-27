package com.example.frontend.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private final Handler handler = new Handler(Looper.getMainLooper());

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
                    // Navigate to checkout activity
                    Intent intent = new Intent(this, CheckoutActivity.class);
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
                try {
                    android.util.Log.d("CartActivity", "onQuantityChanged called - position: " + position + ", newQuantity: " + newQuantity);

                    if (position < 0 || position >= cartItems.size()) {
                        android.util.Log.w("CartActivity", "Invalid position for quantity change: " + position);
                        return;
                    }

                    CartItem item = cartItems.get(position);
                    if (item == null) {
                        android.util.Log.w("CartActivity", "CartItem is null at position: " + position);
                        return;
                    }

                    android.util.Log.d("CartActivity", "Updating quantity for: " + item.getProductName() + " at position: " + position + " to: " + newQuantity);

                    // Cập nhật trực tiếp trong cartItems
                    item.setQuantity(newQuantity);
                    cartManager.saveCartItems(cartItems);

                    // Delay refresh để đảm bảo CartManager đã save xong
                    handler.postDelayed(() -> refreshCartData(), 100);

                } catch (Exception e) {
                    android.util.Log.e("CartActivity", "Error in onQuantityChanged: " + e.getMessage(), e);
                    Toast.makeText(CartActivity.this, "Lỗi cập nhật số lượng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemRemoved(int position) {
                try {
                    android.util.Log.d("CartActivity", "onItemRemoved called - position: " + position);

                    if (position < 0 || position >= cartItems.size()) {
                        android.util.Log.w("CartActivity", "Invalid position for removal: " + position);
                        return;
                    }

                    CartItem item = cartItems.get(position);
                    if (item == null) {
                        android.util.Log.w("CartActivity", "CartItem is null at position: " + position);
                        return;
                    }

                    android.util.Log.d("CartActivity", "Removing item: " + item.getProductName() + " at position: " + position);

                    // Xóa trực tiếp từ cartItems
                    cartItems.remove(position);
                    cartManager.saveCartItems(cartItems);

                    // Reload toàn bộ data và refresh adapter
                    refreshCartData();

                } catch (Exception e) {
                    android.util.Log.e("CartActivity", "Error in onItemRemoved: " + e.getMessage(), e);
                    Toast.makeText(CartActivity.this, "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void updateCartSummary() {
        subtotal = BigDecimal.ZERO;

        int totalItemsCount = 0; // nếu muốn hiển thị tổng số món (số lượng), thay vì số dòng
        for (CartItem item : cartItems) {
            // Use current price (sale price if on sale, otherwise regular price)
            BigDecimal unitPrice = item.getCurrentPrice() != null ? item.getCurrentPrice() : BigDecimal.ZERO;
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // cộng thêm extra của options
            if (item.getSelectedOptions() != null) {
                for (var option : item.getSelectedOptions()) {
                    if (option.getPrice() != null) {
                        itemTotal = itemTotal.add(option.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }

            subtotal = subtotal.add(itemTotal);
            totalItemsCount += item.getQuantity();
        }

        // Cart không hiển thị phí ship, chỉ hiển thị subtotal
        shippingFee = BigDecimal.ZERO;
        discount = BigDecimal.ZERO;
        total = subtotal; // Chỉ tính subtotal, không cộng shipping fee

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

    private void refreshCartData() {
        try {
            android.util.Log.d("CartActivity", "Refreshing cart data...");

            // Reload data từ CartManager
            List<CartItem> latest = cartManager.getCartItems();
            android.util.Log.d("CartActivity", "Latest cart from CartManager has " + (latest != null ? latest.size() : 0) + " items");

            if (latest != null) {
                for (int i = 0; i < latest.size(); i++) {
                    CartItem item = latest.get(i);
                    android.util.Log.d("CartActivity", "Item " + i + ": " + item.getProductName() + " (qty: " + item.getQuantity() + ")");
                }
            }

            cartItems.clear();
            if (latest != null) {
                cartItems.addAll(latest);
            }

            android.util.Log.d("CartActivity", "Cart data refreshed. New size: " + cartItems.size());

            // Refresh adapter - recreate to avoid cached data issues
            if (cartAdapter != null) {
                // Create new adapter with fresh data
                cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
                    @Override
                    public void onQuantityChanged(int position, int newQuantity) {
                        try {
                            android.util.Log.d("CartActivity", "onQuantityChanged called - position: " + position + ", newQuantity: " + newQuantity);

                            if (position < 0 || position >= cartItems.size()) {
                                android.util.Log.w("CartActivity", "Invalid position for quantity change: " + position);
                                return;
                            }

                            CartItem item = cartItems.get(position);
                            if (item == null) {
                                android.util.Log.w("CartActivity", "CartItem is null at position: " + position);
                                return;
                            }

                            android.util.Log.d("CartActivity", "Updating quantity for: " + item.getProductName() + " at position: " + position + " to: " + newQuantity);

                            // Cập nhật trực tiếp trong cartItems
                            item.setQuantity(newQuantity);
                            cartManager.saveCartItems(cartItems);

                            // Delay refresh để đảm bảo CartManager đã save xong
                            handler.postDelayed(() -> refreshCartData(), 100);

                        } catch (Exception e) {
                            android.util.Log.e("CartActivity", "Error in onQuantityChanged: " + e.getMessage(), e);
                            Toast.makeText(CartActivity.this, "Lỗi cập nhật số lượng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onItemRemoved(int position) {
                        try {
                            android.util.Log.d("CartActivity", "onItemRemoved called - position: " + position);

                            if (position < 0 || position >= cartItems.size()) {
                                android.util.Log.w("CartActivity", "Invalid position for removal: " + position);
                                return;
                            }

                            CartItem item = cartItems.get(position);
                            if (item == null) {
                                android.util.Log.w("CartActivity", "CartItem is null at position: " + position);
                                return;
                            }

                            android.util.Log.d("CartActivity", "Removing item: " + item.getProductName() + " at position: " + position);

                            // Xóa trực tiếp từ cartItems
                            cartItems.remove(position);
                            cartManager.saveCartItems(cartItems);

                            // Delay refresh để đảm bảo CartManager đã save xong
                            handler.postDelayed(() -> refreshCartData(), 100);

                        } catch (Exception e) {
                            android.util.Log.e("CartActivity", "Error in onItemRemoved: " + e.getMessage(), e);
                            Toast.makeText(CartActivity.this, "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Set new adapter
                if (recyclerViewCart != null) {
                    recyclerViewCart.setAdapter(cartAdapter);
                }
            }

            // Update UI
            updateCartSummary();
            toggleEmptyState(cartItems.isEmpty());

        } catch (Exception e) {
            android.util.Log.e("CartActivity", "Error refreshing cart data: " + e.getMessage(), e);
        }
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
