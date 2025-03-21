package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.AddMood;
import com.example.team_16.ui.fragments.Feed;
import com.example.team_16.ui.fragments.FilterFragment;
import com.example.team_16.ui.fragments.FilterableFragment;
import com.example.team_16.ui.fragments.Maps;
import com.example.team_16.ui.fragments.Profile;
import com.example.team_16.ui.fragments.Search;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private ImageView filterIcon;
    private TextView toolbarTitle;
    private boolean isNavigatingFragments = false;

    private int previousNavItemId = -1;
    private int currentNavItemId = R.id.nav_feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Enable edge-to-edge layout
        //EdgeToEdge.enable(this);

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

        // Initialize custom back button
        ImageView backButton = toolbar.findViewById(R.id.navigation_icon);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        //initialize toolbar
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        // Filter icon
        filterIcon = toolbar.findViewById(R.id.filter_icon);
        filterIcon.setOnClickListener(v -> handleFilterClick());

        // Initial update
        updateBackButtonVisibility();
    }

    public int getCurrentNavItemId() {
        return currentNavItemId;
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

            previousNavItemId = currentNavItemId;
            currentNavItemId = itemId;
            showBottomNavigation();

            if (itemId == R.id.nav_feed) {
                selectedFragment = new Feed();
                title = "Feed";
                makeToolbarScrollable();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new Search();
                title = "Search";
                makeToolbarUnscrollable();

            } else if (itemId == R.id.nav_add) {
                selectedFragment = new AddMood();
                title = "Add Mood";
                makeToolbarUnscrollable();
            } else if (itemId == R.id.nav_maps) {
                selectedFragment = new Maps();
                title = "Maps";
                makeToolbarUnscrollable();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new Profile();
                title = "Profile";
                makeToolbarScrollable();
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

        // Force UI elements to be visible when navigating
        showToolbar();
        showBottomNavigation();

        clearBackStack();

        int enterAnim = R.anim.slide_in_right;
        int exitAnim = R.anim.slide_out_left;

        Menu menu = bottomNavigationView.getMenu();
        int previousOrder = menu.findItem(previousNavItemId).getOrder();
        int currentOrder = menu.findItem(itemId).getOrder();

        if (currentOrder < previousOrder) {
            enterAnim = R.anim.slide_in_left;
            exitAnim = R.anim.slide_out_right;
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        enterAnim,
                        exitAnim,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateFilterIconVisibility(itemId);

        // Reset scroll position using the direct ID
        resetScrollPosition();
    }

    /**
     * Navigate to another fragment and add it to the back stack (will show the back arrow).
     */
    public void navigateToFragment(Fragment fragment, String title) {
        // Force UI elements to be visible when navigating
        showToolbar();
        showBottomNavigation();

        setToolbarTitle(title);
        hideBottomNavigation();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        // Reset scroll position
        resetScrollPosition();
    }

    /**
     * Helper method to reset the scroll position
     */
    private void resetScrollPosition() {
        // Find the fragment container
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            // Get its parent - should be the NestedScrollView
            View parent = (View) fragmentContainer.getParent();
            if (parent instanceof NestedScrollView) {
                NestedScrollView scrollView = (NestedScrollView) parent;
                scrollView.smoothScrollTo(0, 0);
            }
        }
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

                    // After popping, check if we're on one of the main tabs
                    if (fm.getBackStackEntryCount() == 0) {
                        checkAndShowBottomNav();
                    }
                } else if (isNavigatingFragments) {
                    // If we're in a bottom nav tab with no back stack, go to 'Feed' if not on it
                    if (bottomNavigationView.getSelectedItemId() != R.id.nav_feed) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_feed);
                        showBottomNavigation(); // Ensure bottom nav is invisible
                        makeToolbarScrollable();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        });

        // Listen for back stack changes to update the toolbar/back arrow/filter icon
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateBackButtonVisibility();
            updateFilterIconFromCurrentFragment();
            checkAndShowBottomNav();

            // Update toolbar title based on current fragment type
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof Feed) {
                makeToolbarScrollable();
                setToolbarTitle("Feed");
            } else if (currentFragment instanceof Profile) {
                makeToolbarScrollable();
                setToolbarTitle("Profile");
            } else if (currentFragment instanceof Maps) {
                makeToolbarUnscrollable();
                setToolbarTitle("Maps");
            } else if (currentFragment instanceof Search) {
                setToolbarTitle("Search");
                makeToolbarUnscrollable();
            } else if (currentFragment instanceof AddMood) {
                makeToolbarUnscrollable();
                setToolbarTitle("Add Mood");
            } else if (currentFragment instanceof FilterFragment) {
                makeToolbarUnscrollable();
                setToolbarTitle("Filter");
            }
            // ... add other fragments as needed
        });

    }

    /**
     * Check if current fragment is one of the main 5 tabs and show bottom nav if needed
     */
    private void checkAndShowBottomNav() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        // Check if the current fragment is one of the main tabs
        boolean isMainTab = (currentFragment instanceof Feed ||
                currentFragment instanceof Search ||
                currentFragment instanceof AddMood ||
                currentFragment instanceof Maps ||
                currentFragment instanceof Profile);

        if (isMainTab) {
            showBottomNavigation();
        }
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


        // Update custom back button visibility
        ImageView backButton = toolbar.findViewById(R.id.navigation_icon);
        backButton.setVisibility(canGoBack ? View.VISIBLE : View.GONE);

        // Keep title centered using proper layout param handling
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        toolbarTitle.setLayoutParams(params);
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
     * Update the toolbar title text.
     */
    public void setToolbarTitle(String title) {
        if (toolbar != null && toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }

    /**
     * Hide the toolbar with animation.
     */
    public void hideToolbar() {
        if (toolbar != null && toolbar.getVisibility() == View.VISIBLE) {
            // Try to collapse the AppBarLayout
            AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
            if (appBarLayout != null) {
                // Collapse first, then hide
                appBarLayout.setExpanded(false, true);

                // Optionally hide completely after animation
                // Uncomment if you want the toolbar to be completely hidden
                // appBarLayout.postDelayed(() -> toolbar.setVisibility(View.GONE), 250);
            }
        }
    }

    /**
     * Show the toolbar if it was hidden.
     */
    public void showToolbar() {
        if (toolbar != null && toolbar.getVisibility() == View.GONE) {
            // Make sure it's visible first
            toolbar.setVisibility(View.VISIBLE);
        }

        // Try to expand the AppBarLayout if it exists and is not already expanded
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, true);
        }
    }



    /**
     * Hide the bottom navigation bar.
     */
    public void hideBottomNavigation() {
        if (bottomNavigationView != null && bottomNavigationView.getVisibility() == View.VISIBLE) {
            // Animate hiding
            bottomNavigationView.animate()
                    .translationY(bottomNavigationView.getHeight())
                    .setDuration(200)
                    .withEndAction(() -> bottomNavigationView.setVisibility(View.GONE))
                    .start();
        }
    }

    /**
     * Show the bottom navigation if it was hidden.
     */
    public void showBottomNavigation() {
        if (bottomNavigationView != null &&
                (bottomNavigationView.getVisibility() == View.GONE || bottomNavigationView.getTranslationY() > 0)) {
            // Make sure it's visible first
            bottomNavigationView.setVisibility(View.VISIBLE);
            // Animate showing
            bottomNavigationView.animate()
                    .translationY(0)
                    .setDuration(200)
                    .start();
        }
    }

    /**
     * Makes the toolbar unscrollable by adjusting the AppBarLayout behavior.
     * Call this method when you want to prevent the toolbar from scrolling with content.
     */
    public void makeToolbarUnscrollable() {
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        if (appBarLayout != null) {
            // Get the layout parameters that should be of type AppBarLayout.LayoutParams
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

            // Remove the scroll flags to make it fixed (unscrollable)
            params.setScrollFlags(0); // 0 means no scroll flags

            // Apply the updated layout parameters
            toolbar.setLayoutParams(params);

            // Ensure the toolbar is fully expanded and visible
            appBarLayout.setExpanded(true, false);
        }
    }

    /**
     * Makes the toolbar scrollable by setting appropriate scroll flags.
     * Call this method when you want to allow the toolbar to scroll with content.
     */
    public void makeToolbarScrollable() {
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        if (appBarLayout != null) {
            // Get the layout parameters
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

            // Set scroll flags to make it scrollable
            // AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL enables scrolling
            // AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS makes it reappear immediately when scrolling up
            params.setScrollFlags(
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            );

            // Apply the updated layout parameters
            toolbar.setLayoutParams(params);
        }
    }


}
