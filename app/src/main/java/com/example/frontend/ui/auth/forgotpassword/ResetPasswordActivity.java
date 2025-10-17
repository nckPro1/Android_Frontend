package com.example.frontend.ui.auth.forgotpassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.R;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.ResetPasswordRequest;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private ApiService apiService;
    private String email;

    // Views
    private TextInputEditText etOTP, etNewPassword, etConfirmPassword;
    private TextInputLayout tilOTP, tilNewPassword, tilConfirmPassword;
    private MaterialButton btnResetPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views
        etOTP = findViewById(R.id.etOTP);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilOTP = findViewById(R.id.tilOTP);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String otp = etOTP.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (otp.isEmpty()) {
            tilOTP.setError("Mã OTP không được để trống");
            return;
        }
        tilOTP.setError(null);

        if (otp.length() != 6) {
            tilOTP.setError("Mã OTP phải có 6 chữ số");
            return;
        }
        tilOTP.setError(null);

        if (newPassword.isEmpty()) {
            tilNewPassword.setError("Mật khẩu mới không được để trống");
            return;
        }
        tilNewPassword.setError(null);

        if (newPassword.length() < 6) {
            tilNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }
        tilNewPassword.setError(null);

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
            return;
        }
        tilConfirmPassword.setError(null);

        if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }
        tilConfirmPassword.setError(null);

        // Show loading
        showLoading(true);
        btnResetPassword.setEnabled(false);

        // Create request
        ResetPasswordRequest request = new ResetPasswordRequest(email, otp, newPassword, confirmPassword);

        // Call API
        Call<ApiResponse> call = apiService.resetPassword(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showLoading(false);
                btnResetPassword.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        // Navigate back to login
                        Intent intent = new Intent(ResetPasswordActivity.this, com.example.frontend.ui.auth.login.LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showLoading(false);
                btnResetPassword.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnResetPassword.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
