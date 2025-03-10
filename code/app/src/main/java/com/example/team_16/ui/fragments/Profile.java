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

    private RecyclerView moodHistoryRecyclerView;
    private MoodHistoryAdapter adapter;

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (make sure fragment_feed.xml exists)
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    //@SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // First, initialize userProfile
        // First, initialize userProfile
        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();

// Check if userProfile is null before proceeding
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

// Get the personal mood history from the user profile
        MoodHistory personalHistory = userProfile.getPersonalMoodHistory();

// Check if personal history is null before proceeding
        if (personalHistory == null) {
            Toast.makeText(requireContext(), "Failed to load mood history.", Toast.LENGTH_SHORT).show();
            return;
        }

// Set the callback to handle mood events once they are loaded
        personalHistory.setDataLoadCallback(events -> {
            // This will be called once the events are loaded
            List<MoodEvent> allEvents = personalHistory.getAllEvents();

            // Now you can work with allEvents, which contains the list of mood events
            if (allEvents != null && !allEvents.isEmpty()) {
                // Handle the events (e.g., update UI or pass the data to an adapter)
                for (MoodEvent event : allEvents) {
                    // Do something with each event
                    Log.d("MoodEvent", "Event ID: " + event.getId());
                }
            } else {
                Toast.makeText(requireContext(), "No mood events found.", Toast.LENGTH_SHORT).show();
            }
        });

// Optionally, refresh the data if needed
        personalHistory.refresh();


        // Initialize RecyclerView and adapter
        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryRecyclerView.setNestedScrollingEnabled(true);

        // Set the adapter with the hardcoded events list
        adapter = new MoodHistoryAdapter(getContext(), moodEvents);
        moodHistoryRecyclerView.setAdapter(adapter);


        username = view.findViewById(R.id.userName);
        username.setText(userProfile.getFullName());

        userHandle = view.findViewById(R.id.userHandle);
        userHandle.setText(userProfile.getUsername());

        followingStats = view.findViewById(R.id.followingStats);
        followingStats.setText(userProfile.getUserFollowing().size() + " Following");

        followersStats = view.findViewById(R.id.followersStats);
        // followersStats.setText(userProfile.getFollowers());

        // Ensure moodEvents is populated before setting the adapter
//        if (moodEvents!= null && !moodEvents.isEmpty()) {


//        } else {
//            Toast.makeText(requireContext(), "No mood events to display", Toast.LENGTH_SHORT).show();
//        }

        // Set onClickListener for followersStats (if needed)
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