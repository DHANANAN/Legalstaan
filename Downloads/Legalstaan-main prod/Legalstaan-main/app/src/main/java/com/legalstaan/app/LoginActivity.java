package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        tvToggleMode = findViewById(R.id.tv_toggle_mode);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btnGoogle = findViewById(R.id.btn_google_sign_in);
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
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
                                Toast.makeText(this, "Google authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                String msg;
                int code = e.getStatusCode();
                switch (code) {
                    case 10: // DEVELOPER_ERROR
                        msg = "Sign-in setup error. This build's SHA-1 fingerprint isn't registered in Firebase Console.\n\nFix: add the release SHA-1 in Firebase → Project Settings → Your apps → Add fingerprint, then download a fresh google-services.json.";
                        break;
                    case 7:  // NETWORK_ERROR
                        msg = "No internet connection. Check your network and try again.";
                        break;
                    case 12500: // SIGN_IN_FAILED
                        msg = "Google Play Services error. Update Google Play Services from the Play Store.";
                        break;
                    case 12501: // SIGN_IN_CANCELLED
                        msg = "Sign-in cancelled.";
                        break;
                    default:
                        msg = "Google sign-in failed. Code: " + code;
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void proceedToMain() {
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
