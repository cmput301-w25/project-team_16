package com.example.team_16.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

import java.util.Collections;
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

        // For older Android versions, initialize moodEvents another way
        List<MoodEvent> events = followingMoodHistory.getAllEvents();
        // Manually reverse the list for older Android versions
        Collections.reverse(events);
        moodEvents = events;

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

        adapter.setOnItemClickListener(new FeedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MoodEvent event) {
                MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());
                // Navigation.findNavController(getView()).navigate(R.id.action_feedFragment_to_moodDetailsFragment, bundle);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, moodDetailsFragment) // fragment_container is your host container
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

}
