package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;

/**
 * This is the login screen of the app.
 * Users can enter their username and password to login after we set up a database.
 * There are also options to sign up and reset password.
 */
public class MainActivity extends AppCompatActivity {

    // UI elements
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, signUpButton, resetPasswordButton;

    // FirebaseDB instance
    private FirebaseDB firebaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        firebaseDB = FirebaseDB.getInstance(this);

        // Check if user is already logged in
        String currentUserId = firebaseDB.getCurrentUserId();
        if (currentUserId != null) {
            // User is already logged in, load profile and navigate
            loadUserProfileAndNavigate(currentUserId);
            return;
        }

        // No active session, show login UI
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        EdgeToEdge.enable(this);


        // Handle Login Button Click
        loginButton.setOnClickListener(v -> {

            // Retrieve user input
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate the input fields
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter all fields!", Toast.LENGTH_SHORT).show();
            } else {

                // Use FirebaseDB to login
                firebaseDB.login(username, password, result -> {
                    if (result) {
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Get the current user ID
                        String userId = firebaseDB.getCurrentUserId();

                        // Load user profile and navigate to main content
                        loadUserProfileAndNavigate(userId);
                    } else {
                        Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Go to Sign Up screen
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Go to Reset Password screen
        resetPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Load user profile from Firebase and navigate to home screen
     */
    private void loadUserProfileAndNavigate(String userId) {
        // Show loading indicator if needed
        // loadingProgressBar.setVisibility(View.VISIBLE);

        UserProfile.loadFromFirebase(firebaseDB, userId, userProfile -> {
            // Hide loading indicator if needed
            // loadingProgressBar.setVisibility(View.GONE);

            if (userProfile != null) {
                // Store the user profile in the application
                saveUserProfileToApp(userProfile);

                // Navigate to home/dashboard activity
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Close the login activity
            } else {
                // Could not load user profile
                Toast.makeText(MainActivity.this,
                        "Error loading user profile. Please try again.",
                        Toast.LENGTH_SHORT).show();

                // Log the user out
                firebaseDB.logout();

                // If we were checking an existing session, make sure to show the login UI
                if (findViewById(R.id.loginButton) == null) {
                    setContentView(R.layout.activity_main);
                    // Re-initialize UI elements
                    // ...
                }
            }
        });
    }

    /**
     * Save the UserProfile to the application class
     */
    private void saveUserProfileToApp(UserProfile userProfile) {
        ((MoodTrackerApp) getApplication()).setCurrentUserProfile(userProfile);
    }
}