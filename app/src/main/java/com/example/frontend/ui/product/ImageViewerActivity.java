package com.example.frontend.ui.product;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend.R;
import com.example.frontend.util.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView ivFullscreenImage;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Get image URL from intent
        imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl == null) {
            finish();
            return;
        }

        setupViews();
        loadImage();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivFullscreenImage = findViewById(R.id.ivFullscreenImage);

        // Click to close
        ivFullscreenImage.setOnClickListener(v -> finish());
    }

    private void loadImage() {
        String fullImageUrl = ImageUrlBuilder.buildFullUrl(imageUrl);
        Picasso.get()
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_food_placeholder)
                .error(R.drawable.ic_food_placeholder)
                .into(ivFullscreenImage);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
