package com.example.team_16.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.PersonalMoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.adapters.FeedAdapter;
import com.example.team_16.ui.adapters.MoodHistoryAdapter;
import com.google.firebase.Timestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }
        PersonalMoodHistory personalMoodHistory = userProfile.getPersonalMoodHistory();
        List<MoodEvent> events = personalMoodHistory.getAllEvents();
        moodEvents = events;

        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView); // Use the class-level variable
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryRecyclerView.setNestedScrollingEnabled(true);

        // Initialize the adapter using the class-level field, and set it to the RecyclerView
        adapter = new MoodHistoryAdapter(getContext(), events);
        moodHistoryRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MoodHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MoodEvent event) {
                MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, moodDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        username = view.findViewById(R.id.userName);
        username.setText(userProfile.getFullName());

        userHandle = view.findViewById(R.id.userHandle);
        userHandle.setText(userProfile.getUsername());

        followingStats = view.findViewById(R.id.followingStats);
        followingStats.setText(userProfile.getUserFollowing().size() + " Following");

        followersStats = view.findViewById(R.id.followersStats);

        userProfile.getFollowingList(new FirebaseDB.FirebaseCallback<List<String>>() {
            @Override
            public void onCallback(List<String> followingList) {
                if (followingList != null && !followingList.isEmpty()) {
                    followersStats.setText(followingList.size() + " Followers");
                } else {
                    followersStats.setText("0 Followers");
                }
            }
        });

        followersStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) requireActivity()).navigateToFragment(
                        new FollowRequestsFragment(),
                        "Follow Requests"
                );
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).setToolbarTitle("Profile");
    }
}

