package com.example.team_16.ui.activity;

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
import com.example.team_16.ui.fragments.AddImage;
import com.example.team_16.ui.fragments.AddMood;
import com.example.team_16.ui.fragments.EntryFragment;
import com.example.team_16.ui.fragments.Feed;
import com.example.team_16.ui.fragments.FilterFragment;
import com.example.team_16.ui.fragments.FilterableFragment;
import com.example.team_16.ui.fragments.LoginFragment;
import com.example.team_16.ui.fragments.Maps;
import com.example.team_16.ui.fragments.Profile;
import com.example.team_16.ui.fragments.Search;
import com.example.team_16.ui.fragments.SignUp;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements
        EntryFragment.EntryFragmentListener,
        LoginFragment.LoginFragmentListener,
        SignUp.SignUpListener {

    private BottomNavigationView bottomNavigationView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private ImageView filterIcon;
    private TextView toolbarTitle;
    private boolean isNavigatingFragments = false;

    private int previousNavItemId = -1;
    private int currentNavItemId = R.id.nav_feed;

    // Flag to track auth state
    private boolean isLoggedIn = false;

    // FirebaseDB instance
    private FirebaseDB firebaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize FirebaseDB
        firebaseDB = FirebaseDB.getInstance(this);

        // Initialize toolbar
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        initializeToolbar();

        // Check for a valid user session
        UserProfile userProfile = ((MoodTrackerApp) getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            // Not logged in, first check if we should show entry (first launch)
            if (isFirstLaunch()) {
                showEntryFragment();
            } else {
                // Not first launch, go directly to login
                showLoginFragment();
            }
        } else {
            // User is logged in, set up main navigation
            isLoggedIn = true;
            setupMainNavigation(savedInstanceState);
        }

        // Handle back press logic
        setupBackPressHandling();
    }

    /**
     * Callback from EntryFragment when "Get Started" is clicked
     */
    @Override
    public void onGetStartedClicked() {
        showLoginFragment();
    }

    /**
     * Callback from LoginFragment when login is successful
     */
    @Override
    public void onLoginSuccess(String userId) {
        // Load the user profile and handle navigation
        loadUserProfileAndNavigate(userId);
    }

    /**
     * Callback from SignUp fragment when signup is successful
     * Just reuses the onLoginSuccess method
     */
    @Override
    public void onSignUpSuccess(String userId) {
        // Same behavior as login success
        onLoginSuccess(userId);
    }

    /**
     * Handles loading the user profile and setting up the app after login/signup
     */
    private void loadUserProfileAndNavigate(String userId) {
        UserProfile.loadFromFirebase(firebaseDB, userId, userProfile -> {
            if (userProfile != null) {
                // Store the user profile in the application
                ((MoodTrackerApp) getApplication()).setCurrentUserProfile(userProfile);

                // Update login state
                isLoggedIn = true;

                // Restore system bars
                showSystemBars();

                // Set up main navigation
                setupMainNavigation(null);

                // Navigate to Feed as the default screen
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_feed);
                }
            } else {
                // If loading fails - present an error and sign out
                Toast.makeText(HomeActivity.this,
                        "Error loading user profile. Please try again.",
                        Toast.LENGTH_SHORT).show();
                firebaseDB.logout();
            }
        });
    }

    /**
     * Show the entry/onboarding fragment
     */
    public void showEntryFragment() {
        // Hide bottom navigation when showing entry
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }

        // Hide system bars for immersive experience
        hideSystemBars();

        // Load entry fragment
        EntryFragment entryFragment = new EntryFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, entryFragment)
                .commit();
    }

    /**
     * Show the login fragment
     */
    public void showLoginFragment() {
        // Hide bottom navigation when showing login
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        hideBottomNavigation();
        hideToolbar();


        updateFilterIconVisibility(false);

        // Hide system bars for immersive experience
        hideSystemBars();

        // Load login fragment with fade animation
        LoginFragment loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out
                )
                .replace(R.id.fragment_container, loginFragment)
                .commit();
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
    private void setupMainNavigation(Bundle savedInstanceState) {

        // Make sure bottom navigation is visible
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

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
        makeToolbarUnscrollable();

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
                // If we're not logged in, handle back navigation specially
                if (!isLoggedIn) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                    if (currentFragment instanceof EntryFragment) {
                        // Let the entry fragment handle the back press with exit dialog
                        if (((EntryFragment) currentFragment).handleBackPress()) {
                            return;
                        }
                    } else if (currentFragment instanceof LoginFragment) {
                        // Try to handle back within the login fragment first
                        if (((LoginFragment) currentFragment).handleBackPress()) {
                            return;
                        }

                        // If we're coming from the entry screen originally, go back to it
                        if (isFirstLaunch()) {
                            showEntryFragment();
                            return;
                        }

                        // Otherwise, exit app
                        finish();
                        return;
                    }
                }

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
                        showBottomNavigation(); // Ensure bottom nav is visible
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
            else if (currentFragment instanceof AddImage) {
                makeToolbarUnscrollable();
                setToolbarTitle("Image Preview");
            } else if (currentFragment instanceof LoginFragment) {
                makeToolbarUnscrollable();
                setToolbarTitle("Login");
            } else if (currentFragment instanceof EntryFragment) {
                makeToolbarUnscrollable();
                setToolbarTitle("Welcome");
            }
        });
    }

    /**
     * Check if current fragment is one of the main 5 tabs and show bottom nav if needed
     */
    private void checkAndShowBottomNav() {
        if (!isLoggedIn) return;

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
     * Set filter icon visibility directly
     */
    private void updateFilterIconVisibility(boolean visible) {
        filterIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
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

        // Update login state
        isLoggedIn = false;

        // Show login fragment
        showLoginFragment();
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
        if (!isLoggedIn) return;

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

    /**
     * Check if this is the first launch of the app
     * @return true if first launch, false otherwise
     */
    private boolean isFirstLaunch() {
        // Use SharedPreferences to determine if this is the first launch
        boolean isFirstLaunch = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_first_launch", true);

        // If it is the first launch, update the preference for next time
        if (isFirstLaunch) {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_first_launch", false)
                    .apply();
        }

        return isFirstLaunch;
    }

    /**
     * Hides both the status bar and navigation bar for a more immersive experience
     */
    private void hideSystemBars() {
        // Hide both the navigation bar and the status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Cancel any animations and set visibility immediately
        if (bottomNavigationView != null) {
            bottomNavigationView.clearAnimation();
            bottomNavigationView.setVisibility(View.GONE);
            bottomNavigationView.setTranslationY(0);
        }

        // Hide toolbar immediately
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the status bar and navigation bar
     */
    private void showSystemBars() {
        // Show both the navigation bar and the status bar
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Show the toolbar
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }
}