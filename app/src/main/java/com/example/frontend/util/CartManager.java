package com.example.frontend.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.frontend.model.CartItem;
import com.example.frontend.model.Product;
import com.example.frontend.model.ProductOption;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREFS_NAME = "cart_prefs";
    private static final String KEY_CART_ITEMS = "cart_items";

    // 🟢 Đặt BASE_IMAGE_URL cho path tương đối (VD: https://api.yourdomain.com)
    // Nếu ảnh của bạn đã là full URL (http/https) thì có thể để rỗng ("")
    private static final String BASE_IMAGE_URL = "";

    private static CartManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private CartManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    // -------------------- Public APIs --------------------

    // Add product to cart
    public void addToCart(Product product, int quantity, List<ProductOption> selectedOptions) {
        android.util.Log.d("CartManager", "addToCart called - Product: " + (product != null ? product.getName() : "null") + ", Quantity: " + quantity);

        if (product == null || product.getProductId() == null) return;

        List<CartItem> cartItems = getCartItems();
        // Use current price (sale price if on sale, otherwise regular price)
        BigDecimal currentPrice = product.getCurrentPrice();

        // Create a temporary CartItem to find existing item with same productId + selectedOptions
        CartItem tempItem = new CartItem(
                product.getProductId(),
                safe(product.getName()),
                safe(product.getDescription()),
                ensureFullImageUrl(product.getImageUrl()),
                currentPrice != null ? currentPrice : BigDecimal.ZERO,
                0, // quantity doesn't matter for comparison
                selectedOptions != null ? selectedOptions : new ArrayList<>()
        );

        CartItem existingItem = findCartItemByProductAndOptions(tempItem, cartItems);

        String normalizedImage = ensureFullImageUrl(product.getImageUrl());

        if (existingItem != null) {
            // Cộng dồn số lượng cho item có cùng productId + selectedOptions
            existingItem.setQuantity(Math.max(0, existingItem.getQuantity() + quantity));
            // Nếu item cũ chưa có ảnh thì bổ sung
            if (isNullOrEmpty(existingItem.getProductImageUrl()) && !isNullOrEmpty(normalizedImage)) {
                existingItem.setProductImageUrl(normalizedImage);
            }
            android.util.Log.d("CartManager", "Updated existing item quantity to: " + existingItem.getQuantity());
        } else {
            // Tạo cart item mới với giá sale thông tin
            CartItem newItem = new CartItem(
                    product.getProductId(),
                    safe(product.getName()),
                    safe(product.getDescription()),
                    normalizedImage,
                    currentPrice != null ? currentPrice : BigDecimal.ZERO,
                    Math.max(0, quantity),
                    selectedOptions != null ? selectedOptions : new ArrayList<>()
            );

            // Set sale information
            newItem.setOriginalPrice(product.getPrice());
            newItem.setSalePrice(product.getSalePrice());
            newItem.setIsOnSale(product.getIsOnSale());
            newItem.setDiscountPercentage(product.getSalePercentage());

            cartItems.add(newItem);
            android.util.Log.d("CartManager", "Created new cart item for product: " + product.getName() + " with " + (selectedOptions != null ? selectedOptions.size() : 0) + " options, price: " + currentPrice);
        }

        saveCartItems(cartItems);
    }

    // Remove product from cart
    public void removeFromCart(Long productId) {
        if (productId == null) {
            android.util.Log.w("CartManager", "removeFromCart: productId is null");
            return;
        }

        List<CartItem> cartItems = getCartItems();
        int originalSize = cartItems.size();

        cartItems.removeIf(item -> item != null && productId.equals(item.getProductId()));

        int newSize = cartItems.size();
        android.util.Log.d("CartManager", "removeFromCart: removed " + (originalSize - newSize) + " items for productId: " + productId);

        saveCartItems(cartItems);
    }

    // Remove specific cart item (with specific options)
    public void removeCartItem(CartItem cartItem) {
        if (cartItem == null) return;
        List<CartItem> cartItems = getCartItems();
        cartItems.removeIf(item -> cartItem.equals(item));
        saveCartItems(cartItems);
    }

    // Update cart item (thay toàn bộ object)
    public void updateCartItem(CartItem cartItem) {
        if (cartItem == null || cartItem.getProductId() == null) return;
        List<CartItem> cartItems = getCartItems();

        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItem.equals(cartItems.get(i))) {
                // đảm bảo ảnh đã chuẩn hoá
                if (!isNullOrEmpty(cartItem.getProductImageUrl())) {
                    cartItem.setProductImageUrl(ensureFullImageUrl(cartItem.getProductImageUrl()));
                }
                if (cartItem.getSelectedOptions() == null) {
                    cartItem.setSelectedOptions(new ArrayList<>());
                }
                cartItems.set(i, cartItem);
                break;
            }
        }

        saveCartItems(cartItems);
    }

    // Update quantity for specific product
    public void updateQuantity(Long productId, int newQuantity) {
        if (productId == null) {
            android.util.Log.w("CartManager", "updateQuantity: productId is null");
            return;
        }

        List<CartItem> cartItems = getCartItems();
        boolean found = false;

        android.util.Log.d("CartManager", "updateQuantity: Looking for productId: " + productId + ", newQuantity: " + newQuantity);
        android.util.Log.d("CartManager", "updateQuantity: Current cart has " + cartItems.size() + " items");

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item != null && productId.equals(item.getProductId())) {
                android.util.Log.d("CartManager", "updateQuantity: Found item - " + item.getProductName() + ", current quantity: " + item.getQuantity());

                if (newQuantity <= 0) {
                    // Nếu về 0 thì xoá luôn (tránh quantity âm và trạng thái lệch)
                    cartItems.remove(i);
                    android.util.Log.d("CartManager", "Removed item with productId: " + productId);
                } else {
                    int oldQuantity = item.getQuantity();
                    item.setQuantity(newQuantity);
                    android.util.Log.d("CartManager", "Updated quantity for " + item.getProductName() + " from " + oldQuantity + " to " + newQuantity);
                }
                found = true;
                break;
            } else if (item != null) {
                android.util.Log.d("CartManager", "updateQuantity: Skipping item - " + item.getProductName() + " (ID: " + item.getProductId() + ")");
            }
        }

        if (!found) {
            android.util.Log.w("CartManager", "Product not found in cart: " + productId);
        }

        saveCartItems(cartItems);
        android.util.Log.d("CartManager", "updateQuantity: Cart saved with " + cartItems.size() + " items");
    }

    // Update quantity for specific cart item (with specific options)
    public void updateCartItemQuantity(CartItem cartItem, int newQuantity) {
        if (cartItem == null) {
            android.util.Log.w("CartManager", "updateCartItemQuantity: cartItem is null");
            return;
        }

        List<CartItem> cartItems = getCartItems();
        boolean found = false;

        android.util.Log.d("CartManager", "updateCartItemQuantity: Looking for item - " + cartItem.getProductName() + " (ID: " + cartItem.getProductId() + "), newQuantity: " + newQuantity);
        android.util.Log.d("CartManager", "updateCartItemQuantity: Current cart has " + cartItems.size() + " items");

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item != null) {
                boolean isEqual = cartItem.equals(item);
                android.util.Log.d("CartManager", "updateCartItemQuantity: Comparing item " + i + " - " + item.getProductName() + " (qty: " + item.getQuantity() + ") with target " + cartItem.getProductName() + " (qty: " + cartItem.getQuantity() + ") - equals: " + isEqual);

                if (isEqual) {
                    android.util.Log.d("CartManager", "updateCartItemQuantity: Found matching item - " + item.getProductName() + ", current quantity: " + item.getQuantity());

                    if (newQuantity <= 0) {
                        // Nếu về 0 thì xoá luôn
                        cartItems.remove(i);
                        android.util.Log.d("CartManager", "Removed item: " + item.getProductName());
                    } else {
                        int oldQuantity = item.getQuantity();
                        item.setQuantity(newQuantity);
                        android.util.Log.d("CartManager", "Updated quantity for " + item.getProductName() + " from " + oldQuantity + " to " + newQuantity);
                    }
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            android.util.Log.w("CartManager", "CartItem not found in cart: " + cartItem.getProductName());
        }

        saveCartItems(cartItems);
        android.util.Log.d("CartManager", "updateCartItemQuantity: Cart saved with " + cartItems.size() + " items");
    }

    // Get all cart items
    public List<CartItem> getCartItems() {
        try {
            String json = prefs.getString(KEY_CART_ITEMS, null);
            android.util.Log.d("CartManager", "getCartItems - JSON: " + (json != null ? json.substring(0, Math.min(100, json.length())) + "..." : "null"));

            if (json == null || json.isEmpty()) {
                android.util.Log.d("CartManager", "getCartItems - Returning empty list");
                return new ArrayList<>();
            }

            Type type = new TypeToken<List<CartItem>>(){}.getType();
            List<CartItem> items = gson.fromJson(json, type);
            if (items == null) items = new ArrayList<>();

            // 🔧 MIGRATION/FIX: chuẩn hoá ảnh & options null
            boolean mutated = false;
            for (CartItem it : items) {
                // Chuẩn hoá ảnh
                String img = it.getProductImageUrl();
                String normalized = ensureFullImageUrl(img);
                if (!equalsNullable(img, normalized)) {
                    it.setProductImageUrl(normalized);
                    mutated = true;
                }
                // Tránh options null
                if (it.getSelectedOptions() == null) {
                    it.setSelectedOptions(new ArrayList<>());
                    mutated = true;
                }
                // Tránh price null
                if (it.getUnitPrice() == null) {
                    it.setUnitPrice(BigDecimal.ZERO);
                    mutated = true;
                }
                // Tránh quantity âm
                if (it.getQuantity() < 0) {
                    it.setQuantity(0);
                    mutated = true;
                }
            }
            if (mutated) {
                saveCartItems(items);
            }

            return items;
        } catch (Exception e) {
            android.util.Log.e("CartManager", "Error parsing cart items: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // Get cart item by product ID
    public CartItem getCartItemByProductId(Long productId) {
        if (productId == null) return null;
        List<CartItem> cartItems = getCartItems();
        return findCartItemByProductId(productId, cartItems);
    }

    // Get total items count (sum quantity)
    public int getTotalItemsCount() {
        List<CartItem> cartItems = getCartItems();
        int totalCount = 0;
        for (CartItem item : cartItems) {
            totalCount += Math.max(0, item.getQuantity());
        }
        return totalCount;
    }

    // Get cart total amount
    public BigDecimal getCartTotal() {
        List<CartItem> cartItems = getCartItems();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            try {
                // Nếu CartItem có hàm getTotalPrice() đã tính cả options thì dùng luôn
                BigDecimal line = item.getTotalPrice();
                if (line == null) {
                    // Fallback: unit * qty
                    BigDecimal unit = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                    line = unit.multiply(BigDecimal.valueOf(Math.max(0, item.getQuantity())));
                }
                total = total.add(line);
            } catch (Exception ignore) {
                // an toàn trong mọi trường hợp
            }
        }

        return total;
    }

    // Clear cart
    public void clearCart() {
        android.util.Log.d("CartManager", "Clearing cart - before: " + getCartItems().size() + " items");
        prefs.edit().remove(KEY_CART_ITEMS).apply();
        android.util.Log.d("CartManager", "Clearing cart - after: " + getCartItems().size() + " items");
    }

    // Force clear cart and reset all cart data
    public void forceClearCart() {
        android.util.Log.d("CartManager", "Force clearing cart - before: " + getCartItems().size() + " items");
        prefs.edit().clear().apply();
        android.util.Log.d("CartManager", "Force clearing cart - after: " + getCartItems().size() + " items");
    }

    // Check if cart is empty
    public boolean isCartEmpty() {
        return getCartItems().isEmpty();
    }

    // Check if product is in cart
    public boolean isProductInCart(Long productId) {
        return getCartItemByProductId(productId) != null;
    }

    // Get cart item count for specific product
    public int getProductQuantity(Long productId) {
        CartItem item = getCartItemByProductId(productId);
        return item != null ? Math.max(0, item.getQuantity()) : 0;
    }

    // -------------------- Private helpers --------------------

    private CartItem findCartItemByProductId(Long productId, List<CartItem> cartItems) {
        if (cartItems == null) return null;
        for (CartItem item : cartItems) {
            if (productId.equals(item.getProductId())) {
                return item;
            }
        }
        return null;
    }

    private CartItem findCartItemByProductAndOptions(CartItem targetItem, List<CartItem> cartItems) {
        if (cartItems == null || targetItem == null) return null;
        for (CartItem item : cartItems) {
            if (targetItem.equals(item)) {
                return item;
            }
        }
        return null;
    }

    public void saveCartItems(List<CartItem> cartItems) {
        try {
            android.util.Log.d("CartManager", "saveCartItems called - Items count: " + (cartItems != null ? cartItems.size() : "null"));
            String json = gson.toJson(cartItems != null ? cartItems : new ArrayList<>());
            prefs.edit().putString(KEY_CART_ITEMS, json).apply();
            android.util.Log.d("CartManager", "saveCartItems completed");
        } catch (Exception e) {
            android.util.Log.e("CartManager", "Error saving cart items: " + e.getMessage(), e);
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean equalsNullable(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    /**
     * Chuẩn hoá URL ảnh:
     * - Nếu null/rỗng -> trả về null
     * - Nếu đã bắt đầu bằng http/https -> giữ nguyên
     * - Nếu là path tương đối -> ghép BASE_IMAGE_URL (nếu đã cấu hình)
     */
    private static String ensureFullImageUrl(String url) {
        if (isNullOrEmpty(url)) return null;
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return trimmed;
        }
        if (isNullOrEmpty(BASE_IMAGE_URL)) {
            // Không có base -> trả về nguyên gốc (để dev dễ thấy và sửa cấu hình)
            return trimmed;
        }
        // Ghép base và path, xử lý dấu '/'
        boolean baseEndsWithSlash = BASE_IMAGE_URL.endsWith("/");
        boolean pathStartsWithSlash = trimmed.startsWith("/");
        if (baseEndsWithSlash && pathStartsWithSlash) {
            return BASE_IMAGE_URL + trimmed.substring(1);
        } else if (!baseEndsWithSlash && !pathStartsWithSlash) {
            return BASE_IMAGE_URL + "/" + trimmed;
        } else {
            return BASE_IMAGE_URL + trimmed;
        }
    }
}
