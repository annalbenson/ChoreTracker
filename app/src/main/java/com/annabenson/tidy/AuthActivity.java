package com.annabenson.tidy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity {

    private boolean isSignUp = true;

    private TextInputLayout tilName, tilEmail, tilPassword;
    private TextInputEditText etName, etEmail, etPassword;
    private MaterialButton btnPrimary, btnToggle;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already logged in, skip straight to the right screen
        int userId = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
                .getInt(Prefs.KEY_USER_ID, -1);
        if (userId != -1) {
            proceed(userId);
            return;
        }

        setContentView(R.layout.activity_auth);
        db = new DatabaseHandler(this);

        tilName     = findViewById(R.id.tilName);
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etName      = findViewById(R.id.etName);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnPrimary  = findViewById(R.id.btnPrimary);
        btnToggle   = findViewById(R.id.btnToggle);

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { submit(); return true; }
            return false;
        });

        btnPrimary.setOnClickListener(v -> submit());
        btnToggle.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isSignUp = !isSignUp;
        tilName.setVisibility(isSignUp ? View.VISIBLE : View.GONE);
        btnPrimary.setText(isSignUp ? "Create Account" : "Log In");
        btnToggle.setText(isSignUp
                ? "Already have an account? Log in"
                : "New here? Create one");
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void submit() {
        String name     = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Validate
        boolean valid = true;
        if (isSignUp && name.isEmpty()) {
            tilName.setError("Please enter your name");
            valid = false;
        } else {
            tilName.setError(null);
        }
        if (email.isEmpty() || !email.contains("@")) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        } else {
            tilEmail.setError(null);
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            tilPassword.setError(null);
        }
        if (!valid) return;

        if (isSignUp) {
            int userId = db.createUser(email, password, name);
            if (userId == -1) {
                tilEmail.setError("An account with this email already exists");
                return;
            }
            saveSession(userId);
            proceed(userId);
        } else {
            int userId = db.loginUser(email, password);
            if (userId == -1) {
                Snackbar.make(btnPrimary, "Incorrect email or password", Snackbar.LENGTH_SHORT).show();
                return;
            }
            saveSession(userId);
            proceed(userId);
        }
    }

    private void saveSession(int userId) {
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
                .edit()
                .putInt(Prefs.KEY_USER_ID, userId)
                .apply();
    }

    private void proceed(int userId) {
        DatabaseHandler dbCheck = new DatabaseHandler(this);
        Intent intent = dbCheck.hasProfile(userId)
                ? new Intent(this, MainActivity.class)
                : new Intent(this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
