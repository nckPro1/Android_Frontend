package com.example.frontend.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.frontend.R;
import com.example.frontend.local.TokenManager;
import com.example.frontend.model.ApiResponse;
import com.example.frontend.model.MessageDTO;
import com.example.frontend.model.SendMessageRequest;
import com.example.frontend.remote.ApiClient;
import com.example.frontend.remote.ApiService;
import com.example.frontend.ui.adapter.MessageAdapter;
import com.example.frontend.util.ImageUrlBuilder;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private TokenManager tokenManager;
    private ApiService apiService;
    private Long conversationId;
    private Long currentUserId;

    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private TextView tvSubject;
    private ImageView ivAvatar;

    private DatabaseReference messagesRef;
    private ValueEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== onCreate STARTED ===");
        setContentView(R.layout.activity_chat);
        Log.d(TAG, "setContentView completed");

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        currentUserId = tokenManager.getUserId();
        Log.d(TAG, "Services initialized. Current userId: " + currentUserId);

        conversationId = getIntent().getLongExtra("conversationId", -1);
        Log.d(TAG, "ConversationId from intent: " + conversationId);
        if (conversationId == -1) {
            Log.e(TAG, "ERROR: ConversationId is -1, finishing activity");
            Toast.makeText(this, "Lỗi: Không tìm thấy conversation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!tokenManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, finishing activity");
            finish();
            return;
        }

        Log.d(TAG, "Setting up views...");
        setupViews();
        Log.d(TAG, "Views setup completed");

        // Load messages first, then setup Firebase listener
        // Use a flag to prevent Firebase from clearing API messages on first load
        Log.d(TAG, "Starting to load messages...");
        loadMessages();
        Log.d(TAG, "Load messages called, will setup Firebase listener in 500ms");
        // Delay Firebase listener setup slightly to ensure API messages are loaded first
        rvMessages.postDelayed(() -> {
            Log.d(TAG, "Delayed setup: Starting Firebase listener setup");
            setupFirebaseListener();
            Log.d(TAG, "Firebase listener setup completed");
        }, 500);
        Log.d(TAG, "=== onCreate COMPLETED ===");
    }

    private void setupViews() {
        Log.d(TAG, "setupViews() called");
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            Log.d(TAG, "Toolbar found: " + (toolbar != null));
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            ImageView ivBack = findViewById(R.id.ivBack);
            Log.d(TAG, "Back button found: " + (ivBack != null));
            if (ivBack != null) {
                ivBack.setOnClickListener(v -> finish());
            }

            String subject = getIntent().getStringExtra("subject");
            tvSubject = findViewById(R.id.tvSubject);
            Log.d(TAG, "Subject TextView found: " + (tvSubject != null));
            if (subject != null && !subject.isEmpty()) {
                tvSubject.setText(subject);
            } else {
                tvSubject.setText("Chat với Admin");
            }

            ivAvatar = findViewById(R.id.ivAvatar);
            Log.d(TAG, "Avatar ImageView found: " + (ivAvatar != null));
            if (ivAvatar != null) {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            rvMessages = findViewById(R.id.rvMessages);
            etMessage = findViewById(R.id.etMessage);
            btnSend = findViewById(R.id.btnSend);
            progressBar = findViewById(R.id.progressBar);

            Log.d(TAG, "Views found - rvMessages: " + (rvMessages != null) +
                    ", etMessage: " + (etMessage != null) +
                    ", btnSend: " + (btnSend != null) +
                    ", progressBar: " + (progressBar != null));

            if (rvMessages == null) {
                Log.e(TAG, "ERROR: RecyclerView is null!");
                return;
            }

            messageAdapter = new MessageAdapter(new ArrayList<>());
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(false); // Start from top
            layoutManager.setReverseLayout(false);
            rvMessages.setLayoutManager(layoutManager);
            rvMessages.setHasFixedSize(false);
            rvMessages.setAdapter(messageAdapter);
            rvMessages.setVisibility(View.VISIBLE);

            Log.d(TAG, "RecyclerView setup complete. Initial adapter count: " + messageAdapter.getItemCount());

            // Check RecyclerView dimensions after layout
            rvMessages.post(() -> {
                Log.d(TAG, "RecyclerView dimensions - Width: " + rvMessages.getWidth() + ", Height: " + rvMessages.getHeight());
                Log.d(TAG, "RecyclerView visibility: " + (rvMessages.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
            });

            if (btnSend != null) {
                btnSend.setOnClickListener(v -> sendMessage());
            } else {
                Log.e(TAG, "ERROR: btnSend is null!");
            }

            // Scroll to bottom when new message arrives
            if (messageAdapter != null && rvMessages != null) {
                messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                });
                Log.d(TAG, "Adapter data observer registered");
            }

            Log.d(TAG, "setupViews() completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "ERROR in setupViews(): " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + tokenManager.getAccessToken();

        Log.d(TAG, "Loading messages for conversationId: " + conversationId);
        Log.d(TAG, "Token present: " + (token != null && !token.isEmpty()));

        apiService.getMessages(token, conversationId).enqueue(new Callback<ApiResponse<List<MessageDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MessageDTO>>> call, Response<ApiResponse<List<MessageDTO>>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Response success: " + response.body().isSuccess());
                    if (response.body().isSuccess()) {
                        List<MessageDTO> history = response.body().getData();
                        if (history != null && !history.isEmpty()) {
                            Log.d(TAG, "Loaded " + history.size() + " messages from API");
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                history.sort(Comparator.comparing(MessageDTO::getMessageId));
                            }

                            // Ensure createdAt and isOwnMessage are set correctly
                            for (MessageDTO msg : history) {
                                if (msg.getCreatedAt() == null) {
                                    Log.w(TAG, "Message " + msg.getMessageId() + " has null createdAt, using now");
                                    msg.setCreatedAt(LocalDateTime.now());
                                } else {
                                    Log.d(TAG, "Message " + msg.getMessageId() + " createdAt: " + msg.getCreatedAt());
                                }
                                if (msg.getIsOwnMessage() == null && currentUserId != null) {
                                    msg.setIsOwnMessage(msg.getSenderUserId() != null && msg.getSenderUserId().equals(currentUserId));
                                }
                            }

                            messageAdapter.updateMessages(history);
                            Log.d(TAG, "Adapter updated with " + messageAdapter.getItemCount() + " messages");

                            // Force adapter to notify and check visibility
                            rvMessages.post(() -> {
                                Log.d(TAG, "RecyclerView visibility: " + (rvMessages.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE"));
                                Log.d(TAG, "RecyclerView height: " + rvMessages.getHeight());
                                Log.d(TAG, "Adapter item count: " + messageAdapter.getItemCount());

                                // Log first message details
                                if (!history.isEmpty()) {
                                    MessageDTO firstMsg = history.get(0);
                                    Log.d(TAG, "First message: ID=" + firstMsg.getMessageId() +
                                            ", Content=" + firstMsg.getContent() +
                                            ", createdAt=" + firstMsg.getCreatedAt() +
                                            ", isOwnMessage=" + firstMsg.getIsOwnMessage());
                                }

                                if (messageAdapter.getItemCount() > 0) {
                                    rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                                    // Also try smooth scroll
                                    rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                                }
                            });
                        }
                        markAsRead();
                    } else {
                        String errorMsg = response.body().getMessage();
                        Log.e(TAG, "API Error: " + errorMsg);
                        Toast.makeText(ChatActivity.this, "Lỗi: " + (errorMsg != null ? errorMsg : "Không thể tải tin nhắn"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(ChatActivity.this, "Lỗi khi tải tin nhắn (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MessageDTO>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error loading messages", t);
                Toast.makeText(ChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        etMessage.setText("");
        btnSend.setEnabled(false);

        SendMessageRequest request = new SendMessageRequest(content);
        String token = "Bearer " + tokenManager.getAccessToken();

        apiService.sendMessage(token, conversationId, request).enqueue(new Callback<ApiResponse<MessageDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<MessageDTO>> call, Response<ApiResponse<MessageDTO>> response) {
                btnSend.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    MessageDTO message = response.body().getData();
                    // Gửi message lên Firebase để real-time sync
                    sendMessageToFirebase(message);
                } else {
                    Toast.makeText(ChatActivity.this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MessageDTO>> call, Throwable t) {
                btnSend.setEnabled(true);
                Toast.makeText(ChatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessageToFirebase(MessageDTO message) {
        // Lưu message vào Firebase để real-time sync
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://foodapp-4da5f-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference firebaseRef = database
                .getReference("conversations")
                .child(String.valueOf(conversationId))
                .child("messages")
                .child(String.valueOf(message.getMessageId()));

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", message.getMessageId());
        messageData.put("content", message.getContent());
        messageData.put("senderUserId", message.getSenderUserId());
        messageData.put("senderName", message.getSenderName());

        // Use createdAt from message if available, otherwise use current time
        long timestamp;
        if (message.getCreatedAt() != null) {
            // Convert LocalDateTime to milliseconds timestamp
            java.time.ZonedDateTime zonedDateTime = message.getCreatedAt().atZone(java.time.ZoneId.systemDefault());
            timestamp = zonedDateTime.toInstant().toEpochMilli();
        } else {
            timestamp = System.currentTimeMillis();
        }
        messageData.put("createdAt", timestamp);
        messageData.put("isOwnMessage", message.getIsOwnMessage());

        firebaseRef.setValue(messageData);
        Log.d(TAG, "Message sent to Firebase: " + message.getMessageId() + " with timestamp: " + timestamp);
    }

    private void setupFirebaseListener() {
        // Listen to Firebase for real-time messages
        // Use the Firebase Realtime Database URL from Firebase Console
        FirebaseDatabase database;
        try {
            String firebaseUrl = "https://foodapp-4da5f-default-rtdb.asia-southeast1.firebasedatabase.app/";
            database = FirebaseDatabase.getInstance(firebaseUrl);
            Log.d(TAG, "Firebase Database instance created with URL: " + firebaseUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error getting Firebase Database instance: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi: Không thể kết nối Firebase Database", Toast.LENGTH_LONG).show();
            return;
        }

        // Enable offline persistence
        database.setPersistenceEnabled(true);
        Log.d(TAG, "Firebase persistence enabled");

        // Check connection status
        DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "Firebase connected: " + connected);
                if (!connected) {
                    Log.w(TAG, "Firebase disconnected - will use polling fallback");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase connection check cancelled", error.toException());
            }
        });

        messagesRef = database.getReference("conversations")
                .child(String.valueOf(conversationId))
                .child("messages");

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Firebase onDataChange triggered. Has children: " + snapshot.hasChildren());

                Map<Long, MessageDTO> messageMap = new ConcurrentHashMap<>();

                // 1. Add existing messages from adapter to map first (preserve API messages)
                List<MessageDTO> existingMessages = messageAdapter.getMessages();
                Log.d(TAG, "Existing messages in adapter: " + existingMessages.size());
                for (MessageDTO msg : existingMessages) {
                    if (msg != null && msg.getMessageId() != null) {
                        messageMap.put(msg.getMessageId(), msg);
                    }
                }

                // 2. Add/update with messages from Firebase (only if Firebase has data)
                if (snapshot.hasChildren()) {
                    Log.d(TAG, "Firebase has " + snapshot.getChildrenCount() + " messages");
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        try {
                            MessageDTO fbMessage = new MessageDTO();

                            Long messageId = messageSnapshot.child("messageId").getValue(Long.class);
                            String content = messageSnapshot.child("content").getValue(String.class);
                            Long senderUserId = messageSnapshot.child("senderUserId").getValue(Long.class);
                            String senderName = messageSnapshot.child("senderName").getValue(String.class);

                            if (content != null && senderUserId != null && messageId != null) {
                                fbMessage.setMessageId(messageId);
                                fbMessage.setContent(content);
                                fbMessage.setSenderUserId(senderUserId);
                                fbMessage.setSenderName(senderName);

                                if (currentUserId != null) {
                                    fbMessage.setIsOwnMessage(senderUserId.equals(currentUserId));
                                }

                                // Check if createdAt exists in Firebase
                                Object createdAtObj = messageSnapshot.child("createdAt").getValue();
                                if (createdAtObj != null) {
                                    // Handle different timestamp formats
                                    if (createdAtObj instanceof Long) {
                                        // Convert timestamp (milliseconds) to LocalDateTime
                                        long timestamp = (Long) createdAtObj;
                                        java.time.Instant instant = java.time.Instant.ofEpochMilli(timestamp);
                                        LocalDateTime createdAt = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                                        fbMessage.setCreatedAt(createdAt);
                                        Log.d(TAG, "Firebase createdAt converted: " + createdAt);
                                    } else if (createdAtObj instanceof String) {
                                        // Try to parse as ISO string
                                        try {
                                            LocalDateTime createdAt = LocalDateTime.parse((String) createdAtObj, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                            fbMessage.setCreatedAt(createdAt);
                                            Log.d(TAG, "Firebase createdAt parsed from string: " + createdAt);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Failed to parse createdAt string", e);
                                            fbMessage.setCreatedAt(LocalDateTime.now());
                                        }
                                    }
                                } else {
                                    // If no createdAt in Firebase, use current time
                                    fbMessage.setCreatedAt(LocalDateTime.now());
                                    Log.d(TAG, "Firebase message has no createdAt, using now");
                                }

                                // Put into map to handle duplicates (will override if exists)
                                messageMap.put(fbMessage.getMessageId(), fbMessage);
                                Log.d(TAG, "Added Firebase message: " + messageId + " - " + content);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing message from Firebase", e);
                        }
                    }
                } else {
                    Log.d(TAG, "Firebase has no messages, keeping existing API messages");
                }

                // 3. Only update adapter if we have messages or if Firebase added new ones
                if (!messageMap.isEmpty()) {
                    List<MessageDTO> combinedMessages = new ArrayList<>(messageMap.values());
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        combinedMessages.sort(Comparator.comparing(MessageDTO::getMessageId));
                    }

                    Log.d(TAG, "Updating adapter with " + combinedMessages.size() + " combined messages.");
                    messageAdapter.updateMessages(combinedMessages);
                    if (messageAdapter.getItemCount() > 0) {
                        rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                } else {
                    Log.d(TAG, "No messages to display, keeping current state");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled", error.toException());
                Toast.makeText(ChatActivity.this, "Firebase connection error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        messagesRef.addValueEventListener(messagesListener);
        Log.d(TAG, "Firebase listener added for conversation: " + conversationId);

        // Also add polling as fallback for private networks
        startPollingMessages();
    }

    private static final long POLLING_INTERVAL = 10000; // 10 seconds (giảm tải server)
    private Handler pollingHandler;
    private Runnable pollingRunnable;

    private boolean pollingStarted = false;

    private void startPollingMessages() {
        if (pollingStarted) {
            Log.w(TAG, "Polling already started! Skipping duplicate start.");
            return;
        }

        pollingStarted = true;
        pollingHandler = new Handler();
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                Log.d(TAG, "=== Polling Run Start === " + startTime);
                refreshMessagesFromAPI();
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "=== Polling Run End === Duration: " + (endTime - startTime) + "ms");
                pollingHandler.postDelayed(this, POLLING_INTERVAL);
                Log.d(TAG, "Next polling scheduled in " + POLLING_INTERVAL + "ms");
            }
        };
        // Start polling after delay to allow Firebase to work first
        pollingHandler.postDelayed(pollingRunnable, 5000);
        Log.d(TAG, "Started message polling fallback (every " + POLLING_INTERVAL + "ms, first run in 5s)");
    }

    private void stopPollingMessages() {
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            pollingStarted = false;
            Log.d(TAG, "Stopped message polling");
        }
    }

    private long lastRefreshTime = 0;
    private static final long MIN_REFRESH_INTERVAL = 2000; // Minimum 2 seconds between refreshes

    private void refreshMessagesFromAPI() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefresh = currentTime - lastRefreshTime;

        if (timeSinceLastRefresh < MIN_REFRESH_INTERVAL) {
            Log.w(TAG, "Skipping refresh - too soon! Last refresh was " + timeSinceLastRefresh + "ms ago (min: " + MIN_REFRESH_INTERVAL + "ms)");
            return;
        }

        lastRefreshTime = currentTime;
        Log.d(TAG, "=== refreshMessagesFromAPI() called === Time since last: " + timeSinceLastRefresh + "ms");
        String token = "Bearer " + tokenManager.getAccessToken();
        apiService.getMessages(token, conversationId).enqueue(new Callback<ApiResponse<List<MessageDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MessageDTO>>> call, Response<ApiResponse<List<MessageDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MessageDTO> newMessages = response.body().getData();
                    if (newMessages != null && !newMessages.isEmpty()) {
                        // Only update if message count changed (new message received)
                        int currentCount = messageAdapter.getItemCount();
                        if (newMessages.size() > currentCount) {
                            Log.d(TAG, "Polling found new messages: " + (newMessages.size() - currentCount));

                            // Ensure proper formatting
                            for (MessageDTO msg : newMessages) {
                                if (msg.getCreatedAt() == null) {
                                    msg.setCreatedAt(LocalDateTime.now());
                                }
                                if (msg.getIsOwnMessage() == null && currentUserId != null) {
                                    msg.setIsOwnMessage(msg.getSenderUserId() != null && msg.getSenderUserId().equals(currentUserId));
                                }
                            }

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                newMessages.sort(Comparator.comparing(MessageDTO::getMessageId));
                            }

                            messageAdapter.updateMessages(newMessages);
                            if (messageAdapter.getItemCount() > 0) {
                                rvMessages.post(() -> {
                                    rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MessageDTO>>> call, Throwable t) {
                // Silently fail - this is just a fallback
                Log.d(TAG, "Polling request failed (this is normal)", t);
            }
        });
    }

    private void markAsRead() {
        String token = "Bearer " + tokenManager.getAccessToken();
        apiService.markAsRead(token, conversationId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "Conversation marked as read.");
                } else {
                    Log.e(TAG, "Failed to mark conversation as read.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Network error marking conversation as read.", t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
            Log.d(TAG, "Firebase listener removed");
        }
        stopPollingMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Keep listener active even when paused for real-time updates
        // But we could pause polling to save resources
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "=== onResume() called ===");
        // Refresh messages when returning to activity (only if enough time has passed)
        refreshMessagesFromAPI();
    }
}