package com.example.frontend.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;
import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.UserDto;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.util.ImagePickerUtil;
import com.example.frontend.util.ImageUploadUtil;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;
    private ImagePickerUtil imagePickerUtil;

    // Views
    private ImageView imgAvatar;
    private TextView tvUserName, tvUserEmail, tvUserId;
    private TextView tvDisplayFullName, tvDisplayPhone, tvDisplayAddress, tvDisplayRole;
    private EditText etFullName, etPhoneNumber, etAddress;
    private Button btnSave, btnEdit;
    private ProgressBar progressBar, progressAvatar;
    private MaterialCardView cardProfileInfo, cardEditForm;

    private boolean isEditMode = false;
    private UserDto currentUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Initialize image picker
        imagePickerUtil = new ImagePickerUtil(this, new ImagePickerUtil.ImagePickerCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {
                selectedImageUri = imageUri;
                // Load image preview
                Picasso.get()
                        .load(imageUri)
                        .transform(new com.squareup.picasso.CircleTransform())
                        .into(imgAvatar);

                // Upload image
                uploadSelectedImage();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Check if user is logged in
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        setupViews();
        loadUserProfile();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hồ sơ cá nhân");
        }

        // Initialize views
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserId = findViewById(R.id.tvUserId);
        tvDisplayFullName = findViewById(R.id.tvDisplayFullName);
        tvDisplayPhone = findViewById(R.id.tvDisplayPhone);
        tvDisplayAddress = findViewById(R.id.tvDisplayAddress);
        tvDisplayRole = findViewById(R.id.tvDisplayRole);
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        progressBar = findViewById(R.id.progressBar);
        progressAvatar = findViewById(R.id.progressAvatar);
        cardProfileInfo = findViewById(R.id.cardProfileInfo);
        cardEditForm = findViewById(R.id.cardEditForm);

        // Set click listeners
        btnEdit.setOnClickListener(v -> toggleEditMode());
        btnSave.setOnClickListener(v -> saveProfile());

        // Avatar click to change
        imgAvatar.setOnClickListener(v -> {
            imagePickerUtil.showImagePickerDialog();
        });
    }

    private void loadUserProfile() {
        showLoading(true);

        String token = tokenManager.getAccessToken();
        Call<UserDto> call = apiService.getUserProfile("Bearer " + token);

        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayUserInfo(currentUser);
                } else {
                    Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    // Fallback to local data
                    loadLocalUserInfo();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Fallback to local data
                loadLocalUserInfo();
            }
        });
    }

    private void loadLocalUserInfo() {
        // Create a basic user object from local data
        currentUser = new UserDto();
        currentUser.setUserId(1L); // Default ID
        currentUser.setEmail(tokenManager.getUserEmail());
        currentUser.setFullName(tokenManager.getUserName());
        currentUser.setPhoneNumber("");
        currentUser.setAddress("");
        currentUser.setAvatarUrl("");
        currentUser.setRoleId(1);

        displayUserInfo(currentUser);
    }

    private void displayUserInfo(UserDto user) {
        tvUserName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tvUserId.setText("ID: " + (user.getUserId() != null ? user.getUserId().toString() : "N/A"));

        // Display info in profile card
        tvDisplayFullName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
        tvDisplayPhone.setText(user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() ? user.getPhoneNumber() : "Chưa cập nhật");
        tvDisplayAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Chưa cập nhật");

        // Set role text
        String roleText = user.getRoleId() != null && user.getRoleId() == 1 ? "Người dùng" : "Quản trị viên";
        tvDisplayRole.setText(roleText);

        // Set edit form values
        etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        etPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        etAddress.setText(user.getAddress() != null ? user.getAddress() : "");

        // Set avatar
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String avatarUrl = user.getAvatarUrl();

            // Nếu URL bắt đầu bằng "/", thêm base URL
            if (avatarUrl.startsWith("/")) {
                avatarUrl = ApiClient.BASE_URL + avatarUrl;
            }

            // Debug log
            final String finalAvatarUrl = avatarUrl;
            android.util.Log.d("ProfileActivity", "Loading avatar: " + finalAvatarUrl);

            Picasso.get()
                    .load(finalAvatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .transform(new com.squareup.picasso.CircleTransform())
                    .into(imgAvatar, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            android.util.Log.d("ProfileActivity", "Successfully loaded avatar: " + finalAvatarUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            android.util.Log.e("ProfileActivity", "Failed to load avatar: " + finalAvatarUrl, e);
                        }
                    });
        } else {
            imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            btnEdit.setText("Hủy");
            btnSave.setVisibility(View.VISIBLE);
            cardEditForm.setVisibility(View.VISIBLE);
            cardProfileInfo.setVisibility(View.GONE);

            // Enable editing
            etFullName.setEnabled(true);
            etPhoneNumber.setEnabled(true);
            etAddress.setEnabled(true);
        } else {
            btnEdit.setText("Chỉnh sửa");
            btnSave.setVisibility(View.GONE);
            cardEditForm.setVisibility(View.GONE);
            cardProfileInfo.setVisibility(View.VISIBLE);

            // Disable editing and reset values
            etFullName.setEnabled(false);
            etPhoneNumber.setEnabled(false);
            etAddress.setEnabled(false);

            // Reset to original values
            if (currentUser != null) {
                etFullName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
                etPhoneNumber.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
                etAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
            }
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        // Validate input
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setAddress(address);

        showLoading(true);

        String token = tokenManager.getAccessToken();
        Call<UserDto> call = apiService.updateUserProfile("Bearer " + token, currentUser);

        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    toggleEditMode(); // Exit edit mode
                    displayUserInfo(currentUser);

                    // Update local token manager with new name
                    tokenManager.saveUserName(currentUser.getFullName());
                } else {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadSelectedImage() {
        if (selectedImageUri == null) return;

        showAvatarLoading(true);
        String token = tokenManager.getAccessToken();

        ImageUploadUtil.uploadImage(this, selectedImageUri, token, new ImageUploadUtil.UploadCallback() {
            @Override
            public void onUploadSuccess(String imageUrl) {
                showAvatarLoading(false);
                Toast.makeText(ProfileActivity.this, "Upload avatar thành công!", Toast.LENGTH_SHORT).show();

                // Debug log
                android.util.Log.d("ProfileActivity", "Upload success, received URL: " + imageUrl);

                // Update current user with new avatar URL
                if (currentUser != null) {
                    currentUser.setAvatarUrl(imageUrl);
                    android.util.Log.d("ProfileActivity", "Updated user avatar URL: " + currentUser.getAvatarUrl());
                    displayUserInfo(currentUser);
                }
            }

            @Override
            public void onUploadError(String error) {
                showAvatarLoading(false);
                Toast.makeText(ProfileActivity.this, "Upload thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAvatarLoading(boolean show) {
        progressAvatar.setVisibility(show ? View.VISIBLE : View.GONE);
        imgAvatar.setAlpha(show ? 0.5f : 1.0f);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnEdit.setEnabled(!show);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imagePickerUtil != null) {
            imagePickerUtil.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (imagePickerUtil != null) {
            imagePickerUtil.handlePermissionResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, com.example.frontend.ui.auth.login.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
