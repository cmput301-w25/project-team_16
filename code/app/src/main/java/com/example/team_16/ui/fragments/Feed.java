//package com.example.team_16.ui.fragments;
//
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import com.example.team_16.MoodTrackerApp;
//import com.example.team_16.R;
//import com.example.team_16.models.MoodEvent;
//import com.example.team_16.models.MoodHistory;
//import com.example.team_16.models.UserProfile;
//import com.example.team_16.ui.adapters.FeedAdapter;
//
//import java.util.List;
//
///**
// * Feed fragment that displays the user's mood feed with scroll behavior
// * to support hiding the toolbar and bottom navigation.
// */
//public class Feed extends Fragment {
//    private RecyclerView recyclerView;
//    private UserProfile userProfile;
//    private List<MoodEvent> moodEvents;
//
//    public Feed() {
//        // Required empty public constructor
//    }
//
//    public static Feed newInstance() {
//        return new Feed();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
//
//        // Check if user profile is available
//        if (userProfile == null) {
//            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
//            requireActivity().finish();
//            return;
//        }
//
//        //move below code into method once needed
//        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
//        moodEvents = followingMoodHistory.getAllEvents();
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_feed, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//
//
//        // Set up RecyclerView for scrolling behavior
//        recyclerView = view.findViewById(R.id.feed_recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        // Important: Enable nested scrolling for scroll behavior to work with CoordinatorLayout
//        recyclerView.setNestedScrollingEnabled(true);
//
//        // TODO: Set up your adapter
//         FeedAdapter adapter = new FeedAdapter(getContext(), moodEvents);
//         recyclerView.setAdapter(adapter);
//    }
//
//    public void showFollowedUserMoods() {
//
//    }
//
//}






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
import android.widget.Toast;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.adapters.FeedAdapter;

import java.util.List;

/**
 * Feed fragment that displays the user's mood feed.
 */
public class Feed extends Fragment {
    private RecyclerView recyclerView;
    private UserProfile userProfile;
    private List<MoodEvent> moodEvents;
    private FeedAdapter adapter;

    public Feed() {
        // Required empty public constructor
    }

    public static Feed newInstance() {
        return new Feed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        // Retrieve the list of mood events from the followed users' history
        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
        moodEvents = followingMoodHistory.getAllEvents();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (make sure fragment_feed.xml exists)
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Enable nested scrolling for proper behavior with a CoordinatorLayout
        recyclerView.setNestedScrollingEnabled(true);

        // Set up the adapter with the current list of mood events
        adapter = new FeedAdapter(getContext(), moodEvents);
        recyclerView.setAdapter(adapter);


        showFollowedUserMoods();
    }

    /**
     * Refreshes the list of mood events and updates the RecyclerView.
     */
    public void showFollowedUserMoods() {
        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
        moodEvents.clear();
        moodEvents.addAll(followingMoodHistory.getAllEvents());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
