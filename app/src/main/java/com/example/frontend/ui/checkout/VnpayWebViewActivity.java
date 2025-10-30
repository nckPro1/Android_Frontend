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

public class VnpayWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_PAYMENT_URL = "extra_payment_url";
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_NUMBER = "extra_order_number";
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";

    private WebView webView;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private TextView title;

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
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                tryHandleCallback(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String nextUrl = request.getUrl() != null ? request.getUrl().toString() : null;
                if (nextUrl != null) {
                    tryHandleCallback(nextUrl);
                }
                return false; // load inside WebView
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

    private void tryHandleCallback(String url) {
        if (url == null) return;
        // Detect our backend return URL path
        // Example: http://192.168.1.2:8080/api/payment/payment-callback?...&vnp_ResponseCode=00
        Uri uri = Uri.parse(url);
        if ("192.168.1.2".equals(uri.getHost()) && "/api/payment/payment-callback".equals(uri.getPath())) {
            String responseCode = uri.getQueryParameter("vnp_ResponseCode");
            String txnRef = uri.getQueryParameter("vnp_TxnRef");

            Long orderId = getLongExtraSafe(EXTRA_ORDER_ID);
            String orderNumber = getIntent().getStringExtra(EXTRA_ORDER_NUMBER);
            String totalAmount = getIntent().getStringExtra(EXTRA_TOTAL_AMOUNT);

            if ("00".equals(responseCode)) {
                // Success: go to OrderSuccessActivity and finish this WebView
                Intent intent = new Intent(this, com.example.frontend.ui.order.OrderSuccessActivity.class);
                if (orderNumber != null) intent.putExtra("orderNumber", orderNumber);
                if (totalAmount != null) intent.putExtra("totalAmount", totalAmount);
                if (orderId != null) intent.putExtra("orderId", orderId);
                intent.putExtra("paymentMethod", "VNPay");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Thanh toán thất bại (" + responseCode + ")", Toast.LENGTH_SHORT).show();
            }
        }
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


