package com.example.team_16.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.MoodDetails;

import com.google.firebase.Timestamp;

import java.util.Date;

public class MoodDetailsTestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        UserProfile userProfile = ((MoodTrackerApp) getApplication()).getCurrentUserProfile();

        MoodEvent moodEventOne = new MoodEvent(
                "mood123", // Unique ID
                new Timestamp(new Date()), // Current timestamp
                EmotionalStateRegistry.getByName("Happiness"),
                "Had a great day!",
                "user_001",
                "With colleagues"
        );

        MoodEvent moodEventTwo = new MoodEvent(
                "mood456", // Unique ID
                new Timestamp(new Date()), // Current timestamp
                EmotionalStateRegistry.getByName("Sadness"),
                "Lost my book.",
                "user_002",
                "Alone"
        );

        userProfile.addMoodEvent(moodEventOne);
        userProfile.addMoodEvent(moodEventTwo);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_test);

        if (savedInstanceState == null) {
            MoodDetails fragment = MoodDetails.newInstance("");
            fragment.receiveData(moodEventOne);
            Bundle args = new Bundle();
            args.putString("param1", "");
            fragment.setArguments(args);


            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
    }


}

