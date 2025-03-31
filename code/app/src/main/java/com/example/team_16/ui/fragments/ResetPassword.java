/**
 * ResetPassword.java
 *
 * A fragment that provides UI and logic for resetting a user's password.
 *
 * Key Features:
 * - Allows users to input their email address to request a password reset link.
 * - Validates the email input field and displays an error if empty.
 * - Integrates with FirebaseDB to send the password reset email.
 * - Uses a scale-down animation on button click for a responsive UI.
 * - Navigates back to the previous screen on successful reset email trigger.
 *
 * UI Components:
 * - Email input field with validation (`TextInputLayout` + `EditText`)
 * - Reset button to trigger the email
 * - Back navigation via custom toolbar
 *
 * Dependencies:
 * - `FirebaseDB`: Custom Firebase wrapper for authentication methods
 * - `activity_reset_password.xml`: Layout file containing the reset UI
 * - `R.anim.scale_down`: Animation used on button interaction
 * - `R.drawable.arrow_back_24px`: Custom back arrow icon for toolbar
 *
 * Usage:
 * - Typically navigated to from the Login screen if the user forgets their password.
 * - After successful reset email, the fragment returns to the previous screen.
 */

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
import com.google.android.material.textfield.TextInputLayout;



public class ResetPassword extends Fragment {

    private TextInputLayout emailLayout;
    private EditText emailEditText;
    private Button requestResetButton;

    private FirebaseDB firebaseDB;

    public ResetPassword() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_reset_password, container, false);

        firebaseDB = FirebaseDB.getInstance(requireContext());

        Toolbar toolbar = view.findViewById(R.id.resetToolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_24px);
            activity.getSupportActionBar().setTitle("");
        }

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        emailLayout = view.findViewById(R.id.emailLayout);
        emailEditText = view.findViewById(R.id.email);
        requestResetButton = view.findViewById(R.id.requestResetButton);

        requestResetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            Animation scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);

            requestResetButton.startAnimation(scaleDown);

            if (TextUtils.isEmpty(email)) {
                emailLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Email required"));
                Toast.makeText(requireContext(), "Enter an email!", Toast.LENGTH_SHORT).show();
            } else {
                emailLayout.setError(null);
                sendResetEmail(email);
            }
        });

        return view;
    }

    /**
     * Sends a password reset email using FirebaseDB.
     * @param email The email address to which the reset link should be sent.
     */
    private void sendResetEmail(String email) {
        firebaseDB.sendPasswordResetEmail(email, result -> {
            if (result) {
                Toast.makeText(requireContext(), "Password reset link sent!", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), "Failed to send reset link!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clears the error messages
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (emailEditText != null) {
            emailEditText.setError(null);
        }
    }
}

