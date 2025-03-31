/**
 * EditProfileFragment allows the user to update their profile information,
 * including full name, username, and profile picture.
 *
 * Key Features:
 * - Displays current name and username
 * - Allows editing of full name and username fields
 * - Validates that username is not already taken by another user
 * - Opens system image picker to select a new profile picture
 * - Uploads selected image to Firebase Storage
 * - Updates user profile information in Firebase Firestore
 * - Shows progress dialogs during uploads
 * - Updates UI with new data after successful update
 *
 * Usage:
 * Typically launched from the Profile section or settings screen.
 * Expects the current user profile to be available from MoodTrackerApp.
 *
 * Important:
 * - Ensures username uniqueness by checking existing records in Firestore
 * - Uses Glide for image loading
 * - Relies on FirebaseDB helper functions for uploading and updating
 */

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

    private ProgressDialog progressDialog;

    private UserProfile userProfile;

    private Uri selectedImageUri;

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The View for the fragment's UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned.
     * Sets up UI components and initializes the view state with current user data.
     *
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        currentNameView     = view.findViewById(R.id.currentName);
        currentUsernameView = view.findViewById(R.id.currentUsername);
        editFullName        = view.findViewById(R.id.editFullName);
        editUsername        = view.findViewById(R.id.editUsername);
        btnSave             = view.findViewById(R.id.btnSave);
        avatarImageView     = view.findViewById(R.id.avatarImageView);

        currentNameView.setText("Current Name: " +
                (userProfile.getFullName() != null ? userProfile.getFullName() : ""));
        currentUsernameView.setText("Current Username: @" +
                (userProfile.getUsername() != null ? userProfile.getUsername() : ""));

        editFullName.setText(userProfile.getFullName());
        editUsername.setText(userProfile.getUsername());

        if (userProfile.getProfileImageUrl() != null
                && !userProfile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(userProfile.getProfileImageUrl())
                    .placeholder(R.drawable.image)
                    .into(avatarImageView);
        }

        avatarImageView.setOnClickListener(v -> openImageChooser());

        btnSave.setOnClickListener(v -> {
            String newFullName = editFullName.getText().toString().trim();
            String newUsername = editUsername.getText().toString().trim();

            if (newFullName.isEmpty() || newUsername.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newUsername.equals(userProfile.getUsername())) {
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
                        updateProfileWithOrWithoutImage(newFullName, newUsername);
                    }
                });
            } else {
                updateProfileWithOrWithoutImage(newFullName, newUsername);
            }
        });
    }

    /**
     * Opens the system image picker to allow the user to select a new profile picture.
     */
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the image picker activity.
     * Updates the avatar image view with the selected image.
     *
     * @param requestCode The request code passed to startActivityForResult
     * @param resultCode The result code returned by the child activity
     * @param data The Intent returned by the child activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            avatarImageView.setImageURI(selectedImageUri);
        }
    }

    /**
     * Updates the user's profile with new information.
     * Handles both cases where a new image is selected and where it isn't.
     *
     * @param newFullName The new full name for the user
     * @param newUsername The new username for the user
     */
    private void updateProfileWithOrWithoutImage(String newFullName, String newUsername) {
        if (selectedImageUri != null) {
            uploadImageAndUpdateProfile(newFullName, newUsername);
        } else {
            doProfileUpdate(newFullName, newUsername, userProfile.getProfileImageUrl());
        }
    }

    /**
     * Uploads a new profile image to Firebase Storage and then updates the profile.
     *
     * @param newFullName The new full name for the user
     * @param newUsername The new username for the user
     */
    private void uploadImageAndUpdateProfile(String newFullName, String newUsername) {
        showProgressDialog("Uploading image...");

        userProfile.getFirebaseDB().uploadProfileImage(selectedImageUri, userProfile.getId(), imageUrl -> {
            if (imageUrl != null) {
                doProfileUpdate(newFullName, newUsername, imageUrl);
            } else {
                dismissProgressDialog();
                Toast.makeText(getContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Performs the actual profile update in Firebase.
     *
     * @param fullName The new full name for the user
     * @param username The new username for the user
     * @param imageUrl The URL of the profile image (new or existing)
     */
    private void doProfileUpdate(String fullName, String username, String imageUrl) {
        String email = userProfile.getEmail();

        userProfile.updateProfile(fullName, email, username, imageUrl, success -> {
            dismissProgressDialog();
            if (success) {
                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
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

    /**
     * Shows a progress dialog with the given message.
     *
     * @param message The message to display in the progress dialog
     */
    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * Dismisses the progress dialog if it is currently showing.
     */
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

