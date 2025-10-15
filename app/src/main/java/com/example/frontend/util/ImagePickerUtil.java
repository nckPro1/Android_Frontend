package com.example.frontend.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ImagePickerUtil {

    public interface ImagePickerCallback {
        void onImageSelected(Uri imageUri);
        void onError(String message);
    }

    private Activity activity;
    private ImagePickerCallback callback;
    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private static final int PERMISSION_REQUEST_CODE = 1003;

    public ImagePickerUtil(Activity activity, ImagePickerCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void showImagePickerDialog() {
        String[] options = {"Chọn từ Gallery", "Chụp ảnh mới"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle("Chọn ảnh đại diện");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    checkPermissionAndOpenGallery();
                    break;
                case 1:
                    checkPermissionAndOpenCamera();
                    break;
            }
        });
        builder.show();
    }

    private void checkPermissionAndOpenGallery() {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            // Android 12 and below
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        boolean hasPermission = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }

        if (hasPermission) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    if (data != null && data.getData() != null) {
                        callback.onImageSelected(data.getData());
                    } else {
                        callback.onError("Không thể lấy ảnh từ gallery");
                    }
                    break;
                case CAMERA_REQUEST_CODE:
                    callback.onError("Camera chưa được implement đầy đủ");
                    break;
            }
        }
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permission granted, open gallery
                openGallery();
            } else {
                // Check if user denied permanently
                boolean shouldShowRationale = false;
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        shouldShowRationale = true;
                        break;
                    }
                }

                if (shouldShowRationale) {
                    callback.onError("Cần quyền truy cập để chọn ảnh. Vui lòng cấp quyền trong Settings.");
                } else {
                    callback.onError("Quyền truy cập bị từ chối. Vui lòng vào Settings để cấp quyền.");
                }
            }
        }
    }
}
