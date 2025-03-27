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

import java.util.Map;

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

        // Retrieve current profile info from bundle if available, otherwise use userProfile
        Bundle args = getArguments();
        String prefillFullName = (args != null && args.getString("fullName") != null)
                ? args.getString("fullName") : userProfile.getFullName();
        String prefillUsername = (args != null && args.getString("username") != null)
                ? args.getString("username") : userProfile.getUsername();

        // Prefill the EditText fields with current values
        editFullName.setText(prefillFullName);
        editUsername.setText(prefillUsername);

        // Also display the current user info above the fields
        currentNameView.setText("Current Name: " + (userProfile.getFullName() != null ? userProfile.getFullName() : ""));
        currentUsernameView.setText("Current Username: @" + (userProfile.getUsername() != null ? userProfile.getUsername() : ""));

        // Handle saving changes
        btnSave.setOnClickListener(v -> {

            // Get user input from the EditText fields
            String newFullName = editFullName.getText().toString().trim();
            String newUsername = editUsername.getText().toString().trim();

            // Error check: Ensure neither field is empty
            if (newFullName.isEmpty() || newUsername.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // If the username has been changed, check if it's already taken
            if (!newUsername.equals(userProfile.getUsername())) {
                userProfile.searchUsersByUsername(newUsername, users -> {
                    boolean usernameTaken = false;
                    if (users != null) {
                        // Loop through the returned users and check if any is not the current user
                        for (Map<String, Object> user : users) {
                            String id = (String) user.get("id");
                            if (id != null && !id.equals(userProfile.getId())) {
                                usernameTaken = true;
                                break;
                            }
                        }
                    }
                    if (usernameTaken) {
                        Toast.makeText(getContext(), "Username already taken", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserProfile(newFullName, newUsername);
                    }
                });
            } else {
                // Username hasn't changed so no need to check; update directly.
                updateUserProfile(newFullName, newUsername);
            }
        });
    }

    /**
     * Calls the UserProfile's updateProfile method with the new values.
     */
    private void updateUserProfile(String newFullName, String newUsername) {
        // Use the existing email from the user profile
        String email = userProfile.getEmail();
        userProfile.updateProfile(newFullName, email, newUsername, success -> {
            if (success) {
                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();

                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

