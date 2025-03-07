package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;

/**
 * This screen allows users to create a new account.
 * Users must provide the required information such as name, username, email, and password.
 */
public class SignUpActivity extends AppCompatActivity {


    // UI elements for user input fields and sign up button
    private EditText nameEditText, usernameEditText, emailEditText, passwordEditText;
    private Button signUpButton;

    // FirebaseDB instance
    private FirebaseDB firebaseDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Set up back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        }

        // Initialize FirebaseDB
        firebaseDB = firebaseDB.getInstance(this);

        // Initialize UI elements
        nameEditText = findViewById(R.id.name);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signUpButton = findViewById(R.id.signUpButton);

        // Handle sign up button click
        signUpButton.setOnClickListener(v -> {

            // Retrieve user input from EditText fields
            String name = nameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate input fields before proceeding
            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if length is between 3 and 30
            if (username.length() < 3 || username.length() > 30) {
                Toast.makeText(SignUpActivity.this,
                        "Username must be between 3-30 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Check no embedded spaces
            if (username.contains(" ")) {
                Toast.makeText(SignUpActivity.this,
                        "Username cannot contain spaces",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Check only alphanumeric + underscore
            //    Adjust the regex if you want to allow hyphens or other chars
            if (!username.matches("^[A-Za-z0-9_]+$")) {
                Toast.makeText(SignUpActivity.this,
                        "Username can only contain letters, numbers, or underscores",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Now the username is valid and we can move on the the FirebaseDB step

            // Use FirebaseDB to sign up

            firebaseDB.signup(name, username, email, password, result -> {
                if (result) {

                    // Successful Sign Up
                    Toast.makeText(SignUpActivity.this, "User registered!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Sign up failed
                    Toast.makeText(SignUpActivity.this, "Sign up failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    // Handle back button click to return to the login screen
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

