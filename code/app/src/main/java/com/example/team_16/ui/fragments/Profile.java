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
import com.example.team_16.ui.adapters.MoodHistoryAdapter;
import com.google.firebase.Timestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profile extends Fragment {
    private TextView username;
    private TextView userHandle;
    private TextView followingStats;
    private TextView followersStats;

    private List<MoodEvent> moodEvents;
    private UserProfile userProfile;


    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    //@SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        MoodHistory personalMoodHistory = userProfile.getPersonalMoodHistory();

        List<MoodEvent> moodEvents = personalMoodHistory.getAllEvents();

        RecyclerView moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        MoodHistoryAdapter adapter = new MoodHistoryAdapter(getContext(), moodEvents);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryRecyclerView.setAdapter(adapter);

        NestedScrollView profileScrollView = view.findViewById(R.id.fragment_profile);
        moodHistoryRecyclerView.setNestedScrollingEnabled(true);

        username = view.findViewById(R.id.userName);
        username.setText(userProfile.getFullName());

        userHandle = view.findViewById(R.id.userHandle);
        userHandle.setText(userProfile.getUsername());

        followingStats = view.findViewById(R.id.followingStats);
        followingStats.setText(userProfile.getUserFollowing().size() + " Following");

        followersStats = view.findViewById(R.id.followersStats);

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


//        List<MoodEvent> events = new ArrayList<>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Timestamp timestamp = new Timestamp(Instant.now());
//            EmotionalState emotionalState = new EmotionalState("Happy"); // Create EmotionalState instance
//
//            MoodEvent event1 = new MoodEvent(
//                    "example_id",         // id (String)
//                    timestamp,            // timestamp (Timestamp)
//                    emotionalState,       // emotionalState (EmotionalState)
//                    "Feeling great!",     // trigger (String)
//                    "user_123",           // userID (String)
//                    "At work"             // socialSituation (String)
//            );
//            events.add(event1);
//        }