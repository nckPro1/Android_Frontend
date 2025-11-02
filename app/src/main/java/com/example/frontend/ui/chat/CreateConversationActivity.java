package com.example.frontend.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.ConversationDTO;
import com.example.frontend.model.CreateConversationRequest;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateConversationActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;
    private EditText etSubject;
    private EditText etFirstMessage;
    private Button btnCreate;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_conversation);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (!tokenManager.isLoggedIn()) {
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
            getSupportActionBar().setTitle("Tạo cuộc trò chuyện mới");
        }

        etSubject = findViewById(R.id.etSubject);
        etFirstMessage = findViewById(R.id.etFirstMessage);
        btnCreate = findViewById(R.id.btnCreate);
        progressBar = findViewById(R.id.progressBar);

        btnCreate.setOnClickListener(v -> createConversation());
    }

    private void createConversation() {
        String subject = etSubject.getText().toString().trim();
        String firstMessage = etFirstMessage.getText().toString().trim();

        if (subject.isEmpty()) {
            etSubject.setError("Vui lòng nhập chủ đề");
            return;
        }

        if (firstMessage.isEmpty()) {
            etFirstMessage.setError("Vui lòng nhập tin nhắn đầu tiên");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        CreateConversationRequest request = new CreateConversationRequest(subject, firstMessage);
        String token = "Bearer " + tokenManager.getAccessToken();

        apiService.createConversation(token, request).enqueue(new Callback<ApiResponse<ConversationDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ConversationDTO>> call, Response<ApiResponse<ConversationDTO>> response) {
                progressBar.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ConversationDTO conversation = response.body().getData();
                    Toast.makeText(CreateConversationActivity.this, "Tạo cuộc trò chuyện thành công", Toast.LENGTH_SHORT).show();
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("conversationId", conversation.getConversationId());
                    setResult(RESULT_OK, resultIntent);
                    
                    // Mở ChatActivity
                    Intent chatIntent = new Intent(CreateConversationActivity.this, ChatActivity.class);
                    chatIntent.putExtra("conversationId", conversation.getConversationId());
                    chatIntent.putExtra("subject", conversation.getSubject());
                    startActivity(chatIntent);
                    finish();
                } else {
                    Toast.makeText(CreateConversationActivity.this, "Lỗi khi tạo cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ConversationDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                Toast.makeText(CreateConversationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

