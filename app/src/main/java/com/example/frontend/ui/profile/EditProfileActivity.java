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
import com.example.frontend.util.CircleTransform;
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
    private TextView tvUserName, tvUserEmail;
    private TextView tvDisplayFullName, tvDisplayPhone;
    private TextInputEditText etFullName, etPhoneNumber;
    private MaterialButton btnSave, btnEdit;
    private ProgressBar progressBar, progressAvatar;
    private MaterialCardView cardProfileInfo, cardEditForm;

    private boolean isEditMode = false;
    private UserDto currentUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Initialize image picker
        imagePickerUtil = new ImagePickerUtil(this, new ImagePickerUtil.ImagePickerCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {
                selectedImageUri = imageUri;
                Picasso.get().load(imageUri)
                        .transform(new CircleTransform()) // Use the correct CircleTransform
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

        // Initialize views
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvDisplayFullName = findViewById(R.id.tvDisplayFullName);
        tvDisplayPhone = findViewById(R.id.tvDisplayPhone);
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        progressBar = findViewById(R.id.progressBar);
        progressAvatar = findViewById(R.id.progressAvatar);
        cardProfileInfo = findViewById(R.id.cardProfileInfo);
        cardEditForm = findViewById(R.id.cardEditForm);

        // Set click listeners
        btnEdit.setOnClickListener(v -> toggleEditMode());
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

        tvDisplayFullName.setText(user.getFullName());
        tvDisplayPhone.setText(user.getPhoneNumber());

        etFullName.setText(user.getFullName());
        etPhoneNumber.setText(user.getPhoneNumber());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            // The URL from backend might be relative, so construct the full URL
            String avatarUrl = user.getAvatarUrl();
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = ApiClient.BASE_URL + (avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl);
            }
            Picasso.get().load(avatarUrl)
                    .transform(new CircleTransform()) // Use the correct CircleTransform
                    .placeholder(R.drawable.ic_default_avatar) // Add a placeholder
                    .error(R.drawable.ic_default_avatar)       // Add an error image
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
            // Logic to cancel
            btnEdit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            cardProfileInfo.setVisibility(View.VISIBLE);
            cardEditForm.setVisibility(View.GONE);
            // Reset fields to original values
            displayUserInfo(currentUser);
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
                    toggleEditMode(); // Switch back to display mode
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
                // Immediately save the new avatar URL to the user's profile on the server
                saveProfile();
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