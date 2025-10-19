package com.example.mytraveldiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // <-- Quan trọng: Cần import View
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout; // <-- IMPORT DÒNG NÀY

public class LoginActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, passwordInput;
    private TextInputLayout nameInputLayout; // <-- THÊM DÒNG NÀY
    private Button actionBtn;
    private TextView toggleButton, formTitle;
    private boolean isSignup = false;
    private final AppData appData = AppData.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameInputLayout = findViewById(R.id.nameInputLayout); // <-- THÊM DÒNG NÀY
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        actionBtn = findViewById(R.id.actionButton);
        toggleButton = findViewById(R.id.toggleButton);
        formTitle = findViewById(R.id.formTitle);

        toggleButton.setOnClickListener(v -> toggleMode());
        actionBtn.setOnClickListener(v -> handleAuth());
    }
    private void toggleMode() {
        isSignup = !isSignup;
        nameInputLayout.setVisibility(isSignup ? View.VISIBLE : View.GONE);
        formTitle.setText(isSignup ? "SIGN UP" : "LOGIN");
        actionBtn.setText(isSignup ? "Sign Up" : "Login");
        toggleButton.setText(isSignup ? "Already have an account? Login" : "Don't have an account? Sign Up");
    }

    private void handleAuth() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        boolean success;
        if (isSignup) {
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