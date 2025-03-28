package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.ResetPassword;
import com.example.team_16.ui.fragments.SignUp;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * This is the login screen of the app.
 * Users can enter their username and password to login.
 * There are also options to sign up and reset password.
 */
public class MainActivity extends AppCompatActivity {
    // UI elements
    // TextInputLayouts
    private TextInputLayout usernameInputLayout, passwordInputLayout;
    // EditTexts
    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton, signUpButton, resetPasswordButton;
    private LinearLayout loginLinearLayout;
    private FrameLayout fragmentContainer;

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
        //EdgeToEdge.enable(this);

        // Initialize UI elements
        loginLinearLayout = findViewById(R.id.loginLinear);
        fragmentContainer = findViewById(R.id.fragment_container);
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);


        loginButton.setOnClickListener(v -> {
            Animation scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
            loginButton.startAnimation(scale_down);

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            // The next piece of code is to get the red error messages
            boolean hasError = false;

            // Check username field
            if (username.isEmpty()) {
                usernameInputLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Username required"));
                hasError = true;
            } else {
                usernameEditText.setError(null);
                usernameInputLayout.setError(null);
            }
            // Check password field
            if (password.isEmpty()) {
                passwordInputLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Password required"));
                hasError = true;
            } else {
                passwordEditText.setError(null);
                passwordInputLayout.setError(null);
            }
            // If any field is empty, show toast and stop processing
            if (hasError) {
                Toast.makeText(MainActivity.this, "Please enter both username and password!", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseDB.login(username, password, message -> {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                if (message.equals("Login successful!")) {
                    String userId = firebaseDB.getCurrentUserId();
                    loadUserProfileAndNavigate(userId);
                }
            });
        });

        // Show the SignUp Fragment
        signUpButton.setOnClickListener(v -> {
            // Clear errors on login fields before showing sign-up
            usernameEditText.setError(null);
            passwordEditText.setError(null);

            Animation scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
            signUpButton.startAnimation(scale_down);

            showFragment(new SignUp());
        });

        // Show the ResetPassword Fragment
        resetPasswordButton.setOnClickListener(v -> {
            // Clear errors on login fields before showing sign-up
            usernameEditText.setError(null);
            passwordEditText.setError(null);

            Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
            resetPasswordButton.startAnimation(scaleDown);

            showFragment(new ResetPassword());
        });

        // Set up the FragmentManager listener
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // Check if we're back to the main screen
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                // No more fragments in backstack, show the login form
                loginLinearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Show a fragment in the fragment container
     * @param fragment The fragment to show
     */
    private void showFragment(Fragment fragment) {
        // First make the container visible - it needs to be visible for the animation to work
        fragmentContainer.setVisibility(View.VISIBLE);

        // Create the transaction with animations
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // Fragment enters from right
                        R.anim.slide_out_left,   // Current fragment exits to left
                        R.anim.slide_in_left,    // Fragment enters from left (when popping back stack)
                        R.anim.slide_out_right   // Current fragment exits to right (when popping back stack)
                );

        // Hide the login form
        //loginLinearLayout.setVisibility(View.GONE);

        // Replace any existing fragment and add to back stack
        transaction.replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            // Let the animation finish first, then the visibility will be handled
            // by the BackStackChangedListener
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Load user profile from Firebase and navigate to home screen
     * @param userId The ID of the user who just logged in.
     */
    public void loadUserProfileAndNavigate(String userId) {
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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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