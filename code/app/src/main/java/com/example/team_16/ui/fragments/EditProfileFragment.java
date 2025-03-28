package com.example.team_16.ui.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.UserProfile;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Map;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    // Views
    private TextView currentNameView, currentUsernameView;
    private EditText editFullName, editUsername;
    private Button btnSave;
    private ShapeableImageView avatarImageView;

    // Progress dialog for showing "uploading..."
    private ProgressDialog progressDialog;

    // Current user
    private UserProfile userProfile;

    // Holds the newly picked image URI if user chooses one
    private Uri selectedImageUri;

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

        // Find views
        currentNameView     = view.findViewById(R.id.currentName);
        currentUsernameView = view.findViewById(R.id.currentUsername);
        editFullName        = view.findViewById(R.id.editFullName);
        editUsername        = view.findViewById(R.id.editUsername);
        btnSave             = view.findViewById(R.id.btnSave);
        avatarImageView     = view.findViewById(R.id.avatarImageView);

        // Prefill
        currentNameView.setText("Current Name: " +
                (userProfile.getFullName() != null ? userProfile.getFullName() : ""));
        currentUsernameView.setText("Current Username: @" +
                (userProfile.getUsername() != null ? userProfile.getUsername() : ""));

        editFullName.setText(userProfile.getFullName());
        editUsername.setText(userProfile.getUsername());

        // If we already have a profile image URL, load it with Glide
        if (userProfile.getProfileImageUrl() != null
                && !userProfile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(userProfile.getProfileImageUrl())
                    .placeholder(R.drawable.image) // fallback placeholder
                    .into(avatarImageView);
        }

        // Clicking the avatar picks a new image
        avatarImageView.setOnClickListener(v -> openImageChooser());

        // Single onClickListener for the Save button
        btnSave.setOnClickListener(v -> {
            String newFullName = editFullName.getText().toString().trim();
            String newUsername = editUsername.getText().toString().trim();

            if (newFullName.isEmpty() || newUsername.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username changed
            if (!newUsername.equals(userProfile.getUsername())) {
                // If changed, see if it's taken
                userProfile.searchUsersByUsername(newUsername, users -> {
                    boolean usernameTaken = false;
                    if (users != null) {
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
                        // Username is free => proceed with the update
                        updateProfileWithOrWithoutImage(newFullName, newUsername);
                    }
                });
            } else {
                // Username not changed => just update
                updateProfileWithOrWithoutImage(newFullName, newUsername);
            }
        });
    }

    // Step 1: Let user pick an image from gallery
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Step 2: Get the chosen image URI in onActivityResult
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Immediately show it locally (so user sees what they picked)
            avatarImageView.setImageURI(selectedImageUri);
        }
    }

    // Depending on whether user picked a new image, call "upload and update" or just "update".
    private void updateProfileWithOrWithoutImage(String newFullName, String newUsername) {
        if (selectedImageUri != null) {
            uploadImageAndUpdateProfile(newFullName, newUsername);
        } else {
            // No new image => keep old image URL
            doProfileUpdate(newFullName, newUsername, userProfile.getProfileImageUrl());
        }
    }

    // Step 3: Actually upload the image to Firebase Storage, then update Firestore with the new URL
    private void uploadImageAndUpdateProfile(String newFullName, String newUsername) {
        showProgressDialog("Uploading image...");

        // userProfile has getFirebaseDB(), which can upload the file
        userProfile.getFirebaseDB().uploadProfileImage(selectedImageUri, userProfile.getId(), imageUrl -> {
            if (imageUrl != null) {
                // If upload success, pass this new URL to doProfileUpdate
                doProfileUpdate(newFullName, newUsername, imageUrl);
            } else {
                dismissProgressDialog();
                Toast.makeText(getContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Step 4: Actually update the Firestore user doc with new fields
    private void doProfileUpdate(String fullName, String username, String imageUrl) {
        // Keep the same email from current user
        String email = userProfile.getEmail();

        // Full update with new username, new image
        userProfile.updateProfile(fullName, email, username, imageUrl, success -> {
            dismissProgressDialog();
            if (success) {
                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                // Optionally show new image again or pop back
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.image)
                        .into(avatarImageView);

                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

