package com.example.team_16.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.PersonalMoodHistory;
import com.example.team_16.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity to add a mood event, using PersonalMoodHistory and Firebase Firestore.
 */
public class AddMoodActivity extends AppCompatActivity {
    private MoodEvent moodEvent;
    private PersonalMoodHistory moodHistory;
    private FirebaseDB firebaseDB;
    private UserProfile userProfile;

    private EditText triggerInput;
    private Button saveMoodButton, deleteEntryButton, addPhotoButton, addLocationButton;
    private Button aloneButton, onePersonButton, twoPersonButton, crowdButton;
    private Uri selectedPhotoUri;
    private String selectedMood;
    private String socialSetting;
    private Location selectedLocation;

    private static final int PICK_PHOTO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_entry_screen);

        // Ensure user is logged in
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User must be logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseDB = FirebaseDB.getInstance(this);
        moodHistory = new PersonalMoodHistory(firebaseUser.getUid(), firebaseDB);

        // Load user profile
        firebaseDB.fetchUserById(firebaseUser.getUid(), userData -> {
            if (userData != null) {
                //userProfile = new UserProfile(firebaseUser.getUid(), userData);
            } else {
                Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // UI Elements
        triggerInput = findViewById(R.id.trigger_text);
        saveMoodButton = findViewById(R.id.save_mood_button);
        deleteEntryButton = findViewById(R.id.delete_entry_button);
        addPhotoButton = findViewById(R.id.choose_photo_button);
        addLocationButton = findViewById(R.id.add_location_button);

        // Social Setting Buttons
        aloneButton = findViewById(R.id.alone_button);
        onePersonButton = findViewById(R.id.one_person_button);
        twoPersonButton = findViewById(R.id.two_person_button);
        crowdButton = findViewById(R.id.crowd_button);

        // Setup event listeners
        setupMoodSelectionButtons();
        setupSocialSettingButtons();
        setupPhotoButton();
        //setupLocationButton(); haven't got to location yet
        setupSaveButton();
        setupDeleteButton();
    }

    /**
     * Ensures user selects a mood before saving.
     */
    private boolean validateInputs() {
        if (selectedMood == null) {
            Toast.makeText(this, "Please select a mood before saving.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (socialSetting == null) {
            Toast.makeText(this, "Please select who you were with.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Sets up click listeners for mood selection buttons.
     */
    private void setupMoodSelectionButtons() {
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
            findViewById(moodButtonIds[i]).setOnClickListener(v -> {
                selectedMood = moodNames[index];
                Toast.makeText(this, "Selected mood: " + moodNames[index], Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Sets up click listeners for social setting buttons.
     */
    private void setupSocialSettingButtons() {
        aloneButton.setOnClickListener(v -> socialSetting = "Alone");
        onePersonButton.setOnClickListener(v -> socialSetting = "One Person");
        twoPersonButton.setOnClickListener(v -> socialSetting = "Two People");
        crowdButton.setOnClickListener(v -> socialSetting = "Crowd");
    }

    /**
     * Opens photo picker.
     */
    private void setupPhotoButton() {
        addPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO_REQUEST);
        });
    }

    /**
     * Handles photo selection.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the mood event to Firebase and local storage.
     */
    private void setupSaveButton() {
        saveMoodButton.setOnClickListener(v -> {
            if (!validateInputs()) return; // Ensure mood and social setting are selected

            // Retrieve EmotionalState
            EmotionalState emotionalState = EmotionalStateRegistry.getByName(selectedMood);
            if (emotionalState == null) {
                Toast.makeText(this, "Invalid mood selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get trigger text (optional)
            String triggerText = triggerInput.getText().toString().trim();

            // Create MoodEvent with all optional and required fields
            moodEvent = new MoodEvent(userProfile.getId(), emotionalState, triggerText, socialSetting);

            // Add photo if selected
            if (selectedPhotoUri != null) {
                //moodEvent.setPhotoUri(selectedPhotoUri.toString());
                return;
            }

            // Save to FirebaseDB
            firebaseDB.addMoodEvent(moodEvent, success -> {
                if (success) {
                    Toast.makeText(this, "Mood saved to Firebase!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save mood to Firebase.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Deletes a mood event.
     */
    private void setupDeleteButton() {
        deleteEntryButton.setOnClickListener(v -> {
            if (moodEvent == null) {
                Toast.makeText(this, "No mood event to delete.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseDB.deleteMoodEvent(moodEvent.getId(), success -> {
                if (success) {
                    Toast.makeText(this, "Mood entry deleted!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to delete entry.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
