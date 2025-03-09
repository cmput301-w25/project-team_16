package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.AddMood;
import com.example.team_16.ui.fragments.Feed;
import com.example.team_16.ui.fragments.Maps;
import com.example.team_16.ui.fragments.Profile;
import com.example.team_16.ui.fragments.Search;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private boolean isNavigatingFragments = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        EdgeToEdge.enable(this);

        // Get the user profile from application
        UserProfile userProfile = ((MoodTrackerApp) getApplication()).getCurrentUserProfile();

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
        // Hide the default title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // Find the custom TextView and use it for the title
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Feed");

        // Only show back button when we have fragments in back stack
        updateBackButtonVisibility();

        // Handle back button clicks
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Register back callback (instead of overriding onBackPressed)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if we have any fragments in the back stack
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    // Pop the fragment from the back stack
                    getSupportFragmentManager().popBackStack();
                } else if (isNavigatingFragments) {
                    // If we're in a bottom navigation fragment and there's nothing in back stack,
                    // return to the Feed tab instead of closing app
                    if (bottomNavigationView.getSelectedItemId() != R.id.nav_feed) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_feed);
                    } else {
                        // We're already at the Feed tab, so proceed with normal back behavior
                        this.setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                        this.setEnabled(true);
                    }
                } else {
                    // No fragments in back stack, normal back behavior
                    this.setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    this.setEnabled(true);
                }
            }
        });

        // Add a back stack listener to update back button visibility
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                updateBackButtonVisibility();
            }
        });

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
                title = "Maps";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new Profile();
                title = "Profile";
            }

            if (selectedFragment != null) {
                // Set activity title
                toolbarTitle.setText(title);

                // Mark that we're handling navigation ourselves
                isNavigatingFragments = true;

                // Clear the back stack when navigating to a main tab
                clearBackStack();

                // Replace the fragment without adding to back stack
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

    /**
     * Clear the entire back stack
     */
    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    // Update back button visibility based on back stack
    private void updateBackButtonVisibility() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // We have fragments in back stack, show back button
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            // No fragments in back stack, hide back button
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    // Use this method to navigate to a different fragment that should be added to back stack
    public void navigateToFragment(Fragment fragment, String title) {
        setToolbarTitle(title);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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

    // Method to hide the toolbar
    public void hideToolbar() {
        if (toolbar.getVisibility() == View.VISIBLE) {
            toolbar.setVisibility(View.GONE);
        }
    }

    // Method to show the toolbar
    public void showToolbar() {
        if (toolbar.getVisibility() == View.GONE) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    // Method to change toolbar title
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
            toolbarTitle.setText(title);
        }
    }

    // Method to customize toolbar visibility
    public void setToolbarVisibility(boolean isVisible) {
        toolbar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}