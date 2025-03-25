package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.adapters.MoodHistoryAdapter;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment to display another user's public profile info.
 */
public class OtherUserProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "argUserId";

    // The user we want to display
    private String targetUserId;
    private UserProfile targetUserProfile;

    // Current user (the one who is logged in) - needed for follow/unfollow checks
    private UserProfile currentUserProfile;

    // UI elements
    private ShapeableImageView profileImage;
    private TextView userName;
    private TextView userHandle;
    private TextView followingStats;
    private TextView followersStats;
    private TextView totalMoodEntriesTxt;
    private TextView mostFrequentMoodTxt;
    private RecyclerView moodHistoryRecyclerView;

    public static OtherUserProfileFragment newInstance(String userId) {
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // the updated layout that matches the profile page minus the Edit Profile
        return inflater.inflate(R.layout.fragment_other_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Link up all UI elements by ID
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        userHandle = view.findViewById(R.id.userHandle);
        followingStats = view.findViewById(R.id.followingStats);
        followersStats = view.findViewById(R.id.followersStats);
        totalMoodEntriesTxt = view.findViewById(R.id.totalMoodEntriesTxt);
        mostFrequentMoodTxt = view.findViewById(R.id.mostFrequentMoodTxt);
        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);

        // Current user
        currentUserProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        // 2) Load the target user from Firestore
        FirebaseDB firebaseDB = FirebaseDB.getInstance(requireContext());
        UserProfile.loadFromFirebase(firebaseDB, targetUserId, profile -> {
            if (profile == null) {
                Toast.makeText(requireContext(), "Could not load user.", Toast.LENGTH_SHORT).show();

                // Return to previous screen
                requireActivity().getSupportFragmentManager().popBackStack();
                return;
            }
            targetUserProfile = profile;

            // 3) Display the target user's info
            displayUserProfile();

        });
    }

    /**
     * Displays data for the target user, including:
     *  - Name / handle
     *  - Followers & following counts
     *  - Mood stats (total, most frequent)
     *  - Mood history list
     */
    private void displayUserProfile() {
        // Basic name/handle
        userName.setText(targetUserProfile.getFullName());
        userHandle.setText("@" + targetUserProfile.getUsername());

        // Show follower/following count
        refreshFollowCounts();

        // Display the user's mood events
        List<MoodEvent> allEvents = targetUserProfile
                .getPersonalMoodHistory()
                .getAllEvents();

        // Show total
        totalMoodEntriesTxt.setText("Total Mood Entries: " + allEvents.size());

        // Show most frequent mood
        String mostFrequent = getMostFrequentMoodName(allEvents);
        if (mostFrequent != null) {
            mostFrequentMoodTxt.setText("Most Frequent Mood: " + mostFrequent);
        } else {
            mostFrequentMoodTxt.setText("Most Frequent Mood: None");
        }

        // Populate RecyclerView
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MoodHistoryAdapter adapter = new MoodHistoryAdapter(getContext(), allEvents);
        moodHistoryRecyclerView.setAdapter(adapter);

        // If you want to open a details fragment on item click:
        adapter.setOnItemClickListener(event -> {
            // Show details for that mood
            MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, moodDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     * Fetch the actual follower/following counts and update the two TextViews.
     */
    private void refreshFollowCounts() {
        // Get # of followers
        FirebaseDB.getInstance(requireContext())
                .getFollowersOfUser(targetUserProfile.getId(), followers -> {
                    if (followers != null) {
                        followersStats.setText(followers.size() + " Followers");
                    } else {
                        followersStats.setText("0 Followers");
                    }
                });

        // Get # of following
        FirebaseDB.getInstance(requireContext())
                .getFollowingList(targetUserProfile.getId(), following -> {
                    if (following != null) {
                        followingStats.setText(following.size() + " Following");
                    } else {
                        followingStats.setText("0 Following");
                    }
                });
    }

    /**
     * Return the name of the most frequent mood in the list.
     */
    private String getMostFrequentMoodName(List<MoodEvent> events) {
        if (events.isEmpty()) return null;
        Map<String, Integer> moodCount = new HashMap<>();

        for (MoodEvent event : events) {
            EmotionalState mood = event.getEmotionalState();
            String moodName = mood.getName();
            moodCount.put(moodName, moodCount.getOrDefault(moodName, 0) + 1);
        }

        String mostFrequent = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : moodCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostFrequent = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return mostFrequent;
    }

    // Might add a follow and un follow button if team wants
    // Button would be on the teams profile
    /*
     */
}


