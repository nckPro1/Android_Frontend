package com.example.frontend.ui.auth.otp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Thêm các import cần thiết
import com.example.frontend.R;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.AuthResponse;
import com.example.frontend.model.OTPVerifyRequest;
import com.example.frontend.model.ResendOTPRequest;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.auth.login.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerifyOtp, btnResendOtp;
    private TextView tvEmail, tvStatus;
    private ApiService apiService; // Retrofit ApiService
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Lỗi: Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ KHỞI TẠO RETROFIT
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupOtpInputs();
        setupClickListeners();

        tvEmail.setText("Mã OTP đã được gửi đến\n" + email);
    }

    private void initViews() {
        // ... giữ nguyên code của bạn ...
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        tvEmail = findViewById(R.id.tvEmail);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatus.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnResendOtp.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {
        String otp = etOtp1.getText().toString() + etOtp2.getText().toString() +
                etOtp3.getText().toString() + etOtp4.getText().toString() +
                etOtp5.getText().toString() + etOtp6.getText().toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP!", Toast.LENGTH_SHORT).show();
            return;
        }

        setStatus("Đang xác thực...");
        btnVerifyOtp.setEnabled(false);
        btnResendOtp.setEnabled(false);

        // ✅ SỬ DỤNG RETROFIT ĐỂ GỌI API
        OTPVerifyRequest request = new OTPVerifyRequest(email, otp);
        apiService.verifyOtp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnVerifyOtp.setEnabled(true);
                btnResendOtp.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    handleVerifyResponse(response.body());
                } else {
                    setStatus("❌ Lỗi server: " + response.code(), "#F44336");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnVerifyOtp.setEnabled(true);
                btnResendOtp.setEnabled(true);
                setStatus("❌ Lỗi mạng: " + t.getMessage(), "#F44336");
                Log.e("OTPVerify", "Network Failure", t);
            }
        });
    }

    private void resendOtp() {
        setStatus("Đang gửi lại OTP...");
        btnResendOtp.setEnabled(false);
        btnVerifyOtp.setEnabled(false);

        ResendOTPRequest request = new ResendOTPRequest(email);
        apiService.resendOtp(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnResendOtp.setEnabled(true);
                btnVerifyOtp.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        setStatus("✅ " + apiResponse.getMessage(), "#4CAF50");
                        Toast.makeText(OTPActivity.this, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
                    } else {
                        setStatus("❌ " + apiResponse.getMessage(), "#F44336");
                    }
                } else {
                    setStatus("❌ Lỗi server khi gửi lại OTP.", "#F44336");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnResendOtp.setEnabled(true);
                btnVerifyOtp.setEnabled(true);
                setStatus("❌ Lỗi mạng: " + t.getMessage(), "#F44336");
                Log.e("ResendOTP", "Network Failure", t);
            }
        });
    }

    private void handleVerifyResponse(AuthResponse authResponse) {
        if (authResponse.isSuccess()) {
            setStatus("✅ " + authResponse.getMessage(), "#4CAF50");
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();

            // Chuyển về màn hình Login và tự động điền email
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("registrationSuccess", true);
            intent.putExtra("registeredEmail", email);
            startActivity(intent);
            finish();
        } else {
            setStatus("❌ " + authResponse.getMessage(), "#F44336");
            Toast.makeText(this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Các hàm tiện ích (setupOtpInputs, setStatus) giữ nguyên
    private void setupOtpInputs() {
        setupOtpInput(etOtp1, etOtp2);
        setupOtpInput(etOtp2, etOtp3);
        setupOtpInput(etOtp3, etOtp4);
        setupOtpInput(etOtp4, etOtp5);
        setupOtpInput(etOtp5, etOtp6);
        setupOtpInput(etOtp6, null);
    }
    private void setupOtpInput(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) next.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    private void setStatus(String message, String colorHex) {
        tvStatus.setText(message);
        tvStatus.setTextColor(android.graphics.Color.parseColor(colorHex));
        tvStatus.setVisibility(View.VISIBLE);
    }
    private void setStatus(String message) { setStatus(message, "#FF5722"); }
}