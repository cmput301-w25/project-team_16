/**
 * AddMood Fragment allows users to create or edit a mood entry in the app.
 *
 * Core Functionalities:
 * - Select an emotional state (mood) with visual buttons
 * - Choose social context (Alone, One Person, Two People, Crowd)
 * - Add optional location using Google Maps
 * - Add optional photo from camera or gallery with preview via AddImage fragment
 * - Add a trigger message (max 20 characters)
 * - Choose between Public or Private visibility
 * - Save or delete mood events to/from Firebase via UserProfile and FirebaseDB
 *
 * Modes:
 * - Create mode: opens empty form for new entry
 * - Edit mode: pre-fills data for editing an existing MoodEvent (passed via arguments)
 *
 * Image Handling:
 * - Compresses and uploads selected image to Firebase Storage
 * - Handles removal and replacement of images (even deletes old image if updated)
 *
 * Permissions:
 * - Requests and handles permissions for Location, Camera, and Media access
 * - Guides users to app settings if permissions are permanently denied
 *
 * Communication:
 * - Uses FragmentResult API to handle selected images from AddImage
 * - Uses custom AddLocationDialog for location selection with reverse geocoding
 *
 * Usage:
 * Typically launched when the user clicks "Add Mood" from the bottom navigation.
 * Can also be launched with arguments to support editing an existing mood.
 */

package com.example.team_16.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class AddMood extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 1003;

    private MoodEvent moodEvent;
    private UserProfile userProfile;

    private EditText triggerInput;
    private TextView triggerCounter;
    private Button saveMoodButton, deleteMoodButton;
    private Button takePhotoButton, choosePhotoButton, addLocationButton, removeImageButton;
    private Button aloneButton, onePersonButton, twoPersonButton, crowdButton;
    private Button publicPostButton, privatePostButton;

    private Uri selectedPhotoUri = null;
    private Uri cameraImageUri = null;
    private String selectedMood, socialSetting;

    private LatLng selectedLatLng = null;
    private String selectedPlaceName = null;

    private boolean isEditMode = false;
    private boolean isImageChanged = false;

    private String selectedPostType = "Public";
    private String lastImageSource = "";
    private int currentFlipperIndex = 0;



    private ViewFlipper viewFlipper;
    private Button backButton, nextButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }
        if (savedInstanceState != null) {
            currentFlipperIndex = savedInstanceState.getInt("flipper_index", 0);
        }

        if (getArguments() != null && getArguments().containsKey("moodEvent")) {
            moodEvent = (MoodEvent) getArguments().getSerializable("moodEvent");
            isEditMode = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_mood, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        triggerInput = view.findViewById(R.id.trigger_text);
        triggerCounter = view.findViewById(R.id.trigger_counter);
        saveMoodButton = view.findViewById(R.id.save_mood_button);
        deleteMoodButton = view.findViewById(R.id.delete_entry_button);

        takePhotoButton = view.findViewById(R.id.take_photo_button);
        choosePhotoButton = view.findViewById(R.id.choose_photo_button);
        removeImageButton = view.findViewById(R.id.remove_image_button);
        addLocationButton = view.findViewById(R.id.add_location_button);

        aloneButton = view.findViewById(R.id.alone_button);
        onePersonButton = view.findViewById(R.id.one_person_button);
        twoPersonButton = view.findViewById(R.id.two_person_button);
        crowdButton = view.findViewById(R.id.crowd_button);

        publicPostButton = view.findViewById(R.id.public_post_button);
        privatePostButton = view.findViewById(R.id.private_post_button);
        viewFlipper = view.findViewById(R.id.view_flipper);


        backButton = view.findViewById(R.id.back_button);
        nextButton = view.findViewById(R.id.next_button);

        nextButton.setOnClickListener(v -> navigateToNextStep());
        backButton.setOnClickListener(v -> navigateToPreviousStep());

        setupMoodSelectionButtons(view);
        setupSocialSettingButtons(view);
        setupPostTypeButtons();
        setupPhotoButtons();
        setupLocationButton();
        setupSaveButton();
        setupDeleteButton();

        removeImageButton.setVisibility(View.GONE);
        takePhotoButton.setVisibility(View.VISIBLE);
        choosePhotoButton.setVisibility(View.VISIBLE);

        backButton.setVisibility(currentFlipperIndex == 1 ? View.VISIBLE : View.GONE);
        nextButton.setVisibility(currentFlipperIndex == 0 ? View.VISIBLE : View.GONE);

        checkAndRequestPermissions();
        viewFlipper.setDisplayedChild(currentFlipperIndex);

        if (isEditMode && moodEvent != null) {
            updateUIForExistingMood();
            deleteMoodButton.setVisibility(View.VISIBLE);
        } else {
            deleteMoodButton.setVisibility(View.GONE);
        }

        // Trigger counter initialization
        updateTriggerCounter(triggerCounter, triggerInput.getText().toString());
        triggerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTriggerCounter(triggerCounter, s.toString());
                if (s.length() > 20) {
                    triggerInput.setText(s.subSequence(0, 20));
                    triggerInput.setSelection(20);  // Set cursor position at the end
                    triggerInput.setError("Maximum 20 characters allowed.");
                } else {
                    triggerInput.setError(null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        removeImageButton.setOnClickListener(v -> {
            selectedPhotoUri = null;
            isImageChanged = true;

            takePhotoButton.setVisibility(View.VISIBLE);
            choosePhotoButton.setVisibility(View.VISIBLE);
            removeImageButton.setVisibility(View.GONE);

            lastImageSource = "";
            updatePhotoButtonsVisibility();
        });
    }
    private void updatePhotoButtonsVisibility() {
        if (selectedPhotoUri != null) {
            takePhotoButton.setVisibility(View.GONE);
            choosePhotoButton.setVisibility(View.GONE);
            removeImageButton.setVisibility(View.VISIBLE);
        } else {
            takePhotoButton.setVisibility(View.VISIBLE);
            choosePhotoButton.setVisibility(View.VISIBLE);
            removeImageButton.setVisibility(View.GONE);
        }
    }


    private void setupMoodSelectionButtons(View view) {
        int[] moodButtonIds = {
                R.id.anger_button, R.id.confusion_button, R.id.disgust_button,
                R.id.fear_button, R.id.happiness_button, R.id.sadness_button,
                R.id.shame_button, R.id.surprise_button
        };

        String[] moodNames = {
                "Anger", "Confusion", "Disgust", "Fear", "Happiness",
                "Sadness", "Shame", "Surprise"
        };

        for (int i = 0; i < moodButtonIds.length; i++) {
            int index = i;
            Button button = view.findViewById(moodButtonIds[i]);
            button.setOnClickListener(v -> {
                if (selectedMood != null && selectedMood.equals(moodNames[index])) {
                    // Deselect if clicking the same button
                    selectedMood = null;
                    button.setAlpha(0.5f);
                } else {
                    // Select this mood
                    selectedMood = moodNames[index];
                    highlightMoodButton(view, moodButtonIds[index]);
                }
            });
        }
    }
    private void navigateToNextStep() {
        if (selectedMood == null) {
            Toast.makeText(requireContext(), "Please select a mood.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (socialSetting == null) {
            Toast.makeText(requireContext(), "Please select a social setting.", Toast.LENGTH_SHORT).show();
            return;
        }
        viewFlipper.setDisplayedChild(1);
        currentFlipperIndex = 1;
        backButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
    }

    private void navigateToPreviousStep() {
        viewFlipper.setDisplayedChild(0);
        currentFlipperIndex = 0;
        backButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
    }

    /**
     * Highlight the selected mood button, reset others.
     */
    private void highlightMoodButton(View view, int selectedButtonId) {
        int[] moodButtonIds = {
                R.id.anger_button, R.id.confusion_button, R.id.disgust_button,
                R.id.fear_button, R.id.happiness_button, R.id.sadness_button,
                R.id.shame_button, R.id.surprise_button
        };

        for (int buttonId : moodButtonIds) {
            Button button = view.findViewById(buttonId);
            button.setAlpha(0.5f);
        }
        Button selectedButton = view.findViewById(selectedButtonId);
        selectedButton.setAlpha(1.0f);
    }

    /**
     * Social setting (alone, one person, etc.).
     */
    private void setupSocialSettingButtons(View view) {
        int[] socialButtonIds = {
                R.id.alone_button, R.id.one_person_button,
                R.id.two_person_button, R.id.crowd_button
        };

        String[] socialNames = {
                "Alone", "One Person", "Two People", "Crowd"
        };

        for (int i = 0; i < socialButtonIds.length; i++) {
            int index = i;
            Button button = view.findViewById(socialButtonIds[i]);
            button.setOnClickListener(v -> {
                if (socialSetting != null && socialSetting.equals(socialNames[index])) {
                    // Deselect
                    socialSetting = null;
                    button.setAlpha(0.5f);
                } else {
                    // Select new social setting
                    socialSetting = socialNames[index];
                    highlightSocialButton(button);
                }
            });
        }
    }

    private void highlightSocialButton(Button selectedButton) {
        aloneButton.setAlpha(0.5f);
        onePersonButton.setAlpha(0.5f);
        twoPersonButton.setAlpha(0.5f);
        crowdButton.setAlpha(0.5f);

        selectedButton.setAlpha(1.0f);


    }

    /**
     * Public/Private post toggles.
     */
    private void setupPostTypeButtons() {
        // Default highlight = Public
        highlightPostTypeButton(publicPostButton);
        privatePostButton.setAlpha(0.5f);

        publicPostButton.setOnClickListener(v -> {
            if (!"Public".equals(selectedPostType)) {
                selectedPostType = "Public";
                highlightPostTypeButton(publicPostButton);
                privatePostButton.setAlpha(0.5f);
            }
        });

        privatePostButton.setOnClickListener(v -> {
            if (!"Private".equals(selectedPostType)) {
                selectedPostType = "Private";
                highlightPostTypeButton(privatePostButton);
                publicPostButton.setAlpha(0.5f);
            }
        });
    }

    private void highlightPostTypeButton(Button selectedButton) {
        publicPostButton.setAlpha(0.5f);
        privatePostButton.setAlpha(0.5f);
        selectedButton.setAlpha(1.0f);
    }

    /**
     * Buttons for camera/gallery and launching AddImage dialog.
     */
    private void setupPhotoButtons() {
        choosePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        takePhotoButton.setOnClickListener(v -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MoodApp");

            cameraImageUri = requireContext().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            takePhotoLauncher.launch(cameraImageUri);
        });
    }

    /**
     * Launchers to handle the results of picking or taking a photo.
     */
    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    lastImageSource = "camera";

                    takePhotoButton.setVisibility(View.GONE);
                    choosePhotoButton.setVisibility(View.GONE);
                    removeImageButton.setVisibility(View.VISIBLE);

                    Bundle args = new Bundle();
                    args.putParcelable("selectedUriOld", selectedPhotoUri);
                    args.putParcelable("selectedUri", cameraImageUri);
                    currentFlipperIndex = viewFlipper.getDisplayedChild();

                    AddImage addImageFragment = AddImage.newInstance("Camera");
                    addImageFragment.setArguments(args);

                    getParentFragmentManager().setFragmentResultListener(
                            "image_result", this, (requestKey, result2) -> {
                                Uri finalUri = result2.getParcelable("uri");
                                if (finalUri != null) {
                                    selectedPhotoUri = finalUri;
                                    isImageChanged = true;
                                    updatePhotoButtonsVisibility();
                                }
                            });

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addImageFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        lastImageSource = "gallery";

                        takePhotoButton.setVisibility(View.GONE);
                        choosePhotoButton.setVisibility(View.GONE);
                        removeImageButton.setVisibility(View.VISIBLE);

                        Bundle args = new Bundle();
                        args.putParcelable("selectedUriOld", selectedPhotoUri);
                        args.putParcelable("selectedUri", imageUri);
                        currentFlipperIndex = viewFlipper.getDisplayedChild();  // save current index


                        AddImage addImageFragment = AddImage.newInstance("Gallery");
                        addImageFragment.setArguments(args);

                        getParentFragmentManager().setFragmentResultListener("image_result", this, (requestKey, result2) -> {
                            Uri finalUri = result2.getParcelable("uri");
                            if (finalUri != null && !finalUri.equals(selectedPhotoUri)) {
                                selectedPhotoUri = finalUri;
                                isImageChanged = true;
                                updatePhotoButtonsVisibility();
                            }
                        });
                        viewFlipper.setDisplayedChild(currentFlipperIndex);
                        backButton.setVisibility(currentFlipperIndex == 1 ? View.VISIBLE : View.GONE);
                        nextButton.setVisibility(currentFlipperIndex == 0 ? View.VISIBLE : View.GONE);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, addImageFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("flipper_index", currentFlipperIndex);
    }

    private void setupLocationButton() {
        addLocationButton.setOnClickListener(v -> {
            if (addLocationButton.getText().toString().equalsIgnoreCase("Remove Location")) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove Location")
                        .setMessage("Are you sure you want to remove the location?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            selectedLatLng = null;
                            selectedPlaceName = null;
                            addLocationButton.setText("Add Location");
                            Toast.makeText(requireContext(), "Location removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                            LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    AddLocationDialog locationDialog = new AddLocationDialog((latLng, placeName) -> {
                        selectedLatLng = latLng;
                        selectedPlaceName = placeName;
                        addLocationButton.setText("Remove Location");
                        Toast.makeText(requireContext(),
                                "Location selected: " + (placeName != null ? placeName :
                                        latLng.latitude + ", " + latLng.longitude),
                                Toast.LENGTH_SHORT).show();
                    });
                    locationDialog.show(getParentFragmentManager(), "AddLocationDialog");
                }
            }
        });
    }

    private void setupSaveButton() {
        saveMoodButton.setOnClickListener(v -> {
            if (!validateInputs()) return;

            EmotionalState emotionalState = EmotionalStateRegistry.getByName(selectedMood);
            if (emotionalState == null) {
                Toast.makeText(requireContext(), "Invalid mood selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            String triggerText = triggerInput.getText().toString().trim();

            Double latitude = (selectedLatLng != null) ? selectedLatLng.latitude : null;
            Double longitude = (selectedLatLng != null) ? selectedLatLng.longitude : null;
            String placeName = selectedPlaceName;

            // remove location on clear
            if (addLocationButton.getText().toString().equalsIgnoreCase("Add Location")) {
                latitude = null;
                longitude = null;
                placeName = null;
            }

            // Disable the save button to prevent multiple saves
            saveMoodButton.setEnabled(false);

            // Check if we're offline
            boolean isOffline = !FirebaseDB.getInstance(requireContext()).isOnline();
            if (isOffline) {
                Toast.makeText(requireContext(), "You're offline. Changes will sync when connected.", Toast.LENGTH_LONG).show();
            }

            if (isEditMode) {
                // Editing existing
                moodEvent.setEmotionalState(emotionalState);
                moodEvent.setTrigger(triggerText);
                moodEvent.setSocialSituation(socialSetting);
                moodEvent.setPostType(selectedPostType);
                moodEvent.setLatitude(latitude);
                moodEvent.setLongitude(longitude);
                moodEvent.setPlaceName(placeName);

                if (isImageChanged) {
                    if (moodEvent.getPhotoFilename() != null && !moodEvent.getPhotoFilename().isEmpty()) {
                        deleteImageFromFirebase(moodEvent.getPhotoFilename());
                    }

                    if (selectedPhotoUri != null) {
                        String filename = uploadImageToFirebase(selectedPhotoUri);
                        moodEvent.setPhotoFilename(filename);
                    } else {
                        moodEvent.setPhotoFilename(null);
                    }
                }

                // Dismiss fragment immediately
                if (isEditMode) {
                    // When editing, go back to profile
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    } else if (getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    } else {
                        requireActivity().onBackPressed();
                    }
                }

                // Save to database in background
                userProfile.editMoodEvent(moodEvent.getId(), moodEvent, success -> {
                    if (!isAdded()) return; // Check if fragment is still attached
                    if (!success) {
                        Toast.makeText(requireContext(), "Failed to update mood.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Mood updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                    saveMoodButton.setEnabled(true);
                });
            } else {
                MoodEvent newMoodEvent = new MoodEvent(
                        userProfile.getId(),
                        emotionalState,
                        triggerText,
                        socialSetting,
                        latitude,
                        longitude,
                        placeName
                );
                newMoodEvent.setPostType(selectedPostType);

                if (selectedPhotoUri != null) {
                    String filename = uploadImageToFirebase(selectedPhotoUri);
                    newMoodEvent.setPhotoFilename(filename);
                }

                // Save to database in background
                userProfile.addMoodEvent(newMoodEvent, success -> {
                    if (!isAdded()) return; // Check if fragment is still attached

                    if (!success) {
                        Toast.makeText(requireContext(), "Failed to save mood.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Mood saved successfully!", Toast.LENGTH_SHORT).show();

                        // Reset the form instead of navigating to Feed
                        resetForm();
                    }
                    saveMoodButton.setEnabled(true);
                });
            }
        });
    }

    /**
     * Reset all form fields after successful submission
     */
    private void resetForm() {
        // Reset mood selection
        selectedMood = null;

        // Reset all mood buttons
        int[] moodButtonIds = {
                R.id.anger_button, R.id.confusion_button, R.id.disgust_button,
                R.id.fear_button, R.id.happiness_button, R.id.sadness_button,
                R.id.shame_button, R.id.surprise_button
        };

        for (int buttonId : moodButtonIds) {
            View view = getView();
            if (view != null) {
                Button button = view.findViewById(buttonId);
                button.setAlpha(0.5f);
            }
        }

        // Reset social setting
        socialSetting = null;
        aloneButton.setAlpha(0.5f);
        onePersonButton.setAlpha(0.5f);
        twoPersonButton.setAlpha(0.5f);
        crowdButton.setAlpha(0.5f);

        // Reset trigger text
        triggerInput.setText("");

        // Reset photo
        selectedPhotoUri = null;
        isImageChanged = false;
        updatePhotoButtonsVisibility();

        // Reset location
        selectedLatLng = null;
        selectedPlaceName = null;
        addLocationButton.setText("Add Location");

        // Reset post type to default (Public)
        selectedPostType = "Public";
        highlightPostTypeButton(publicPostButton);
        privatePostButton.setAlpha(0.5f);

        // Reset to the first screen
        viewFlipper.setDisplayedChild(0);
        currentFlipperIndex = 0;
        backButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
    }
    private boolean validateInputs() {
        if (selectedMood == null) {
            Toast.makeText(requireContext(), "Please select a mood before saving.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (socialSetting == null) {
            Toast.makeText(requireContext(), "Please select who you were with.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void deleteImageFromFirebase(String filename) {
        if (filename == null || filename.isEmpty()) {
            Log.e("FirebaseStorage", "Filename is null or empty. Nothing to delete.");
            return;
        }
        StorageReference storageRef = FirebaseDB.getInstance(requireContext()).getReference(filename);
        storageRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("FirebaseStorage", "File deleted successfully: " + filename))
                .addOnFailureListener(e -> Log.e("FirebaseStorage", "Error deleting file: " + filename, e));
    }

    /**
     * Upload the selectedPhotoUri to Firebase and return the file path.
     */
    private String uploadImageToFirebase(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Keep compressing if > 65536 bytes
            int quality = 90;
            while (imageData.length > 65536 && quality > 10) {
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                imageData = baos.toByteArray();
                quality -= 10;
            }

            String filename = "images/" + userProfile.getId() + "_" + System.currentTimeMillis() + "_mood.jpg";
            StorageReference storageRef = FirebaseDB.getInstance(requireContext()).getReference(filename);

            UploadTask uploadTask = storageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot ->
                            Log.d("FirebaseStorage", "File uploaded: " + filename))
                    .addOnFailureListener(e ->
                            Log.e("FirebaseStorage", "File upload failed for: " + filename, e));

            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void updateUIForExistingMood() {
        if (moodEvent == null) return;

        highlightMoodButton(getView(), getMoodButtonId(moodEvent.getEmotionalState().getName()));
        selectedMood = moodEvent.getEmotionalState().getName();

        highlightSocialButton(getSocialButton(moodEvent.getSocialSituation()));
        socialSetting = moodEvent.getSocialSituation();

        triggerInput.setText(moodEvent.getTrigger());

        if ("Private".equalsIgnoreCase(moodEvent.getPostType())) {
            selectedPostType = "Private";
            highlightPostTypeButton(privatePostButton);
            publicPostButton.setAlpha(0.5f);
        } else {
            selectedPostType = "Public";
            highlightPostTypeButton(publicPostButton);
            privatePostButton.setAlpha(0.5f);
        }
        if (moodEvent.hasLocation()) {
            selectedLatLng = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
            selectedPlaceName = moodEvent.getPlaceName();
            addLocationButton.setText("Remove Location");
        } else {
            addLocationButton.setText("Add Location");
        }

        if (moodEvent.getPhotoFilename() != null && !moodEvent.getPhotoFilename().isEmpty()) {
            removeImageButton.setVisibility(View.VISIBLE);
            takePhotoButton.setVisibility(View.GONE);
            choosePhotoButton.setVisibility(View.GONE);
        } else {
            removeImageButton.setVisibility(View.GONE);
            takePhotoButton.setVisibility(View.VISIBLE);
            choosePhotoButton.setVisibility(View.VISIBLE);
        }
    }

    private int getMoodButtonId(String moodName) {
        switch (moodName) {
            case "Anger":     return R.id.anger_button;
            case "Confusion": return R.id.confusion_button;
            case "Disgust":   return R.id.disgust_button;
            case "Fear":      return R.id.fear_button;
            case "Happiness": return R.id.happiness_button;
            case "Sadness":   return R.id.sadness_button;
            case "Shame":     return R.id.shame_button;
            case "Surprise":  return R.id.surprise_button;
        }
        return -1;
    }

    private Button getSocialButton(String setting) {
        switch (setting) {
            case "Alone":       return aloneButton;
            case "One Person":  return onePersonButton;
            case "Two People":  return twoPersonButton;
            case "Crowd":       return crowdButton;
        }
        return null;
    }

    /**
     * Delete button for removing the entire mood event.
     */
    private void setupDeleteButton() {
        deleteMoodButton.setOnClickListener(v -> {
            if (moodEvent == null || moodEvent.getId() == null) {
                Toast.makeText(requireContext(), "No mood event to delete.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood event?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // Check if we're offline
                        boolean isOffline = !FirebaseDB.getInstance(requireContext()).isOnline();
                        if (isOffline) {
                            Toast.makeText(requireContext(), "You're offline. Changes will sync when connected.", Toast.LENGTH_LONG).show();
                        }

                        // Dismiss fragment immediately
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().popBackStack();
                        } else if (getFragmentManager() != null) {
                            getFragmentManager().popBackStack();
                        } else {
                            requireActivity().onBackPressed();
                        }

                        // Delete from Firebase in background

                        if (moodEvent.getPhotoFilename() != null && !moodEvent.getPhotoFilename().isEmpty()) {
                            deleteImageFromFirebase(moodEvent.getPhotoFilename());
                        }

                        userProfile.deleteMoodEvent(moodEvent.getId(), success -> {
                            if (!isAdded()) return; // Check if fragment is still attached
                            if (!success) {
                                Toast.makeText(requireContext(), "Failed to delete mood event.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Mood event deleted successfully!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    /**
     * Simple helper to update the character counter label.
     */
    private void updateTriggerCounter(TextView counterView, String text) {
        int charRemaining = Math.max(20 - text.length(), 0);
        counterView.setText(charRemaining + " characters left");
    }

    private void enableButton(Button button, boolean enable) {
        button.setEnabled(enable);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{permission}, requestCode);
    }



    /**
     * Checks all needed permissions at once (location, camera, gallery).
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!hasPermission(Manifest.permission.CAMERA)) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!hasPermission(Manifest.permission.READ_MEDIA_IMAGES)) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    permissionsNeeded.toArray(new String[0]),
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableButton(takePhotoButton, true);
            enableButton(choosePhotoButton, true);
            enableButton(addLocationButton, true);
        }
    }

    /**
     * Handle the result of requesting permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE ||
                requestCode == CAMERA_PERMISSION_REQUEST_CODE ||
                requestCode == GALLERY_PERMISSION_REQUEST_CODE) {

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])) {
                        enableButton(addLocationButton, true);
                    }
                    if (Manifest.permission.CAMERA.equals(permissions[i])) {
                        enableButton(takePhotoButton, true);
                    }
                    if (Manifest.permission.READ_MEDIA_IMAGES.equals(permissions[i])) {
                        enableButton(choosePhotoButton, true);
                    }
                } else {
                    // Permission denied
                    handlePermissionDenied(permissions[i], permissions[i].substring(19));
                }
            }
        }
    }

    /**
     * Guides user to settings if they permanently denied permission.
     */
    private void handlePermissionDenied(String permission, String permissionName) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(permissionName + " Permission Needed")
                    .setMessage("To use this feature, please enable " + permissionName.toLowerCase() + " access in your device settings.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).setToolbarTitle(isEditMode ? "Edit Mood" : "Add Mood");
        }
    }

}

