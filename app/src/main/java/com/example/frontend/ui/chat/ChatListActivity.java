package com.example.frontend.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.ConversationDTO;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.adapter.ConversationAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService apiService;
    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabNewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (!tokenManager.isLoggedIn()) {
            finish();
            return;
        }

        setupViews();
        loadConversations();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        rvConversations = findViewById(R.id.rvConversations);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabNewChat = findViewById(R.id.fabNewChat);

        adapter = new ConversationAdapter(new ArrayList<>(), conversation -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("conversationId", conversation.getConversationId());
            intent.putExtra("subject", conversation.getSubject());
            startActivity(intent);
        });

        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);

        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(ChatListActivity.this, CreateConversationActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void loadConversations() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        String token = "Bearer " + tokenManager.getAccessToken();
        android.util.Log.d("ChatListActivity", "Loading conversations with token: " + (token != null ? "present" : "null"));

        apiService.getUserConversations(token).enqueue(new Callback<ApiResponse<List<ConversationDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ConversationDTO>>> call, Response<ApiResponse<List<ConversationDTO>>> response) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.d("ChatListActivity", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("ChatListActivity", "Response success: " + response.body().isSuccess());
                    if (response.body().isSuccess()) {
                        List<ConversationDTO> conversations = response.body().getData();
                        android.util.Log.d("ChatListActivity", "Conversations count: " + (conversations != null ? conversations.size() : 0));
                        if (conversations != null && !conversations.isEmpty()) {
                            adapter.updateConversations(conversations);
                            tvEmpty.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                            adapter.updateConversations(new ArrayList<>());
                        }
                    } else {
                        String errorMsg = response.body().getMessage();
                        android.util.Log.e("ChatListActivity", "API Error: " + errorMsg);
                        Toast.makeText(ChatListActivity.this, "Lỗi: " + (errorMsg != null ? errorMsg : "Không thể tải danh sách chat"), Toast.LENGTH_SHORT).show();
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    android.util.Log.e("ChatListActivity", "Response not successful. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ChatListActivity", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ChatListActivity", "Error reading error body", e);
                    }
                    Toast.makeText(ChatListActivity.this, "Lỗi khi tải danh sách chat (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ConversationDTO>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("ChatListActivity", "Network error", t);
                Toast.makeText(ChatListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadConversations();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }
}

