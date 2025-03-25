package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.UserProfile;

/**
 * Class that allows the user to edit there name / username
 * Updates are saved in the database for other users to see
 */
public class EditProfileFragment extends Fragment {

    // Display current user info
    private TextView currentNameView, currentUsernameView;
    // User inputs
    private EditText editFullName, editUsername;
    // Save button
    private Button btnSave;
    // Current user profile
    private UserProfile userProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current UserProfile instance
        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        // Initialize our views
        currentNameView    = view.findViewById(R.id.currentName);
        currentUsernameView= view.findViewById(R.id.currentUsername);
        editFullName       = view.findViewById(R.id.editFullName);
        editUsername       = view.findViewById(R.id.editUsername);
        btnSave            = view.findViewById(R.id.btnSave);

        // Display the current user name and username above the EditText fields
        String currentName     = userProfile.getFullName()  != null ? userProfile.getFullName()  : "";
        String currentUsername = userProfile.getUsername()  != null ? userProfile.getUsername() : "";

        currentNameView.setText("Current Name: " + currentName);
        currentUsernameView.setText("Current Username: @" + currentUsername);

        // Handle saving changes
        btnSave.setOnClickListener(v -> {

            // Get user input from the EditText fields
            String newFullName = editFullName.getText().toString().trim();
            String newUsername = editUsername.getText().toString().trim();

            // If you want to allow optional updates (only update non-empty fields):
            String finalFullName  = newFullName.isEmpty() ? null : newFullName;
            String finalUsername  = newUsername.isEmpty() ? null : newUsername;

            // keep the same email (not editing email here), so just pass existing:
            String existingEmail  = userProfile.getEmail();

            // Call the 3-parameter update method that includes the username
            userProfile.updateProfile(finalFullName, existingEmail, finalUsername, success -> {
                if (success) {
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    // Go back to Profile fragment
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

