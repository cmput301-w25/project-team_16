package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.ResetPassword;
import com.example.team_16.ui.fragments.SignUp;

/**
 * This is the login screen of the app.
 * Users can enter their username and password to login.
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

        // Makes sure the user has to log in / sign up every time
        firebaseDB.logout();


        // show login UI
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



        // Show the SignUp Fragment
        signUpButton.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new SignUp())
                    .addToBackStack(null)
                    .commit();
        });

        // Show the ResetPassword Fragment
        resetPasswordButton.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new ResetPassword())
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     * Load user profile from Firebase and navigate to home screen
     * @param userId The ID of the user who just logged in.
     */
    private void loadUserProfileAndNavigate(String userId) {
        // May implement a loading progress bar if we choose so.
        // Show loading indicator if needed - loadingProgressBar.setVisibility(View.VISIBLE);

        UserProfile.loadFromFirebase(firebaseDB, userId, userProfile -> {
            // Hide loading indicator if needed - loadingProgressBar.setVisibility(View.GONE);

            if (userProfile != null) {
                // Store the user profile in the application
                // and navigate to home/dashboard activity

                saveUserProfileToApp(userProfile);
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            } else {
                // If loading fails - present an error and sign out
                Toast.makeText(MainActivity.this,
                        "Error loading user profile. Please try again.",
                        Toast.LENGTH_SHORT).show();

                firebaseDB.logout();

            }
        });
    }

    /**
     * Save the UserProfile to the application class
     * @param userProfile The user profile to save.
     */
    private void saveUserProfileToApp(UserProfile userProfile) {
        ((MoodTrackerApp) getApplication()).setCurrentUserProfile(userProfile);
    }
}