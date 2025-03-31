/**
 * SignUp.java
 *
 * A fragment that handles the user sign-up process in the Mood Tracker app.
 * Users can register by providing their full name, username, email, and password.
 *
 * Features:
 * - Validates user input for proper formatting and completeness.
 * - Displays inline error messages using `TextInputLayout` if validation fails.
 * - Uses `FirebaseDB` to create a new user in the database.
 * - Automatically logs the user in upon successful registration.
 * - Notifies the parent activity or fragment via `SignUpListener` on successful sign-up.
 *
 * UI Components:
 * - `TextInputLayout`s for full name, username, email, and password input.
 * - `EditText` fields tied to the layouts for user input.
 * - A `signUpButton` that triggers validation and account creation.
 * - A `Toolbar` with a back button to return to the login screen.
 *
 * Validations:
 * - Full name must not be empty.
 * - Username must be 3–30 characters long, no spaces, only letters, numbers, and underscores.
 * - Email must not be empty (further format validation assumed to be handled by Firebase).
 * - Password must not be empty (additional strength checks can be added as needed).
 *
 * Dependencies:
 * - `FirebaseDB`: Used to interact with Firebase Authentication and Firestore.
 *
 * Navigation:
 * - On success, `onSignUpSuccess(String userId)` is triggered in the hosting component.
 * - Toolbar back button allows navigation back to the previous screen.
 */

package com.example.team_16.ui.fragments;

import android.content.Context;
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
import com.google.android.material.textfield.TextInputLayout;


public class SignUp extends Fragment {
    public interface SignUpListener {
        void onSignUpSuccess(String userId);
    }

    private SignUpListener listener;

    private TextInputLayout nameLayout, usernameLayout, emailLayout, passwordLayout;
    private EditText nameEditText, usernameEditText, emailEditText, passwordEditText;
    private Button signUpButton;

    private FirebaseDB firebaseDB;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof SignUpListener) {
            listener = (SignUpListener) parentFragment;
        }
        else if (context instanceof SignUpListener) {
            listener = (SignUpListener) context;
        } else {
            throw new ClassCastException(
                    "Parent fragment or host activity must implement SignUpListener");
        }
    }

    public SignUp() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_sign_up, container, false);

        firebaseDB = FirebaseDB.getInstance(requireContext());

        Toolbar toolbar = view.findViewById(R.id.signUpToolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_24px);
            activity.getSupportActionBar().setTitle("");
        }

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        nameLayout = view.findViewById(R.id.nameLayout);
        usernameLayout = view.findViewById(R.id.usernameLayout);
        emailLayout = view.findViewById(R.id.emailLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);

        nameEditText = view.findViewById(R.id.name);
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        signUpButton = view.findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> {
            attemptSignUp();

            Animation scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            signUpButton.startAnimation(scaleDown);
        });

        return view;
    }


    private void attemptSignUp() {
        String name = nameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean hasError = false;

        if (name.isEmpty()) {
            nameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Name required"));
            hasError = true;
        } else {
            nameLayout.setError(null);
        }

        if (username.isEmpty()) {
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Username required"));
            hasError = true;
        } else if (username.length() < 3 || username.length() > 30) {
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Must be 3-30 characters"));
            hasError = true;

        } else if (username.contains(" ")) {
            usernameLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> No spaces allowed"));
            hasError = true;

            //  only allowed characters using regex
        } else if (!username.matches("^[A-Za-z0-9_]+$")) {
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

        if (hasError) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseDB.signup(name, username, email, password, message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            if (message.equals("Signup successful!")) {
                firebaseDB.login(username, password, loginMessage -> {
                    Toast.makeText(requireContext(), loginMessage, Toast.LENGTH_SHORT).show();

                    if (loginMessage.equals("Login successful!")) {
                        String userId = firebaseDB.getCurrentUserId();

                        if (listener != null) {
                            listener.onSignUpSuccess(userId);
                        }
                    }
                });
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (nameEditText != null) nameEditText.setError(null);
        if (usernameEditText != null) usernameEditText.setError(null);
        if (emailEditText != null) emailEditText.setError(null);
        if (passwordEditText != null) passwordEditText.setError(null);
    }
}