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
    private Button btnFollow;

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
        return inflater.inflate(R.layout.fragment_other_user_profile, container, false);
    }

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

        // Get current user profile from the application
        currentUserProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

        // Load the target user from Firestore
        FirebaseDB firebaseDB = FirebaseDB.getInstance(requireContext());
        UserProfile.loadFromFirebase(firebaseDB, targetUserId, profile -> {
            if (profile == null) {
                Toast.makeText(requireContext(), "Could not load user.", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
                return;
            }
            targetUserProfile = profile;

            // Display the target user's info, including profile image
            displayUserProfile();

            // Update the Follow/Unfollow button after user profile is loaded
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
        // Load and display the profile image using Glide
        String imageUrl = targetUserProfile.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.image)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.image);
        }

        // Set basic name/handle info
        userName.setText(targetUserProfile.getFullName());
        userHandle.setText("@" + targetUserProfile.getUsername());

        // Update follower/following counts
        refreshFollowCounts();

        // Display mood events and stats
        List<MoodEvent> allEvents = targetUserProfile.getPersonalMoodHistory().getAllEvents();
        totalMoodEntriesTxt.setText("Total Mood Entries: " + allEvents.size());

        String mostFrequent = getMostFrequentMoodName(allEvents);
        if (mostFrequent != null) {
            mostFrequentMoodTxt.setText("Most Frequent Mood: " + mostFrequent);
        } else {
            mostFrequentMoodTxt.setText("Most Frequent Mood: None");
        }

        // Populate the mood history RecyclerView
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MoodHistoryAdapter adapter = new MoodHistoryAdapter(getContext(), allEvents);
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
     * Returns the name of the most frequent mood in the given list.
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
     * Updates the follow/unfollow button state based on whether the current user
     * is following or has a pending request for the target user.
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
}
