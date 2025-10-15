package com.example.frontend.ui.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Thêm các import cần thiết
import com.example.frontend.ui.auth.otp.OTPActivity;
import com.example.frontend.R;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.RegisterRequest;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etFullName;
    private Button btnFinish;
    private TextView tvBackToLogin, tvStatus;
    private ApiService apiService; // Retrofit ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ✅ KHỞI TẠO RETROFIT
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // ... giữ nguyên code của bạn ...
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        etFullName = findViewById(R.id.etRegisterFullName);
        btnFinish = findViewById(R.id.btnRegisterFinish);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvStatus = findViewById(R.id.tvRegisterStatus);
        tvStatus.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnFinish.setOnClickListener(v -> register());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        // Validation (giữ nguyên logic của bạn)
        if (!validateInput(email, password, confirmPassword, fullName)) {
            return;
        }

        setStatus("Đang gửi yêu cầu đăng ký...");
        btnFinish.setEnabled(false);

        // ✅ SỬ DỤNG RETROFIT ĐỂ GỌI API
        RegisterRequest request = new RegisterRequest(email, password, fullName);
        apiService.register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnFinish.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    handleRegisterResponse(response.body(), email);
                } else {
                    setStatus("❌ Lỗi server: " + response.code(), "#F44336");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnFinish.setEnabled(true);
                setStatus("❌ Lỗi mạng: " + t.getMessage(), "#F44336");
                Log.e("Register", "Network Failure", t);
            }
        });
    }

    private void handleRegisterResponse(ApiResponse apiResponse, String email) {
        if (apiResponse.isSuccess()) {
            setStatus("✅ " + apiResponse.getMessage(), "#4CAF50");
            Toast.makeText(this, "OTP đã được gửi đến email của bạn!", Toast.LENGTH_LONG).show();

            // Chuyển sang màn hình xác thực OTP
            Intent intent = new Intent(this, OTPActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish(); // Đóng màn hình đăng ký

        } else {
            setStatus("❌ " + apiResponse.getMessage(), "#F44336");
            Toast.makeText(this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInput(String email, String password, String confirmPassword, String fullName) {
        // ... Giữ nguyên toàn bộ logic validate của bạn ...
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ"); etEmail.requestFocus(); return false;
        }
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ tên"); etFullName.requestFocus(); return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự"); etPassword.requestFocus(); return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp"); etConfirmPassword.requestFocus(); return false;
        }
        return true;
    }

    private void setStatus(String message, String colorHex) {
        tvStatus.setText(message);
        tvStatus.setTextColor(android.graphics.Color.parseColor(colorHex));
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setStatus(String message) {
        setStatus(message, "#FF5722");
    }
}