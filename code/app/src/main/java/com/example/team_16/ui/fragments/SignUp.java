package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * Fragment for user sign-up.
 * Allows users to register by entering their full name, username, email, and password.
 */
public class SignUp extends Fragment {

    // UI elements for sign up
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


        // Initialize UI elements
        nameEditText = view.findViewById(R.id.name);
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signUpButton = view.findViewById(R.id.signUpButton);

        // Handle sign up button click
        signUpButton.setOnClickListener(v -> attemptSignUp());

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

        // Validate input fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }


        // check length between 3 and 30
        if (username.length() < 3 || username.length() > 30) {
            Toast.makeText(requireContext(),
                    "Username must be between 3-30 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // check spaces - present error if invalid
        if (username.contains(" ")) {
            Toast.makeText(requireContext(),
                    "Username cannot contain spaces",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ensure only allowed characters using regex
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            Toast.makeText(requireContext(),
                    "Username can only contain letters, numbers, or underscores",
                    Toast.LENGTH_SHORT).show();
            return;
        }


        // Call FirebaseDB signup method
        firebaseDB.signup(name, username, email, password, message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            if (message.equals("Signup successful!")) {
                requireActivity().onBackPressed();
            }
        });
    }
}
