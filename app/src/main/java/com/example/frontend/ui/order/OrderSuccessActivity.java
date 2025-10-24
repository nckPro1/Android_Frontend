package com.example.frontend.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.R;
import com.example.frontend.ui.home.HomeActivity;
import com.example.frontend.util.PriceFormatter;

public class OrderSuccessActivity extends AppCompatActivity {

    private TextView textViewOrderNumber;
    private TextView textViewTotalAmount;
    private TextView textViewPaymentMethod;
    private Button buttonBackToHome;
    private Button buttonViewOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewPaymentMethod = findViewById(R.id.textViewPaymentMethod);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
        buttonViewOrder = findViewById(R.id.buttonViewOrder);
    }

    private void loadData() {
        Intent intent = getIntent();
        
        String orderNumber = intent.getStringExtra("orderNumber");
        String totalAmount = intent.getStringExtra("totalAmount");
        String paymentMethod = intent.getStringExtra("paymentMethod");

        if (orderNumber != null) {
            textViewOrderNumber.setText(orderNumber);
        }

        if (totalAmount != null) {
            try {
                textViewTotalAmount.setText(PriceFormatter.format(new java.math.BigDecimal(totalAmount)));
            } catch (Exception e) {
                textViewTotalAmount.setText(totalAmount);
            }
        }

        if (paymentMethod != null) {
            textViewPaymentMethod.setText(paymentMethod);
        }
    }

    private void setupListeners() {
        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        buttonViewOrder.setOnClickListener(v -> {
            Intent intent = getIntent();
            Long orderId = intent.getLongExtra("orderId", -1);
            
            if (orderId != -1) {
                Intent detailIntent = new Intent(this, OrderDetailActivity.class);
                detailIntent.putExtra("orderId", orderId);
                startActivity(detailIntent);
            } else {
                // Fallback to home if no orderId
                Intent intent2 = new Intent(this, HomeActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish();
            }
        });
    }
}
