package com.example.frontend.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.R;
import com.example.frontend.ui.home.HomeActivity;
import com.example.frontend.ui.order.OrderHistoryActivity;
import com.example.frontend.util.PriceFormatter;

import java.math.BigDecimal;

public class PaymentSuccessActivity extends AppCompatActivity {

    private TextView textViewOrderNumber;
    private TextView textViewTotalAmount;
    private TextView textViewPaymentMethod;
    private Button buttonViewOrder;
    private Button buttonContinueShopping;
    private ImageView imageViewBack;

    private String orderNumber;
    private BigDecimal totalAmount;
    private String paymentMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        initViews();
        loadData();
        setupClickListeners();
    }

    private void initViews() {
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewPaymentMethod = findViewById(R.id.textViewPaymentMethod);
        buttonViewOrder = findViewById(R.id.buttonViewOrder);
        buttonContinueShopping = findViewById(R.id.buttonContinueShopping);
        imageViewBack = findViewById(R.id.imageViewBack);
    }

    private void loadData() {
        orderNumber = getIntent().getStringExtra("orderNumber");
        totalAmount = new BigDecimal(getIntent().getStringExtra("totalAmount"));
        paymentMethod = getIntent().getStringExtra("paymentMethod");

        if (orderNumber != null) {
            textViewOrderNumber.setText("Đơn hàng: " + orderNumber);
        }
        if (totalAmount != null) {
            textViewTotalAmount.setText(PriceFormatter.format(totalAmount));
        }
        if (paymentMethod != null) {
            textViewPaymentMethod.setText("Phương thức: " + paymentMethod);
        } else {
            textViewPaymentMethod.setText("Phương thức: Tiền mặt");
        }
    }

    private void setupClickListeners() {
        imageViewBack.setOnClickListener(v -> goToHome());

        buttonViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
            finish();
        });

        buttonContinueShopping.setOnClickListener(v -> goToHome());
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goToHome();
    }
}

