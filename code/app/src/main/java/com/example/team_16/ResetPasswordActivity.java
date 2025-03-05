package com.example.team_16;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


/**
 * This is for the reset password page which allows users to request a password reset.
 * The implementation is not functional yet.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    // UI elements
    private EditText emailEditText;
    private Button requestResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Set up back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        requestResetButton = findViewById(R.id.requestResetButton);

        // Handles the reset password button click
        requestResetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // Validate if email field is empty
            if (email.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Enter an email!", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Implement password reset logic
                Toast.makeText(ResetPasswordActivity.this, "Password reset link sent!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle back button click to return to previous screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

