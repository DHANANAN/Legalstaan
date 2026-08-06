package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private EditText etEmail, etPassword;
    private Button btnEmailAuth;
    private MaterialButton btnGuestSignIn;
    private TextView tvToggleMode;
    private boolean isSignUpMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.login_progress);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnEmailAuth = findViewById(R.id.btn_email_auth);
        btnGuestSignIn = findViewById(R.id.btn_guest_sign_in);
        tvToggleMode = findViewById(R.id.tv_toggle_mode);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btnGoogle = findViewById(R.id.btn_google_sign_in);
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        if (btnGuestSignIn != null) {
            btnGuestSignIn.setOnClickListener(v -> signInGuest());
        }
        btnEmailAuth.setOnClickListener(v -> handleEmailAuth());
        tvToggleMode.setOnClickListener(v -> toggleMode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current = mAuth.getCurrentUser();
        if (current != null) proceedToMain();
    }

    private void toggleMode() {
        isSignUpMode = !isSignUpMode;
        btnEmailAuth.setText(isSignUpMode ? "Create Account" : "Sign In");
        tvToggleMode.setText(isSignUpMode ? "Already have an account? Sign In" : "New here? Create an account");
    }

    private void handleEmailAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        if (isSignUpMode) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            proceedToMain();
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Sign up failed";
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            proceedToMain();
                        } else {
                            Toast.makeText(this, "Sign in failed. Check your credentials.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInGuest() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Welcome to Legalstaan!", Toast.LENGTH_SHORT).show();
                        proceedToMain();
                    } else {
                        // Direct local fallback if Firebase anonymous auth is disabled
                        proceedToMain();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, t -> {
                            progressBar.setVisibility(View.GONE);
                            if (t.isSuccessful()) {
                                proceedToMain();
                            } else {
                                signInGuest();
                            }
                        });
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                int code = e.getStatusCode();
                if (code == 10 || code == 12500 || code == 7) {
                    Toast.makeText(this, "Signing in as Guest student...", Toast.LENGTH_SHORT).show();
                    signInGuest();
                } else if (code == 12501) {
                    Toast.makeText(this, "Sign-in cancelled.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Google Sign-In notice (" + code + "). Entering app...", Toast.LENGTH_SHORT).show();
                    signInGuest();
                }
            }
        }
    }

    private void proceedToMain() {
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
