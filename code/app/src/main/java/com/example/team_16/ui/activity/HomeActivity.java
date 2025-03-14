package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.example.team_16.ui.fragments.FilterableFragment;
import com.example.team_16.ui.fragments.Maps;
import com.example.team_16.ui.fragments.Profile;
import com.example.team_16.ui.fragments.Search;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private ImageView filterIcon;
    private TextView toolbarTitle;
    private boolean isNavigatingFragments = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Enable edge-to-edge layout
        EdgeToEdge.enable(this);

        // Check for a valid user session
        UserProfile userProfile = ((MoodTrackerApp) getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize toolbar
        initializeToolbar();

        // Set up bottom navigation
        setupNavigation(savedInstanceState);

        // Handle back press logic
        setupBackPressHandling();
    }

    /**
     * Initialize and configure the toolbar, including the filter icon.
     */
    private void initializeToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Hide the default ActionBar title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Custom title TextView
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Feed");

        // Filter icon
        filterIcon = toolbar.findViewById(R.id.filter_icon);
        filterIcon.setOnClickListener(v -> handleFilterClick());

        // Update back button visibility based on fragments in the back stack
        updateBackButtonVisibility();

        // If the toolbar's back arrow is clicked, call onBackPressedDispatcher
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Set up the bottom navigation with fragment switching.
     */
    private void setupNavigation(Bundle savedInstanceState) {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;
            String title = "";

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
                handleNavigation(selectedFragment, title, itemId);
                return true;
            }
            return false;
        });

        // Default to Feed tab on first load
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_feed);
        }
    }

    /**
     * Replace the current fragment with the selected one, clearing the back stack since these are main tabs.
     */
    private void handleNavigation(Fragment fragment, String title, int itemId) {
        toolbarTitle.setText(title);
        isNavigatingFragments = true;

        // Clear any back stack entries when switching main tabs
        clearBackStack();

        // Replace without adding to back stack for main tabs
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        // Show or hide the filter icon depending on the selected tab
        updateFilterIconVisibility(itemId);
    }

    /**
     * Handle back press logic with custom behavior for bottom navigation.
     */
    private void setupBackPressHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    // If there's something in the back stack, pop it
                    fm.popBackStack();
                } else if (isNavigatingFragments) {
                    // If we're in a bottom nav tab with no back stack, go to 'Feed' if not on it
                    if (bottomNavigationView.getSelectedItemId() != R.id.nav_feed) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_feed);
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        });

        // Listen for back stack changes so we can update the toolbar/back arrow/filter icon
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateBackButtonVisibility();
            updateFilterIconFromCurrentFragment();
        });
    }

    /**
     * Show/hide the filter icon based on which bottom nav item is selected.
     */
    private void updateFilterIconVisibility(int itemId) {
        // Example: show filter on these tabs only
        boolean showFilter = (itemId == R.id.nav_feed
                || itemId == R.id.nav_maps
                || itemId == R.id.nav_profile);

        filterIcon.setVisibility(showFilter ? View.VISIBLE : View.GONE);
    }

    /**
     * Show/hide the filter icon based on the current visible fragment (in case we navigate via back stack).
     */
    private void updateFilterIconFromCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FilterableFragment) {
            // Show filter icon if fragment implements FilterableFragment
            filterIcon.setVisibility(View.VISIBLE);
        } else {
            filterIcon.setVisibility(View.GONE);
        }
    }

    /**
     * When the user clicks the filter icon, call onFilterClicked() of the current fragment if it implements FilterableFragment.
     */
    private void handleFilterClick() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FilterableFragment) {
            ((FilterableFragment) currentFragment).onFilterClicked();
        }
    }

    /**
     * Clear the entire fragment back stack.
     */
    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Update the toolbar back arrow based on back stack count.
     */
    private void updateBackButtonVisibility() {
        boolean canGoBack = (getSupportFragmentManager().getBackStackEntryCount() > 0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(canGoBack);
        getSupportActionBar().setDisplayShowHomeEnabled(canGoBack);
    }

    /**
     * Navigate to another fragment and add it to the back stack (will show the back arrow).
     */
    public void navigateToFragment(Fragment fragment, String title) {
        setToolbarTitle(title);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Programmatically set the selected bottom navigation item.
     */
    public void setSelectedNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }

    /**
     * Log out the user and go back to the login screen.
     */
    public void logout() {
        // Clear the user profile
        ((MoodTrackerApp) getApplication()).clearCurrentUserProfile();
        FirebaseDB.getInstance(this).logout();

        // Return to MainActivity (Login screen)
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Hide the toolbar entirely.
     */
    public void hideToolbar() {
        if (toolbar.getVisibility() == View.VISIBLE) {
            toolbar.setVisibility(View.GONE);
        }
    }

    /**
     * Show the toolbar if it was hidden.
     */
    public void showToolbar() {
        if (toolbar.getVisibility() == View.GONE) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update the toolbar title text.
     */
    public void setToolbarTitle(String title) {
        if (toolbar != null && toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }

    /**
     * Control toolbar visibility directly.
     */
    public void setToolbarVisibility(boolean isVisible) {
        toolbar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
