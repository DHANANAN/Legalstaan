package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.login_progress);

        GoogleSignInOptions gso = buildGoogleSignInOptions();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btnGoogle = findViewById(R.id.btn_google_sign_in);
        btnGoogle.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                String idToken = account.getIdToken();
                if (idToken != null && !idToken.isEmpty()) {
                    firebaseAuthWithGoogle(idToken);
                } else {
                    checkEnrollmentAndProceed(account.getEmail());
                }
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Google sign in is not configured yet.", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        checkEnrollmentAndProceed(user);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private GoogleSignInOptions buildGoogleSignInOptions() {
        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        String webClientId = resolveWebClientId();
        if (webClientId != null && !webClientId.startsWith("YOUR_WEB_CLIENT_ID")) {
            builder.requestIdToken(webClientId);
        }

        return builder.build();
    }

    @Nullable
    private String resolveWebClientId() {
        int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (resId == 0) {
            return null;
        }
        return getString(resId);
    }

    private void checkEnrollmentAndProceed(FirebaseUser user) {
        String email = user.getEmail();
        if (email == null) {
            signOutAndError("Email not found.");
            return;
        }

        db.collection("enrolled_users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // User is enrolled
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // User not enrolled
                    signOutAndError("Access Denied: Email " + email + " is not enrolled.");
                }
            });
    }

    private void checkEnrollmentAndProceed(String email) {
        if (email == null || email.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("enrolled_users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    signOutAndError("Access Denied: Email " + email + " is not enrolled.");
                }
            });
    }

    private void signOutAndError(String message) {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
