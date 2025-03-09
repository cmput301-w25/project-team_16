package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.AddMood;
import com.example.team_16.ui.fragments.Feed;
import com.example.team_16.ui.fragments.Maps;
import com.example.team_16.ui.fragments.MoodDetails;
import com.example.team_16.ui.fragments.Profile;
import com.example.team_16.ui.fragments.Search;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private UserProfile userProfile;
    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        EdgeToEdge.enable(this);

        // Get the user profile from application
        userProfile = ((MoodTrackerApp) getApplication()).getCurrentUserProfile();

        // Check if user profile exists
        if (userProfile == null) {
            // No valid user session, redirect to login
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int itemId = item.getItemId();

            if (itemId == R.id.nav_feed) {
                selectedFragment = new Feed();
                title = "Feed";
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new Search();
                title = "Search";
            } else if (itemId == R.id.nav_add) {
                selectedFragment = new AddMood();
                title = "Add Mood";
            } else if (itemId == R.id.nav_maps) {
                selectedFragment = new Maps();
                title = "Notifications";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new Profile();
                title = "Profile";
            }

            if (selectedFragment != null) {
                // Set activity title
                setTitle(title);

                // Replace the fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Set default selection to Feed if this is the first creation
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_feed);
        }
    }

    // Method to programmatically select a navigation item
    public void setSelectedNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }

    // Logout method (can be called from fragments)
    public void logout() {
        // Clear user profile from app
        ((MoodTrackerApp) getApplication()).clearCurrentUserProfile();

        // Log out from Firebase
        FirebaseDB.getInstance(this).logout();

        // Return to login screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

//    public void loadMoodDetailsFragment(MoodEvent moodEvent) {
//        MoodDetails moodDetailsFragment = MoodDetails.newInstance("");
//        moodDetailsFragment.receiveData(moodEvent);
//
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, moodDetailsFragment)
//                .addToBackStack(null)  // Allows navigating back
//                .commit();
//    }

}