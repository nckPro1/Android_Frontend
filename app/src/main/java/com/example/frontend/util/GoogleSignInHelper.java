package com.example.frontend.util;

import android.content.Context;
import android.content.Intent;

import com.example.frontend.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleSignInHelper {

    private GoogleSignInClient googleSignInClient;
    private Context context;

    public interface OnSignInListener {
        void onSignInSuccess(String idToken, String email, String name, String photoUrl);
        void onSignInFailed(String error);
    }

    // Trong GoogleSignInHelper.java
    public GoogleSignInHelper(Context context) {
        this.context = context;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // ✅ ĐÃ SỬA
                .requestIdToken(context.getString(R.string.server_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleSignInResult(Intent data, OnSignInListener listener) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            String idToken = account.getIdToken();
            String email = account.getEmail();
            String name = account.getDisplayName();
            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

            if (idToken != null) {
                listener.onSignInSuccess(idToken, email, name, photoUrl);
            } else {
                listener.onSignInFailed("Không thể lấy ID token từ Google");
            }

        } catch (ApiException e) {
            listener.onSignInFailed("Google sign in failed: " + e.getStatusCode() + " - " + e.getMessage());
        }
    }

    public void signOut() {
        googleSignInClient.signOut();
    }

    public void revokeAccess() {
        googleSignInClient.revokeAccess();
    }
}

