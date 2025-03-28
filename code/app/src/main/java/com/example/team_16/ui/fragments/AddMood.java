package com.example.team_16.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


/**
 * Fragment to add a mood event, using UserProfile to manage mood events.
 */
public class AddMood extends Fragment {
    // Permission constants
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 1003;
    private static final int CAMERA_REQUEST_CODE = 101;


    private MoodEvent moodEvent;
    private UserProfile userProfile;

    private EditText triggerInput;
    private TextView triggerCounter;
    private Button saveMoodButton, deleteMoodButton, takePhotoButton, choosePhotoButton, addLocationButton;
    private Button aloneButton, onePersonButton, twoPersonButton, crowdButton;
    private Uri selectedPhotoUri = null;
    private Uri cameraImageUri = null;
    private String selectedMood, socialSetting;

    // Stores the selected coordinates (null if no location selected)
    private LatLng selectedLatLng = null;

    // Stores the selected place name (null if no name is available)
    private String selectedPlaceName = null;

    private static final int PICK_PHOTO_REQUEST = 1;
    private boolean isEditMode = false; //flag for adding or editing
    private boolean isImageChanged = false;

    private Button publicPostButton, privatePostButton;
    private String selectedPostType = "Public";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
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
        addLocationButton = view.findViewById(R.id.add_location_button);

        publicPostButton = view.findViewById(R.id.public_post_button);
        privatePostButton = view.findViewById(R.id.private_post_button);

        aloneButton = view.findViewById(R.id.alone_button);
        onePersonButton = view.findViewById(R.id.one_person_button);
        twoPersonButton = view.findViewById(R.id.two_person_button);
        crowdButton = view.findViewById(R.id.crowd_button);

        setupMoodSelectionButtons(view);
        setupSocialSettingButtons(view);
        setupPhotoButtons();
        setupLocationButton();
        setupSaveButton();
        setupDeleteButton();
        setupPostTypeButtons();

        // App permission handling
        checkAndRequestPermissions();

        // If editing an existing MoodEvent, update the UI immediately
        if (isEditMode && moodEvent != null) {
            updateUIForExistingMood();
            deleteMoodButton.setVisibility(View.VISIBLE);
        } else {
            deleteMoodButton.setVisibility(View.GONE);
        }

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
    }

    /**
     * Ensures user selects a mood before saving.
     */
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

    /**
     * Sets up click listeners for mood selection buttons.
     */
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
                    selectedMood = null;
                    button.setAlpha(0.8f);
                } else {
                    selectedMood = moodNames[index];
                    highlightMoodButton(view, moodButtonIds[index]);
                }
            });
        }
    }

    /**
     * Visual feedback for selected mood button.
     */
    private void highlightMoodButton(View view, int selectedButtonId) {
        int[] moodButtonIds = {
                R.id.anger_button, R.id.confusion_button, R.id.disgust_button,
                R.id.fear_button, R.id.happiness_button, R.id.sadness_button,
                R.id.shame_button, R.id.surprise_button
        };

        for (int buttonId : moodButtonIds) {
            Button button = view.findViewById(buttonId);
            button.setAlpha(0.8f);
        }
        Button selectedButton = view.findViewById(selectedButtonId);
        selectedButton.setAlpha(1.0f);
    }

    /**
     * Sets up click listeners for social setting buttons.
     */
    private void setupSocialSettingButtons(View view) {
        int[] socialButtonIds = {
                R.id.alone_button, R.id.one_person_button, R.id.two_person_button, R.id.crowd_button
        };

        String[] socialNames = {
                "Alone", "One Person", "Two People", "Crowd"
        };

        for (int i = 0; i < socialButtonIds.length; i++) {
            int index = i;
            Button button = view.findViewById(socialButtonIds[i]);
            button.setOnClickListener(v -> {
                if (socialSetting != null && socialSetting.equals(socialNames[index])) {
                    // If already selected, deselect
                    socialSetting = null;
                    button.setAlpha(0.8f);
                } else {
                    // Select new social setting
                    socialSetting = socialNames[index];
                    highlightSocialButton(button);
                }
            });
        }
    }

    /**
     * Visual feedback for selected social button.
     */
    private void highlightSocialButton(Button selectedButton) {
        aloneButton.setAlpha(0.8f);
        onePersonButton.setAlpha(0.8f);
        twoPersonButton.setAlpha(0.8f);
        crowdButton.setAlpha(0.8f);

        selectedButton.setAlpha(1.0f);
    }

    private void populateUIWithExistingData() {
        triggerInput.setText(moodEvent.getTrigger());
        selectedMood = moodEvent.getEmotionalState().getName();
        socialSetting = moodEvent.getSocialSituation();

        highlightMoodButton(requireView(), getMoodButtonId(selectedMood));
        highlightSocialButton(getSocialButton(socialSetting));
    }

    /**
     * Updates the UI with remaining character count.
     */
    private void updateTriggerCounter(TextView counterView, String text) {
        int charRemaining = Math.max(20 - text.length(), 0);
        counterView.setText(charRemaining + " characters left");
    }

    /**
     * Resets the input field when the form is reset.
     */
    private void resetForm() {
        triggerInput.setText("");
        triggerInput.setError(null);
        triggerCounter.setText("20 characters left");
    }

    private int getMoodButtonId(String moodName) {
        switch (moodName) {
            case "Anger": return R.id.anger_button;
            case "Confusion": return R.id.confusion_button;
            case "Disgust": return R.id.disgust_button;
            case "Fear": return R.id.fear_button;
            case "Happiness": return R.id.happiness_button;
            case "Sadness": return R.id.sadness_button;
            case "Shame": return R.id.shame_button;
            case "Surprise": return R.id.surprise_button;
            default: return -1;
        }
    }

    private Button getSocialButton(String setting) {
        switch (setting) {
            case "Alone": return aloneButton;
            case "One Person": return onePersonButton;
            case "Two People": return twoPersonButton;
            case "Crowd": return crowdButton;
            default: return null;
        }
    }

    /**
     * Opens the photo picker.
     */
    private void setupPhotoButtons() {
        choosePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");

            if (selectedPhotoUri == null) {
                pickImageLauncher.launch(intent);
            }
            else {
                Bundle args = new Bundle();
                args.putParcelable("selectedUriOld", selectedPhotoUri);
                args.putParcelable("selectedUri", null);
                AddImage addImageFragment = AddImage.newInstance("Gallery");
                addImageFragment.setArguments(args);

                getParentFragmentManager().setFragmentResultListener("image_result", this, (requestKey, result2) -> {
                    if (selectedPhotoUri != result2.getParcelable("uri")) {
                        selectedPhotoUri = result2.getParcelable("uri");
                        isImageChanged = Boolean.TRUE;
                    }
                });

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addImageFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        takePhotoButton.setOnClickListener(v -> {

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MoodApp");

            cameraImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            takePhotoLauncher.launch(cameraImageUri);

        });
    }

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {

                if (success && cameraImageUri != null) {
                    //cameraImageUri = selectedPhotoUri;
                    //isImageChanged = Boolean.TRUE;

                    Bundle args = new Bundle();
                    args.putParcelable("selectedUriOld", selectedPhotoUri);
                    args.putParcelable("selectedUri", cameraImageUri);
                    AddImage addImageFragment = AddImage.newInstance("Camera");
                    addImageFragment.setArguments(args);

                    getParentFragmentManager().setFragmentResultListener("image_result", this, (requestKey, result2) -> {
                        if (selectedPhotoUri != result2.getParcelable("uri")) {
                            selectedPhotoUri = result2.getParcelable("uri");
                            isImageChanged = Boolean.TRUE;
                        }
                    });

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addImageFragment)
                            .addToBackStack(null)
                            .commit();

                }

            });

    /**
     * Handles photo selection.
     */
    private ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Get the selected image URI.
                    Uri imageUri = result.getData().getData();
                    Bundle args = new Bundle();
                    args.putParcelable("selectedUriOld", selectedPhotoUri);
                    args.putParcelable("selectedUri", imageUri);
                    AddImage addImageFragment = AddImage.newInstance("Gallery");
                    addImageFragment.setArguments(args);

                    getParentFragmentManager().setFragmentResultListener("image_result", this, (requestKey, result2) -> {
                        if (selectedPhotoUri != result2.getParcelable("uri")) {
                            selectedPhotoUri = result2.getParcelable("uri");
                            isImageChanged = Boolean.TRUE;
                        }
                    });

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addImageFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
    );

    private void setupLocationButton() {
        boolean hasLocationPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        enableButton(addLocationButton, hasLocationPermission);

        addLocationButton.setOnClickListener(v -> {
            if (!hasLocationPermission) {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                // Open location picker dialog
                AddLocationDialog locationDialog = new AddLocationDialog((latLng, placeName) -> {
                    selectedLatLng = latLng;
                    selectedPlaceName = placeName;

                    // Update UI with selected location (if needed)
                    Toast.makeText(requireContext(), "Location selected: " +
                                    (placeName != null ? placeName : latLng.latitude + ", " + latLng.longitude),
                            Toast.LENGTH_SHORT).show();
                });

                locationDialog.show(getParentFragmentManager(), "AddLocationDialog");
            }
        });
    }

    /**
     * Saves the mood event using UserProfile.
     */
    private void setupSaveButton() {
        saveMoodButton.setOnClickListener(v -> {
            if (!validateInputs()) return;

            EmotionalState emotionalState = EmotionalStateRegistry.getByName(selectedMood);
            if (emotionalState == null) {
                Toast.makeText(requireContext(), "Invalid mood selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            String triggerText = triggerInput.getText().toString().trim();

            //get location data (optional)
            // Handle optional location data
            Double latitude = (selectedLatLng != null) ? selectedLatLng.latitude : null;
            Double longitude = (selectedLatLng != null) ? selectedLatLng.longitude : null;
            String placeName = (selectedPlaceName != null) ? selectedPlaceName : null;

            if (isEditMode) {
                moodEvent.setEmotionalState(emotionalState);
                moodEvent.setTrigger(triggerText);
                moodEvent.setSocialSituation(socialSetting);
                moodEvent.setPostType(selectedPostType);
                moodEvent.setLatitude(latitude);
                moodEvent.setLongitude(longitude);
                moodEvent.setPlaceName(placeName);

                if (selectedPhotoUri != null && isImageChanged) {
                    deleteImageFromFirebase(moodEvent.getPhotoFilename());
                    String filename = uploadImageToFirebase();
                    moodEvent.setPhotoFilename(filename);
                }

                userProfile.editMoodEvent(moodEvent.getId(), moodEvent, success -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Mood updated successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update mood.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Create new MoodEvent
                MoodEvent newMoodEvent = new MoodEvent(userProfile.getId(),
                                                        emotionalState,
                                                        triggerText,
                                                        socialSetting,
                                                        latitude,
                                                        longitude,
                                                        placeName);
                newMoodEvent.setPostType(selectedPostType); // Set post type

                if (selectedPhotoUri != null) {
                    // Handle photo upload and set URI in newMoodEvent (if needed)
                    String filename = uploadImageToFirebase();
                    newMoodEvent.setPhotoFilename(filename);
                }

                userProfile.addMoodEvent(newMoodEvent, success -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Mood saved successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save mood.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String uploadImageToFirebase() {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedPhotoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] imageData = baos.toByteArray();

            // 🔧 keep compressing if > 65536 bytes
            int quality = 90;
            while (imageData.length > 65536 && quality > 10) {
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                imageData = baos.toByteArray();
                quality -= 10;
            }

            //FirebaseStorage storage = FirebaseStorage.getInstance();
            String filename = "images/" + userProfile.getId() + System.currentTimeMillis() + "_mood.jpg";
            StorageReference storageRef = FirebaseDB.getInstance(requireContext()).getReference(filename);
            //StorageReference storageRef = storage.getReference()
                    //.child(filename);

            UploadTask uploadTask = storageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
            }).addOnFailureListener(e -> {
            });

            return filename;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteImageFromFirebase(String filename) {
        if (filename == null || filename.isEmpty()) {
            Log.e("FirebaseStorage", "Filename is null or empty.");
            return;
        }

        StorageReference storageRef = FirebaseDB.getInstance(requireContext()).getReference(filename);
        //FirebaseStorage storage = FirebaseStorage.getInstance();
        //StorageReference storageRef = storage.getReference().child(filename);

        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseStorage", "File deleted successfully: " + filename);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Error deleting file: " + filename, e);
                });
    }


    private void updateUIForExistingMood() {
        if (moodEvent == null) return;

        highlightMoodButton(getView(), getMoodButtonId(moodEvent.getEmotionalState().getName()));
        highlightSocialButton(getSocialButton(moodEvent.getSocialSituation()));
        triggerInput.setText(moodEvent.getTrigger());

        if ("Private".equalsIgnoreCase(moodEvent.getPostType())) {
            selectedPostType = "Private";
            highlightPostTypeButton(privatePostButton);
            publicPostButton.setAlpha(0.8f);
        } else {
            selectedPostType = "Public";
            highlightPostTypeButton(publicPostButton);
            privatePostButton.setAlpha(0.8f);
        }

    }

    private void setupDeleteButton() {
        deleteMoodButton.setOnClickListener(v -> {
            if (moodEvent == null || moodEvent.getId() == null) {
                Toast.makeText(requireContext(), "No mood event selected to delete.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        userProfile.deleteMoodEvent(moodEvent.getId(), success -> {
                            if (success) {
                                Toast.makeText(requireContext(), "Mood event deleted successfully!", Toast.LENGTH_SHORT).show();
                                if (getFragmentManager() != null) {
                                    getFragmentManager().popBackStack();
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete mood event.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void enableButton(Button button, boolean enable) {
        button.setEnabled(enable);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, requestCode);
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasPermission(Manifest.permission.CAMERA)) permissionsNeeded.add(Manifest.permission.CAMERA);
        if (!hasPermission(Manifest.permission.READ_MEDIA_IMAGES)) permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsNeeded.toArray(new String[0]), LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableButton(takePhotoButton, true);
            enableButton(choosePhotoButton, true);
            enableButton(addLocationButton, true);
        }
    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE ||
                requestCode == CAMERA_PERMISSION_REQUEST_CODE ||
                requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) enableButton(addLocationButton, true);
                    if (permissions[i].equals(Manifest.permission.CAMERA)) enableButton(takePhotoButton, true);
                    if (permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES)) enableButton(choosePhotoButton, true);
                } else {
                    handlePermissionDenied(permissions[i], permissions[i].substring(19));
                }
            }
        }
    }

    /**
     * Sets up the post type selection buttons.
     */
    private void setupPostTypeButtons() {
        highlightPostTypeButton(publicPostButton);
        privatePostButton.setAlpha(0.8f);

        publicPostButton.setOnClickListener(v -> {
            if (!"Public".equals(selectedPostType)) {
                selectedPostType = "Public";
                highlightPostTypeButton(publicPostButton);
                privatePostButton.setAlpha(0.8f);
            }
        });

        privatePostButton.setOnClickListener(v -> {
            if (!"Private".equals(selectedPostType)) {
                selectedPostType = "Private";
                highlightPostTypeButton(privatePostButton);
                publicPostButton.setAlpha(0.8f);
            }
        });
    }

    private void highlightPostTypeButton(Button selectedButton) {
        publicPostButton.setAlpha(0.8f);
        privatePostButton.setAlpha(0.8f);
        selectedButton.setAlpha(1.0f);
    }
}
