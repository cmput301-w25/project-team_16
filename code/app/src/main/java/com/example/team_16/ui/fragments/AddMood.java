package com.example.team_16.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;



/**
 * Fragment to add a mood event, using UserProfile to manage mood events.
 */
public class AddMood extends Fragment {
    private MoodEvent moodEvent;
    private UserProfile userProfile;

    private EditText triggerInput;
    private Button saveMoodButton, deleteMoodButton, takePhotoButton, choosePhotoButton, addLocationButton;
    private Button aloneButton, onePersonButton, twoPersonButton, crowdButton;
    private Uri selectedPhotoUri;
    private String selectedMood, socialSetting;
    private Location selectedLocation;

    private static final int PICK_PHOTO_REQUEST = 1;
    private boolean isEditMode = false; //flag for adding or editing

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the UserProfile from Application context
        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        // Check if user profile is available
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_mood, container, false);
        // HomeActivity will handle the toolbar title based on navigation selection
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Elements
        triggerInput = view.findViewById(R.id.trigger_text);
        saveMoodButton = view.findViewById(R.id.save_mood_button);
        deleteMoodButton = view.findViewById(R.id.delete_entry_button);
        takePhotoButton = view.findViewById(R.id.take_photo_button);
        choosePhotoButton = view.findViewById(R.id.choose_photo_button);
        addLocationButton = view.findViewById(R.id.add_location_button);

        // Social Setting Buttons
        aloneButton = view.findViewById(R.id.alone_button);
        onePersonButton = view.findViewById(R.id.one_person_button);
        twoPersonButton = view.findViewById(R.id.two_person_button);
        crowdButton = view.findViewById(R.id.crowd_button);

        // Setup event listeners
        setupMoodSelectionButtons(view);
        setupSocialSettingButtons();
        setupPhotoButtons();
        //setupLocationButton(); // Haven't got to location yet
        setupSaveButton();
        setupDeleteButton();

        // If editing an existing MoodEvent, update the UI immediately
        if (isEditMode && moodEvent != null) {
            updateUIForExistingMood();
            deleteMoodButton.setVisibility(View.VISIBLE);
        } else {
            deleteMoodButton.setVisibility(View.GONE);
        }
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
            view.findViewById(moodButtonIds[i]).setOnClickListener(v -> {
                selectedMood = moodNames[index];
                Toast.makeText(requireContext(), "Selected mood: " + moodNames[index], Toast.LENGTH_SHORT).show();

                // Visual feedback for selection
                highlightMoodButton(view, moodButtonIds[index]);
            });
        }
    }

    /**
     * Visual feedback for selected mood button
     */
    private void highlightMoodButton(View view, int selectedButtonId) {
        int[] moodButtonIds = {
                R.id.anger_button, R.id.confusion_button, R.id.disgust_button,
                R.id.fear_button, R.id.happiness_button, R.id.sadness_button,
                R.id.shame_button, R.id.surprise_button
        };

        // Reset all buttons to default state
        for (int buttonId : moodButtonIds) {
            Button button = view.findViewById(buttonId);
            button.setAlpha(0.8f);
        }

        // Highlight the selected button
        Button selectedButton = view.findViewById(selectedButtonId);
        selectedButton.setAlpha(1.0f);
    }

    /**
     * Sets up click listeners for social setting buttons.
     */
    private void setupSocialSettingButtons() {
        aloneButton.setOnClickListener(v -> {
            socialSetting = "Alone";
            highlightSocialButton(aloneButton);
        });
        onePersonButton.setOnClickListener(v -> {
            socialSetting = "One Person";
            highlightSocialButton(onePersonButton);
        });
        twoPersonButton.setOnClickListener(v -> {
            socialSetting = "Two People";
            highlightSocialButton(twoPersonButton);
        });
        crowdButton.setOnClickListener(v -> {
            socialSetting = "Crowd";
            highlightSocialButton(crowdButton);
        });
    }

    /**
     * Visual feedback for selected social button
     */
    private void highlightSocialButton(Button selectedButton) {
        // Reset all buttons to default state
        aloneButton.setAlpha(0.8f);
        onePersonButton.setAlpha(0.8f);
        twoPersonButton.setAlpha(0.8f);
        crowdButton.setAlpha(0.8f);

        // Highlight the selected button
        selectedButton.setAlpha(1.0f);
    }

    private void populateUIWithExistingData() {
        triggerInput.setText(moodEvent.getTrigger());
        selectedMood = moodEvent.getEmotionalState().getName();
        socialSetting = moodEvent.getSocialSituation();

        highlightMoodButton(getView(), getMoodButtonId(selectedMood));
        highlightSocialButton(getSocialButton(socialSetting));
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
     * Opens photo picker.
     */
    private void setupPhotoButtons() {
        choosePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO_REQUEST);
        });

        takePhotoButton.setOnClickListener(v -> {
            // TODO: Implement camera functionality
            Toast.makeText(requireContext(), "Camera functionality not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Handles photo selection.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            Toast.makeText(requireContext(), "Photo selected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the mood event using UserProfile.
     */
    private void setupSaveButton() {
        saveMoodButton.setOnClickListener(v -> {
            if (!validateInputs()) return; // Ensure required fields are selected

            // Retrieve EmotionalState
            EmotionalState emotionalState = EmotionalStateRegistry.getByName(selectedMood);
            if (emotionalState == null) {
                Toast.makeText(requireContext(), "Invalid mood selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get trigger text (optional)
            String triggerText = triggerInput.getText().toString().trim();

            if (isEditMode) {
                // Update existing MoodEvent
                moodEvent.setEmotionalState(emotionalState);
                moodEvent.setTrigger(triggerText);
                moodEvent.setSocialSituation(socialSetting);

                if (selectedPhotoUri != null) {
                    // Handle photo upload and set URI in moodEvent (if needed)
                    // moodEvent.setPhotoUri(selectedPhotoUri.toString());
                    // TODO: Upload photo to Firebase Storage and update event
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
                MoodEvent newMoodEvent = new MoodEvent(userProfile.getId(), emotionalState, triggerText, socialSetting);

                if (selectedPhotoUri != null) {
                    // Handle photo upload and set URI in newMoodEvent (if needed)
                    // newMoodEvent.setPhotoUri(selectedPhotoUri.toString());
                    // TODO: Upload photo to Firebase Storage
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

    private void updateUIForExistingMood() {
        if (moodEvent == null) return;

        // Highlight selected mood
        highlightMoodButton(getView(), getMoodButtonId(moodEvent.getEmotionalState().getName()));

        // Highlight selected social setting
        highlightSocialButton(getSocialButton(moodEvent.getSocialSituation()));

        // Show the updated trigger text
        triggerInput.setText(moodEvent.getTrigger());
    }



    private void setupDeleteButton() {
        deleteMoodButton.setOnClickListener(v -> {
            if (moodEvent == null || moodEvent.getId() == null) {
                Toast.makeText(requireContext(), "No mood event selected to delete.", Toast.LENGTH_SHORT).show();
                return;
            }

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
        });
    }
}
