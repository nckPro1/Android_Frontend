package com.example.frontend.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.UserDto;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.util.ImagePickerUtil;
import com.example.frontend.util.ImageUploadUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;
    private ImagePickerUtil imagePickerUtil;

    // Views
    private ShapeableImageView imgAvatar;
    private TextView tvUserName, tvUserEmail, tvUserId;
    private TextView tvDisplayFullName, tvDisplayPhone, tvDisplayAddress, tvDisplayRole;
    private TextInputEditText etFullName, etPhoneNumber, etAddress;
    private MaterialButton btnSave, btnEdit;
    private ProgressBar progressBar, progressAvatar;
    private MaterialCardView cardProfileInfo, cardEditForm;

    private boolean isEditMode = false;
    private UserDto currentUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn sử dụng đúng layout cho màn hình chỉnh sửa.
        // Tên gợi ý: R.layout.activity_edit_profile
        setContentView(R.layout.activity_edit_profile); // <-- KIỂM TRA LẠI TÊN FILE LAYOUT NÀY

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Initialize image picker
        imagePickerUtil = new ImagePickerUtil(this, new ImagePickerUtil.ImagePickerCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {
                selectedImageUri = imageUri;
                Picasso.get().load(imageUri)
                        .transform(new com.squareup.picasso.CircleTransform())
                        .into(imgAvatar);
                uploadSelectedImage();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        setupViews();
        loadUserProfile();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views from your edit layout
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
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit button clicked!", Toast.LENGTH_SHORT).show();
            toggleEditMode();
        });
        btnSave.setOnClickListener(v -> saveProfile());
        imgAvatar.setOnClickListener(v -> imagePickerUtil.showImagePickerDialog());
    }

    private void loadUserProfile() {
        showLoading(true);
        Call<UserDto> call = apiService.getUserProfile("Bearer " + tokenManager.getAccessToken());
        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayUserInfo(currentUser);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(UserDto user) {
        tvUserName.setText(user.getFullName());
        tvUserEmail.setText(user.getEmail());
        tvUserId.setText("ID: " + user.getUserId());

        tvDisplayFullName.setText(user.getFullName());
        tvDisplayPhone.setText(user.getPhoneNumber());
        tvDisplayAddress.setText(user.getAddress());
        tvDisplayRole.setText(user.getRoleId() != null && user.getRoleId() == 2 ? "Quản trị viên" : "Người dùng");

        etFullName.setText(user.getFullName());
        etPhoneNumber.setText(user.getPhoneNumber());
        etAddress.setText(user.getAddress());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String avatarUrl = user.getAvatarUrl().startsWith("/") ? ApiClient.BASE_URL + user.getAvatarUrl() : user.getAvatarUrl();
            Picasso.get().load(avatarUrl)
                    .transform(new com.squareup.picasso.CircleTransform())
                    .into(imgAvatar);
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        if (isEditMode) {
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            cardProfileInfo.setVisibility(View.GONE);
            cardEditForm.setVisibility(View.VISIBLE);
        } else {
            // Logic to cancel (can be implemented if needed)
            btnEdit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            cardProfileInfo.setVisibility(View.VISIBLE);
            cardEditForm.setVisibility(View.GONE);
        }
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên không được để trống");
            return;
        }

        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(etPhoneNumber.getText().toString().trim());
        currentUser.setAddress(etAddress.getText().toString().trim());

        showLoading(true);
        Call<UserDto> call = apiService.updateUserProfile("Bearer " + tokenManager.getAccessToken(), currentUser);
        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    tokenManager.saveUserName(currentUser.getFullName());
                    displayUserInfo(currentUser);
                    toggleEditMode();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadSelectedImage() {
        if (selectedImageUri == null) return;
        showAvatarLoading(true);
        ImageUploadUtil.uploadImage(this, selectedImageUri, tokenManager.getAccessToken(), new ImageUploadUtil.UploadCallback() {
            @Override
            public void onUploadSuccess(String imageUrl) {
                showAvatarLoading(false);
                Toast.makeText(EditProfileActivity.this, "Upload avatar thành công!", Toast.LENGTH_SHORT).show();
                currentUser.setAvatarUrl(imageUrl);
                // Optionally, save this new URL to the backend immediately
            }
            @Override
            public void onUploadError(String error) {
                showAvatarLoading(false);
                Toast.makeText(EditProfileActivity.this, "Upload thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAvatarLoading(boolean show) {
        progressAvatar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // You might not need these if using the newer ActivityResultContracts API in ImagePickerUtil
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerUtil.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePickerUtil.handlePermissionResult(requestCode, permissions, grantResults);
    }
}