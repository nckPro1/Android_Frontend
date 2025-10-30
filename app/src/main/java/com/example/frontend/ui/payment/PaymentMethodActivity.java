package com.example.frontend.ui.payment;

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
import com.example.frontend.model.PaymentMethod;
import com.example.frontend.ui.adapter.PaymentMethodAdapter;
import com.example.frontend.ui.checkout.CheckoutActivity;
import com.example.frontend.util.PriceFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPaymentMethods;
    private TextView textViewTotalAmount;
    private TextView textViewOrderNumber;
    private Button buttonConfirmPayment;
    private ImageView imageViewBack;

    private List<PaymentMethod> paymentMethods;
    private PaymentMethodAdapter paymentMethodAdapter;
    private PaymentMethod selectedPaymentMethod;
    private BigDecimal totalAmount;
    private String orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        initViews();
        loadData();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        recyclerViewPaymentMethods = findViewById(R.id.recyclerViewPaymentMethods);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        buttonConfirmPayment = findViewById(R.id.buttonConfirmPayment);
        imageViewBack = findViewById(R.id.imageViewBack);
    }

    private void loadData() {
        // Lấy dữ liệu từ intent
        totalAmount = new BigDecimal(getIntent().getStringExtra("totalAmount"));
        orderNumber = getIntent().getStringExtra("orderNumber");

        // Hiển thị thông tin
        textViewTotalAmount.setText(PriceFormatter.format(totalAmount));
        textViewOrderNumber.setText("Đơn hàng: " + orderNumber);

        // Tạo danh sách phương thức thanh toán
        createPaymentMethods();
    }

    private void createPaymentMethods() {
        paymentMethods = new ArrayList<>();

        // CASH - Tiền mặt khi nhận hàng
        PaymentMethod cash = new PaymentMethod();
        cash.setMethodId("CASH");
        cash.setMethodName("Tiền mặt");
        cash.setDescription("Thanh toán khi nhận hàng");
        cash.setActive(true);
        cash.setOnline(false);
        paymentMethods.add(cash);
    }

    private void setupRecyclerView() {
        paymentMethodAdapter = new PaymentMethodAdapter(paymentMethods, new PaymentMethodAdapter.OnPaymentMethodSelectedListener() {
            @Override
            public void onPaymentMethodSelected(PaymentMethod paymentMethod) {
                selectedPaymentMethod = paymentMethod;
                buttonConfirmPayment.setEnabled(true);
                buttonConfirmPayment.setText("Thanh toán với " + paymentMethod.getMethodName());
            }
        });

        recyclerViewPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPaymentMethods.setAdapter(paymentMethodAdapter);
    }

    private void setupClickListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        buttonConfirmPayment.setOnClickListener(v -> {
            if (selectedPaymentMethod != null) {
                processPayment();
            } else {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPayment() {
        if (selectedPaymentMethod.getMethodId().equals("CASH")) {
            // Xử lý thanh toán tiền mặt
            processCashPayment();
        } else {
            Toast.makeText(this, "Phương thức thanh toán không được hỗ trợ", Toast.LENGTH_SHORT).show();
        }
    }

    private void processCashPayment() {
        // Hiển thị dialog xác nhận
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thanh toán")
                .setMessage("Bạn đã chọn thanh toán tiền mặt khi nhận hàng. Đơn hàng sẽ được xác nhận ngay lập tức.")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Gọi API xác nhận thanh toán tiền mặt
                    confirmCashPayment();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmCashPayment() {
        // TODO: Gọi API xác nhận thanh toán tiền mặt
        Toast.makeText(this, "Đơn hàng đã được xác nhận. Bạn sẽ thanh toán khi nhận hàng.", Toast.LENGTH_LONG).show();
        
        // Chuyển về màn hình chính
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

