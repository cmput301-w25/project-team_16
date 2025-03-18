package com.example.team_16.ui.fragments;

import android.os.Bundle;
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


/**
 * Fragment for resetting password.
 * Provides a field for entering an email address and a button to send a reset link.
 */
public class ResetPassword extends Fragment {

    // UI elements for reset functionality
    private EditText emailEditText;
    private Button requestResetButton;

    // FirebaseDB instance
    private FirebaseDB firebaseDB;

    // default constructor
    public ResetPassword() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // reset password layout (activity_reset_password.xml)
        View view = inflater.inflate(R.layout.activity_reset_password, container, false);

        // Initialize FirebaseDB
        firebaseDB = FirebaseDB.getInstance(requireContext());

        // Setup the toolbar as ActionBar with back arrow
        Toolbar toolbar = view.findViewById(R.id.resetToolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back_24px);
            activity.getSupportActionBar().setTitle("");
        }

        // Handle the back arrow click
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // UI elements in the layout
        emailEditText = view.findViewById(R.id.email);
        requestResetButton = view.findViewById(R.id.requestResetButton);

        // Handle the reset button click
        requestResetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            Animation scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);

            requestResetButton.startAnimation(scaleDown);

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(requireContext(), "Enter an email!", Toast.LENGTH_SHORT).show();
            } else {
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
}

