package com.example.frontend.ui.checkout;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.R;
import com.example.frontend.util.CartManager;
import android.os.Handler;
import android.os.Looper;

public class VnpayWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_PAYMENT_URL = "extra_payment_url";
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_NUMBER = "extra_order_number";
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";

    private WebView webView;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private TextView title;
    private boolean isCallbackHandled = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_webview);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        title = findViewById(R.id.textTitle);

        title.setText("Thanh toán VNPay");
        btnBack.setOnClickListener(v -> finish());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                // Phát hiện callback URL sớm nhưng vẫn để backend xử lý
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                // Sau khi trang load xong (backend đã xử lý callback và trả về HTML)
                // Đợi một chút để đảm bảo backend đã hoàn thành việc cập nhật database
                if (url != null && isCallbackUrl(url) && !isCallbackHandled) {
                    // Đợi 500ms để đảm bảo backend đã xử lý và cập nhật trạng thái xong
                    handler.postDelayed(() -> {
                        if (!isCallbackHandled) {
                            tryHandleCallback(url);
                        }
                    }, 500);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Không chặn callback URL, để backend xử lý trước
                // Sau đó sẽ xử lý trong onPageFinished
                return false; // Tiếp tục load trong WebView để backend xử lý
            }
        });

        String paymentUrl = getIntent().getStringExtra(EXTRA_PAYMENT_URL);
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Không có liên kết thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        webView.loadUrl(paymentUrl);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Kiểm tra xem URL có phải là callback URL từ backend không
     */
    private boolean isCallbackUrl(String url) {
        if (url == null) return false;
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            // Kiểm tra path chứa payment-callback
            return path != null && path.contains("/api/payment/payment-callback");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Xử lý callback từ VNPay
     * Lưu ý: Hàm này được gọi SAU KHI backend đã xử lý callback và cập nhật trạng thái
     * @return true nếu đã xử lý thành công và redirect, false nếu chưa xử lý
     */
    private boolean tryHandleCallback(String url) {
        if (url == null || isCallbackHandled) return false;

        try {
            // Detect our backend return URL path
            // Example: http://192.168.1.2:8080/api/payment/payment-callback?...&vnp_ResponseCode=00
            Uri uri = Uri.parse(url);
            String path = uri.getPath();

            // Kiểm tra path có chứa payment-callback
            if (path != null && path.contains("/api/payment/payment-callback")) {
                String responseCode = uri.getQueryParameter("vnp_ResponseCode");
                String txnRef = uri.getQueryParameter("vnp_TxnRef");
                String transactionStatus = uri.getQueryParameter("vnp_TransactionStatus");

                Long orderId = getLongExtraSafe(EXTRA_ORDER_ID);
                String orderNumber = getIntent().getStringExtra(EXTRA_ORDER_NUMBER);
                String totalAmount = getIntent().getStringExtra(EXTRA_TOTAL_AMOUNT);

                // Kiểm tra thanh toán thành công (ResponseCode = 00 và TransactionStatus = 00)
                // Backend đã cập nhật trạng thái payment và order thành COMPLETED/DONE trong updatePaymentStatus()
                if ("00".equals(responseCode) && ("00".equals(transactionStatus) || transactionStatus == null)) {
                    isCallbackHandled = true;

                    // Clear cart sau khi thanh toán thành công
                    CartManager cartManager = CartManager.getInstance(this);
                    cartManager.clearCart();

                    // Success: go to OrderSuccessActivity and finish this WebView
                    Intent intent = new Intent(this, com.example.frontend.ui.order.OrderSuccessActivity.class);
                    if (orderNumber != null) intent.putExtra("orderNumber", orderNumber);
                    if (totalAmount != null) intent.putExtra("totalAmount", totalAmount);
                    if (orderId != null) intent.putExtra("orderId", orderId);
                    intent.putExtra("paymentMethod", "VNPay");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true; // Đã xử lý thành công
                } else {
                    // Thanh toán thất bại (backend đã cập nhật trạng thái thành FAILED)
                    isCallbackHandled = true;
                    String errorMsg = "Thanh toán thất bại";
                    if (responseCode != null && !responseCode.isEmpty()) {
                        errorMsg += " (Mã lỗi: " + responseCode + ")";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    // Vẫn finish activity để quay lại checkout
                    finish();
                    return true;
                }
            }
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            return false;
        }
        return false;
    }

    private Long getLongExtraSafe(String key) {
        try {
            if (getIntent().hasExtra(key)) {
                return getIntent().getLongExtra(key, -1L);
            }
        } catch (Exception ignored) {}
        return null;
    }
}


