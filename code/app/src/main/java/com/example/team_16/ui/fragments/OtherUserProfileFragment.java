/**
 * OtherUserProfileFragment.java
 *
 * This fragment displays another user's public profile, including:
 * - Their profile image, name, and username
 * - Followers and following counts
 * - Mood statistics such as:
 *     - Total mood entries (only public ones)
 *     - Most frequently posted mood (emoji)
 * - A list of their public mood events shown in a RecyclerView
 * - A Follow/Unfollow/Pending button based on the relationship status
 *
 * Key Features:
 * - Uses `UserProfile.loadFromFirebase` to fetch the target user's data
 * - Filters out private mood events unless the profile is the current user
 * - Loads and displays mood history using `MoodHistoryAdapter`
 * - Determines the most frequent mood via frequency counting
 * - Uses FirebaseDB to manage follow requests and real-time status
 *
 * Usage:
 * - To open this fragment, use `OtherUserProfileFragment.newInstance(targetUserId)`
 * - Used typically from:
 *     - Followers/following list
 *     - Search results
 *     - Profile click on mood event cards
 *
 * Dependencies:
 * - Glide for loading profile images
 * - FirebaseDB for database access
 * - MoodTrackerApp for current user access
 * - MoodDetails fragment for viewing full mood event details
 *
 * Note:
 * - Follows the same UI pattern as the logged-in user's profile, but read-only
 * - Follow button state is dynamically set based on currentUser → targetUser relation
 */

package com.example.team_16.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
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

import com.bumptech.glide.Glide;
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

public class OtherUserProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "argUserId";

    private String targetUserId;
    private UserProfile targetUserProfile;

    private UserProfile currentUserProfile;

    private ShapeableImageView profileImage;
    private TextView userName;
    private TextView userHandle;
    private TextView followingStats;
    private TextView followersStats;
    private TextView totalMoodEntriesTxt;
    private TextView mostFrequentMoodTxt;
    private RecyclerView moodHistoryRecyclerView;
    private Button btnFollow;

    /**
     * Creates a new instance of the OtherUserProfileFragment.
     *
     * @param userId The ID of the user whose profile should be displayed
     * @return A new instance of OtherUserProfileFragment
     */
    public static OtherUserProfileFragment newInstance(String userId) {
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment and retrieves the target user ID from arguments.
     *
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The View for the fragment's UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other_user_profile, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned.
     * Sets up UI components and loads the target user's profile data.
     *
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Link up UI elements by ID
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        userHandle = view.findViewById(R.id.userHandle);
        followingStats = view.findViewById(R.id.followingStats);
        followersStats = view.findViewById(R.id.followersStats);
        totalMoodEntriesTxt = view.findViewById(R.id.totalMoodEntriesTxt);
        mostFrequentMoodTxt = view.findViewById(R.id.mostFrequentMoodTxt);
        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        btnFollow = view.findViewById(R.id.btnFollow);

        currentUserProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        FirebaseDB firebaseDB = FirebaseDB.getInstance(requireContext());
        UserProfile.loadFromFirebase(firebaseDB, targetUserId, profile -> {
            if (profile == null) {
                Toast.makeText(requireContext(), "Could not load user.", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
                return;
            }
            targetUserProfile = profile;

            displayUserProfile();

            refreshFollowButton();
        });
    }

    /**
     * Displays data for the target user, including:
     *  - Name / handle
     *  - Profile image
     *  - Followers & following counts
     *  - Mood stats (total, most frequent)
     *  - Mood history list
     */
    private void displayUserProfile() {
        String imageUrl = targetUserProfile.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.image)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.image);
        }

        userName.setText(targetUserProfile.getFullName());
        userHandle.setText("@" + targetUserProfile.getUsername());
        refreshFollowCounts();

        List<MoodEvent> allEvents = targetUserProfile.getPersonalMoodHistory().getAllEvents();

        if (!targetUserProfile.getId().equals(currentUserProfile.getId())) {
            List<MoodEvent> publicEvents = new ArrayList<>();
            for (MoodEvent event : allEvents) {
                if (!"Private".equals(event.getPostType())) {
                    publicEvents.add(event);
                }
            }
            allEvents = publicEvents;
        }

        totalMoodEntriesTxt.setText("Total Mood Entries: " + allEvents.size());

        // Get most frequent mood emoji
        String mostFrequentEmoji = getMostFrequentMoodEmoji(allEvents);
        mostFrequentMoodTxt.setText(mostFrequentEmoji != null ?
                "Most Frequent Mood: " + mostFrequentEmoji :
                "Most Frequent Mood: None");

        // Setup RecyclerView
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MoodHistoryAdapter adapter = new MoodHistoryAdapter(getContext(), allEvents);
        adapter.setCurrentUserId(currentUserProfile.getId());
        moodHistoryRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(event -> {
            MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, moodDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     * Determines the most frequently used mood emoji from a list of mood events.
     *
     * @param events The list of mood events to analyze
     * @return The emoji of the most frequent mood, or null if no events exist
     */
    private String getMostFrequentMoodEmoji(List<MoodEvent> events) {
        if (events.isEmpty()) return null;
        Map<String, Integer> moodCount = new HashMap<>();
        for (MoodEvent event : events) {
            String emoji = event.getEmotionalState().getEmoji();
            moodCount.put(emoji, moodCount.getOrDefault(emoji, 0) + 1);
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


    /**
     * Fetches and displays the follower and following counts.
     */
    private void refreshFollowCounts() {
        FirebaseDB.getInstance(requireContext())
                .getFollowersOfUser(targetUserProfile.getId(), followers -> {
                    if (followers != null) {
                        followersStats.setText(followers.size() + " Followers");
                    } else {
                        followersStats.setText("0 Followers");
                    }
                });

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
     * Determines the name of the most frequently used mood from a list of mood events.
     *
     * @param events The list of mood events to analyze
     * @return The name of the most frequent mood, or null if no events exist
     */
    private String getMostFrequentMoodName(List<MoodEvent> events) {
        if (events.isEmpty()) return null;
        Map<String, Integer> moodCount = new HashMap<>();
        for (MoodEvent event : events) {
            String moodName = event.getEmotionalState().getName();
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

    /**
     * Updates the follow button's appearance and behavior based on the current follow status.
     */
    private void refreshFollowButton() {
        if (currentUserProfile == null || targetUserProfile == null) return;
        currentUserProfile.isFollowing(targetUserProfile.getId(), isFollowing -> {
            if (isFollowing) {
                setButtonStateUnfollow();
            } else {
                if (currentUserProfile.getPendingFollow().contains(targetUserProfile.getId())) {
                    setButtonStatePending();
                } else {
                    setButtonStateFollow();
                }
            }
        });
    }

    private void setButtonStateFollow() {
        btnFollow.setText("Follow");
        btnFollow.setEnabled(true);
        btnFollow.setBackgroundResource(R.drawable.follow_button_bg);
        btnFollow.setTextColor(Color.parseColor("#4CAF50"));
        btnFollow.setOnClickListener(v -> {
            btnFollow.setEnabled(false);
            currentUserProfile.sendFollowRequest(targetUserProfile.getId(), success -> {
                if (success) {
                    Toast.makeText(getContext(), "Follow request sent", Toast.LENGTH_SHORT).show();
                    setButtonStatePending();
                } else {
                    Toast.makeText(getContext(), "Failed to send follow request", Toast.LENGTH_SHORT).show();
                    btnFollow.setEnabled(true);
                }
            });
        });
    }

    private void setButtonStatePending() {
        btnFollow.setText("Pending");
        btnFollow.setEnabled(false);
        btnFollow.setBackgroundResource(R.drawable.pending_button_bg);
        btnFollow.setTextColor(Color.parseColor("#1E293F"));
    }

    private void setButtonStateUnfollow() {
        btnFollow.setText("Unfollow");
        btnFollow.setEnabled(true);
        btnFollow.setBackgroundResource(R.drawable.unfollow_button_bg);
        btnFollow.setTextColor(Color.RED);
        btnFollow.setOnClickListener(v -> {
            btnFollow.setEnabled(false);
            currentUserProfile.unfollowUser(targetUserProfile.getId(), success -> {
                if (success) {
                    Toast.makeText(getContext(), "Unfollowed successfully", Toast.LENGTH_SHORT).show();
                    setButtonStateFollow();
                } else {
                    Toast.makeText(getContext(), "Failed to unfollow", Toast.LENGTH_SHORT).show();
                    btnFollow.setEnabled(true);
                }
            });
        });
    }

    /**
     * Handles the follow/unfollow action when the follow button is clicked.
     * Updates the follow status in Firebase and refreshes the UI.
     */
    private void handleFollowAction() {
        // Implementation of handleFollowAction method
    }
}
