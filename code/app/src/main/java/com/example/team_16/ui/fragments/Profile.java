package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.PersonalMoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.adapters.MoodHistoryAdapter;

import java.util.List;

/**
 * Profile fragment that displays the user's profile along with its mood feed
 */
public class Profile extends Fragment {

    private TextView username;
    private TextView userHandle;
    private TextView followingStats;
    private TextView followersStats;
    private UserProfile userProfile;
    private List<MoodEvent> moodEvents;
    private MoodHistoryAdapter adapter;
    private RecyclerView moodHistoryRecyclerView;

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Get current user profile from the Application
        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        // Initialize and setup UI elements
        initializeViews(view);
        setupMoodHistoryRecyclerView();
        setupProfileInfo();
        setupClickListeners();

        // Fetch follower/following counts from Firebase
        refreshCounts();
    }

    private void initializeViews(View view) {
        username = view.findViewById(R.id.userName);
        userHandle = view.findViewById(R.id.userHandle);
        followingStats = view.findViewById(R.id.followingStats);
        followersStats = view.findViewById(R.id.followersStats);
        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
    }

    /**
     * Sets up the RecyclerView to show the user's personal mood history.
     */
    private void setupMoodHistoryRecyclerView() {
        PersonalMoodHistory personalMoodHistory = userProfile.getPersonalMoodHistory();
        moodEvents = personalMoodHistory.getAllEvents();

        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryRecyclerView.setNestedScrollingEnabled(true);

        adapter = new MoodHistoryAdapter(getContext(), moodEvents);
        moodHistoryRecyclerView.setAdapter(adapter);

        // Handle item clicks
        adapter.setOnItemClickListener(event -> {
            MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, moodDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     * Displays basic profile info (Full name, username).
     */
    private void setupProfileInfo() {
        username.setText(userProfile.getFullName());
        userHandle.setText(userProfile.getUsername());
    }

    /**
     * Sets up click listeners for the follower/following stats.
     */
    private void setupClickListeners() {
        // Navigate to the "Follow Requests" screen on clicking the followers count
        followersStats.setOnClickListener(v -> {
            ((HomeActivity) requireActivity()).navigateToFragment(
                    new FollowRequestsFragment(),
                    "Follow Requests"
            );
        });

        // Navigate to the "Following" screen on clicking the following count
        followingStats.setOnClickListener(v -> {
            ((HomeActivity) requireActivity()).navigateToFragment(
                    new FollowingFragment(),
                    "Following"
            );
        });
    }

    /**
     * Fetches the latest followers and following counts from Firebase
     * and updates their respective TextViews.
     */
    private void refreshCounts() {
        // Refresh followers count
        FirebaseDB.getInstance(requireContext())
                .getFollowersOfUser(userProfile.getId(), followers -> {
                    if (followers != null) {
                        followersStats.setText(followers.size() + " Followers");
                    } else {
                        followersStats.setText("0 Followers");
                    }
                });

        // Refresh following count
        FirebaseDB.getInstance(requireContext())
                .getFollowingList(userProfile.getId(), following -> {
                    if (following != null) {
                        followingStats.setText(following.size() + " Following");
                    } else {
                        followingStats.setText("0 Following");
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).setToolbarTitle("Profile");

        // Optionally refresh counts whenever we resume
        refreshCounts();
    }
}
