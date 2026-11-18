package com.example.forgotpassword;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * MainActivity - CycloneSafe Forgot Password Page
 * Handles user email input validation, password-reset link dispatch,
 * and loading / success / error UI feedback states.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageButton btnBack;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSendResetLink;
    private ProgressBar progressBar;
    private LinearLayout layoutStatusMessage;
    private ImageView ivStatusIcon;
    private TextView tvStatusText;
    private LinearLayout llBackToLoginContainer;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupWindowInsets();
        initViews();
        setupListeners();
    }

    /**
     * Configure Edge-to-Edge window insets padding for system bars.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Initialize UI components.
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        progressBar = findViewById(R.id.progressBar);
        layoutStatusMessage = findViewById(R.id.layoutStatusMessage);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusText = findViewById(R.id.tvStatusText);
        llBackToLoginContainer = findViewById(R.id.llBackToLoginContainer);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    /**
     * Setup event listeners for user interactions.
     */
    private void setupListeners() {
        // Back Navigation
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBackAction());
        }
        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> handleBackAction());
        }
        if (llBackToLoginContainer != null) {
            llBackToLoginContainer.setOnClickListener(v -> handleBackAction());
        }

        // Send Reset Link Action
        if (btnSendResetLink != null) {
            btnSendResetLink.setOnClickListener(v -> handleSendResetLink());
        }

        // Clear error when user edits text
        if (etEmail != null) {
            etEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilEmail != null && tilEmail.getError() != null) {
                        tilEmail.setError(null);
                    }
                    if (layoutStatusMessage != null && layoutStatusMessage.getVisibility() == View.VISIBLE) {
                        layoutStatusMessage.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    /**
     * Handles password reset submission after validating user email.
     */
    private void handleSendResetLink() {
        String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (!validateEmail(email)) {
            return;
        }

        setLoadingState(true);
        sendPasswordResetLink(email);
    }

    /**
     * Validates the provided email address string.
     *
     * @param email user input string
     * @return true if valid email format; false otherwise.
     */
    private boolean validateEmail(@NonNull String email) {
        if (TextUtils.isEmpty(email)) {
            if (tilEmail != null) {
                tilEmail.setError(getString(R.string.email_empty_error));
            }
            if (etEmail != null) {
                etEmail.requestFocus();
            }
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) {
                tilEmail.setError(getString(R.string.email_invalid_error));
            }
            if (etEmail != null) {
                etEmail.requestFocus();
            }
            return false;
        }

        if (tilEmail != null) {
            tilEmail.setError(null);
        }
        return true;
    }

    /**
     * Password Reset Request dispatcher.
     * Prepared for future Supabase Integration.
     * Note: Replace simulation below with Supabase Auth call:
     * supabase.getGotrue().resetPasswordForEmail(email, options, callback);
     *
     * @param email Target recipient email address
     */
    private void sendPasswordResetLink(@NonNull String email) {
        Log.d(TAG, "Requesting password reset link for email: " + email);

        // Simulated network delay for UI verification
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            setLoadingState(false);

            // Simulation of reset link request response
            if (email.endsWith("@error.com")) {
                showErrorMessage(getString(R.string.reset_failed_message));
            } else {
                showSuccessMessage(getString(R.string.reset_success_message));
                Toast.makeText(MainActivity.this, R.string.reset_success_message, Toast.LENGTH_LONG).show();
            }
        }, 1500);
    }

    /**
     * Toggles UI loading state during asynchronous operations.
     *
     * @param isLoading true to enable loading progress; false to restore interactive state.
     */
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            if (btnSendResetLink != null) {
                btnSendResetLink.setEnabled(false);
                btnSendResetLink.setText("");
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (etEmail != null) {
                etEmail.setEnabled(false);
            }
            if (layoutStatusMessage != null) {
                layoutStatusMessage.setVisibility(View.GONE);
            }
        } else {
            if (btnSendResetLink != null) {
                btnSendResetLink.setEnabled(true);
                btnSendResetLink.setText(R.string.send_reset_link);
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (etEmail != null) {
                etEmail.setEnabled(true);
            }
        }
    }

    /**
     * Displays a styled success message banner.
     *
     * @param message Message to present to the user.
     */
    public void showSuccessMessage(@NonNull String message) {
        if (layoutStatusMessage == null || ivStatusIcon == null || tvStatusText == null) return;
        int successColor = ContextCompat.getColor(this, R.color.success_text);
        layoutStatusMessage.setBackgroundResource(R.drawable.bg_status_success);
        ivStatusIcon.setImageResource(R.drawable.ic_email);
        ivStatusIcon.setImageTintList(ColorStateList.valueOf(successColor));
        tvStatusText.setText(message);
        tvStatusText.setTextColor(successColor);
        layoutStatusMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Displays a styled error message banner.
     *
     * @param message Message to present to the user.
     */
    public void showErrorMessage(@NonNull String message) {
        if (layoutStatusMessage == null || ivStatusIcon == null || tvStatusText == null) return;
        int errorColor = ContextCompat.getColor(this, R.color.error_text);
        layoutStatusMessage.setBackgroundResource(R.drawable.bg_status_error);
        ivStatusIcon.setImageResource(R.drawable.ic_email);
        ivStatusIcon.setImageTintList(ColorStateList.valueOf(errorColor));
        tvStatusText.setText(message);
        tvStatusText.setTextColor(errorColor);
        layoutStatusMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Handles back or return to login action.
     */
    private void handleBackAction() {
        Toast.makeText(this, "Navigating back to login...", Toast.LENGTH_SHORT).show();
        finish();
    }
}
