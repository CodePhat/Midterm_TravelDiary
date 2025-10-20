package com.example.mytraveldiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, passwordInput;
    private Button actionBtn;
    private TextView toggleBtn;
    private View nameSpace;
    private boolean isSignup = false;
    private final AppData appData = AppData.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize AppData with persistence
        AppData.getInstance().initialize(this);

        // Apply saved theme preference
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_login);

        nameInput = findViewById(R.id.nameInput);
        nameSpace = findViewById(R.id.nameSpace);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        actionBtn = findViewById(R.id.actionButton);
        toggleBtn = findViewById(R.id.toggleButton);

        toggleBtn.setOnClickListener(v -> toggleMode());
        actionBtn.setOnClickListener(v -> handleAuth());
    }

    private void toggleMode() {
        isSignup = !isSignup;
        int visibility = isSignup ? View.VISIBLE : View.GONE;
        nameInput.setVisibility(visibility);
        nameSpace.setVisibility(visibility);
        actionBtn.setText(isSignup ? "Sign Up" : "Sign in");
        toggleBtn.setText(isSignup ? "Already have an account? Login" : "Don't have an account? Sign Up");
    }

    private void handleAuth() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate email format
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (isSignup) {
            // Validate name for signup
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            success = appData.signup(name, email, password);
            if (!success) {
                Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
        } else {
            success = appData.login(email, password);
            if (!success) {
                Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (appData.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Login failed — user not set!", Toast.LENGTH_SHORT).show();
        }
    }
}