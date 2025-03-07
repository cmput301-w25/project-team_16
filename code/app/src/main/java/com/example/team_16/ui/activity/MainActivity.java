package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;


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
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        firebaseDB = FirebaseDB.getInstance(this);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

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

                        // Now after a successful login:
                        // Go to another activity

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
}


