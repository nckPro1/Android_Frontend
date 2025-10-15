package com.example.frontend.ui.auth.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.util.GoogleSignInHelper;
import com.example.frontend.ui.home.HomeActivity;
import com.example.frontend.R;
import com.example.frontend.ui.auth.register.RegisterActivity;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.AuthResponse;
import com.example.frontend.model.GoogleLoginRequest;
import com.example.frontend.model.LoginRequest;
import com.example.frontend.model.UserDto;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Thêm TAG để lọc log trong Logcat dễ dàng hơn
    private static final String TAG = "MainActivity_DEBUG";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButton btnLoginGoogle;
    private TextView tvStatus, tvCreateAccount;

    private TokenManager tokenManager;
    private ApiService apiService;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private GoogleSignInHelper googleSignInHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);

        if (tokenManager.isLoggedIn()) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();

        apiService = ApiClient.getClient().create(ApiService.class);
        googleSignInHelper = new GoogleSignInHelper(this);

        setupGoogleSignInLauncher();
        setupClickListeners();
        handleRegistrationSuccess();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatus.setVisibility(View.GONE);
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Log.d(TAG, "Google Sign-In Activity thành công. Đang xử lý kết quả...");
                        googleSignInHelper.handleSignInResult(result.getData(),
                                new GoogleSignInHelper.OnSignInListener() {
                                    @Override
                                    public void onSignInSuccess(String idToken, String email, String name, String photoUrl) {
                                        Log.i(TAG, "✅ GIAI ĐOẠN 1 THÀNH CÔNG: Lấy được ID Token từ Google.");
                                        sendIdTokenToBackend(idToken);
                                    }

                                    @Override
                                    public void onSignInFailed(String error) {
                                        Log.e(TAG, "❌ GIAI ĐOẠN 1 THẤT BẠI: Lỗi từ GoogleSignInHelper: " + error);
                                        setStatus("❌ Lỗi từ Google: " + error, "#F44336");
                                    }
                                });
                    } else {
                        Log.w(TAG, "Người dùng đã hủy đăng nhập Google.");
                        setStatus("❌ Đăng nhập Google đã bị hủy.", "#F44336");
                    }
                });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> login());
        tvCreateAccount.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnLoginGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    // --- LOGIC ĐĂNG NHẬP THƯỜNG ---
    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        setStatus("Đang đăng nhập...");
        setButtonsEnabled(false);

        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setButtonsEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    handleAuthResponse(response.body());
                } else {
                    setStatus("❌ Email hoặc mật khẩu không đúng.", "#F44336");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setButtonsEnabled(true);
                setStatus("❌ Lỗi mạng: " + t.getMessage(), "#F44336");
                Log.e("LoginApi", "Network Failure", t);
            }
        });
    }

    // --- LOGIC ĐĂNG NHẬP GOOGLE ---
    private void signInWithGoogle() {
        Log.d(TAG, "Bắt đầu luồng đăng nhập Google...");

        // Dòng code debug: Ép Google phải thực hiện đăng nhập mới hoàn toàn mỗi lần.
        // Sau khi sửa lỗi xong, bạn có thể xóa 2 dòng này đi.
        googleSignInHelper.signOut();
        googleSignInHelper.revokeAccess();

        setStatus("Đang kết nối với Google...");
        Intent signInIntent = googleSignInHelper.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void sendIdTokenToBackend(String idToken) {
        Log.d(TAG, "GIAI ĐOẠN 2: Đang gửi ID Token đến Backend...");
        setStatus("Đang xác thực với server...");
        setButtonsEnabled(false);

        GoogleLoginRequest request = new GoogleLoginRequest(idToken);
        apiService.loginWithGoogle(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setButtonsEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "✅ GIAI ĐOẠN 3 THÀNH CÔNG: Backend xác thực thành công.");
                    handleAuthResponse(response.body());
                } else {
                    // Cố gắng đọc thông báo lỗi cụ thể từ server
                    String errorMessage = "Lỗi không xác định từ server.";
                    if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            JsonObject jsonObject = JsonParser.parseString(errorJson).getAsJsonObject();
                            if (jsonObject.has("message")) {
                                errorMessage = jsonObject.get("message").getAsString();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Không thể phân tích error body", e);
                        }
                    }
                    Log.e(TAG, "❌ GIAI ĐOẠN 3 THẤT BẠI: Lỗi từ Backend. Code: " + response.code() + " | Message: " + errorMessage);
                    setStatus("❌ " + errorMessage + " (Code: " + response.code() + ")", "#F44336");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setButtonsEnabled(true);
                Log.e(TAG, "❌ GIAI ĐOẠN 2 THẤT BẠI: Lỗi kết nối mạng.", t);
                setStatus("❌ Lỗi kết nối: " + t.getMessage(), "#F44336");
            }
        });
    }

    // --- XỬ LÝ CHUNG KẾT QUẢ ĐĂNG NHẬP ---
    private void handleAuthResponse(AuthResponse authResponse) {
        if (authResponse.isSuccess()) {
            setStatus("✅ " + authResponse.getMessage(), "#4CAF50");

            tokenManager.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
            UserDto user = authResponse.getUser();
            if (user != null) {
                tokenManager.saveUserInfo(user.getUserId(), user.getEmail(), user.getFullName());
            }

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            goToHome();
        } else {
            setStatus("❌ " + authResponse.getMessage(), "#F44336");
            Toast.makeText(this, authResponse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- CÁC HÀM TIỆN ÍCH ---
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setStatus(String message, String colorHex) {
        tvStatus.setText(message);
        tvStatus.setTextColor(android.graphics.Color.parseColor(colorHex));
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setStatus(String message) {
        setStatus(message, "#FF5722");
    }

    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnLoginGoogle.setEnabled(enabled);
    }

    private void handleRegistrationSuccess() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("registrationSuccess", false)) {
            String registeredEmail = intent.getStringExtra("registeredEmail");
            if (registeredEmail != null) {
                etEmail.setText(registeredEmail);
                etPassword.requestFocus();
            }
            setStatus("✅ Đăng ký thành công! Vui lòng đăng nhập.", "#4CAF50");
        }
    }
}