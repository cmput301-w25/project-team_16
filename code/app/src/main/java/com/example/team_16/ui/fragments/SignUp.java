package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.ui.activity.MainActivity;
import com.google.android.material.textfield.TextInputLayout;


/**
 * Fragment for user sign-up.
 * Allows users to register by entering their full name, username, email, and password.
 */
public class SignUp extends Fragment {

    // UI elements for sign up
    private TextInputLayout nameLayout, usernameLayout, emailLayout, passwordLayout;
    private EditText nameEditText, usernameEditText, emailEditText, passwordEditText;
    private Button signUpButton;

    // FirebaseDB instance
    private FirebaseDB firebaseDB;


    // default constructor
    public SignUp() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // sign up layout (activity_sign_up.xml)
        View view = inflater.inflate(R.layout.activity_sign_up, container, false);

        // Initialize FirebaseDB
        firebaseDB = FirebaseDB.getInstance(requireContext());

        // Setup the toolbar as ActionBar with back arrow
        Toolbar toolbar = view.findViewById(R.id.signUpToolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_24px);
            activity.getSupportActionBar().setTitle("");
        }

        // Handle the back arrow click
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        nameLayout = view.findViewById(R.id.nameLayout);
        usernameLayout = view.findViewById(R.id.usernameLayout);
        emailLayout = view.findViewById(R.id.emailLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);

        // Initialize UI elements
        nameEditText = view.findViewById(R.id.name);
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signUpButton = view.findViewById(R.id.signUpButton);

        // Handle sign up button click
        signUpButton.setOnClickListener(v -> {
            attemptSignUp();

            Animation scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            signUpButton.startAnimation(scaleDown);
        });


        return view;
    }


    /**
     * Validates input fields and attempts to register a new user using FirebaseDB.
     */
    private void attemptSignUp() {

        // This section retrieves and trims the input values
        String name = nameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean hasError = false;

        // Check "Full Name"
        if (name.isEmpty()) {
            // Set error on the LAYOUT
            nameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Name required"));
            hasError = true;
        } else {
            // Clear layout error
            nameLayout.setError(null);
        }

        // Check username
        if (username.isEmpty()) {
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Username required"));
            hasError = true;
            // check length between 3 and 30
        } else if (username.length() < 3 || username.length() > 30) {
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Must be 3-30 characters"));
            hasError = true;

        // check spaces - present error if invalid
        } else if (username.contains(" ")) {
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> No spaces allowed"));
            hasError = true;

        // ensure only allowed characters using regex
        } else if (!username.matches("^[A-Za-z0-9_]+$")) {
            // Check for invalid characters
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Letters, numbers, underscores only"));
            hasError = true;

        } else {
            usernameLayout.setError(null);
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Email required"));
            hasError = true;
        } else {
            emailLayout.setError(null);
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Password required"));
            hasError = true;
        } else {
            passwordLayout.setError(null);
        }

        // If any field is invalid, show toast and stop processing
        if (hasError) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call FirebaseDB signup method
        firebaseDB.signup(name, username, email, password, message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            if (message.equals("Signup successful!")) {
                // Automatically log in the user after successful signup
                firebaseDB.login(username, password, loginMessage -> {
                    Toast.makeText(requireContext(), loginMessage, Toast.LENGTH_SHORT).show();

                    if (loginMessage.equals("Login successful!")) {
                        String userId = firebaseDB.getCurrentUserId();

                        // Load profile and navigate - call MainActivity's method
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).loadUserProfileAndNavigate(userId);
                        }
                    }
                });
            }
        });
    }

    /**
     * Clears the error messages
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear any error messages before the view is destroyed
        if (nameEditText != null) nameEditText.setError(null);
        if (usernameEditText != null) usernameEditText.setError(null);
        if (emailEditText != null) emailEditText.setError(null);
        if (passwordEditText != null) passwordEditText.setError(null);
    }
}
