package com.example.team_16.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
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
    private TextView triggerCounter;
    private Button saveMoodButton, deleteMoodButton, takePhotoButton, choosePhotoButton, addLocationButton;
    private Button aloneButton, onePersonButton, twoPersonButton, crowdButton;
    private Uri selectedPhotoUri;
    private String selectedMood, socialSetting;
    private Location selectedLocation;

    private static final int PICK_PHOTO_REQUEST = 1;
    private boolean isEditMode = false; // flag for adding or editing

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
        setupSaveButton();
        setupDeleteButton();
        setupPostTypeButtons();

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
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO_REQUEST);
        });

        takePhotoButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Camera functionality not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

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
            if (!validateInputs()) return;

            EmotionalState emotionalState = EmotionalStateRegistry.getByName(selectedMood);
            if (emotionalState == null) {
                Toast.makeText(requireContext(), "Invalid mood selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            String triggerText = triggerInput.getText().toString().trim();

            if (isEditMode) {
                moodEvent.setEmotionalState(emotionalState);
                moodEvent.setTrigger(triggerText);
                moodEvent.setSocialSituation(socialSetting);
                moodEvent.setPostType(selectedPostType);

                if (selectedPhotoUri != null) {
                    // TODO: Upload photo to Firebase Storage and update moodEvent with URI if needed.
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
                MoodEvent newMoodEvent = new MoodEvent(userProfile.getId(), emotionalState, triggerText, socialSetting);
                newMoodEvent.setPostType(selectedPostType); // Set post type

                if (selectedPhotoUri != null) {
                    // TODO: Upload photo to Firebase Storage and update newMoodEvent with URI if needed.
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
