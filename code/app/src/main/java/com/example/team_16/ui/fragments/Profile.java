package com.example.team_16.ui.fragments;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Profile extends Fragment implements FilterableFragment, FilterFragment.FilterListener {

    private TextView username;
    private TextView userHandle;
    private TextView followingStats;
    private TextView followersStats;
    private UserProfile userProfile;
    private RecyclerView moodHistoryRecyclerView;

    private List<MoodEvent> fullMoodEvents;
    private List<MoodEvent> filteredMoodEvents;
    private MoodHistoryAdapter adapter;

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Any initialization logic
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        initializeViews(view);
        setupMoodHistoryData();
        setupProfileInfo();
        setupClickListeners();
        refreshCounts();
    }

    private void initializeViews(View view) {
        username = view.findViewById(R.id.userName);
        userHandle = view.findViewById(R.id.userHandle);
        followingStats = view.findViewById(R.id.followingStats);
        followersStats = view.findViewById(R.id.followersStats);
        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
    }

    private void setupMoodHistoryData() {
        fullMoodEvents = userProfile.getPersonalMoodHistory().getAllEvents();

        Collections.reverse(fullMoodEvents);

        filteredMoodEvents = new ArrayList<>(fullMoodEvents);

        setupMoodHistoryRecyclerView();
    }

    private void setupMoodHistoryRecyclerView() {
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryRecyclerView.setNestedScrollingEnabled(true);

        adapter = new MoodHistoryAdapter(getContext(), filteredMoodEvents);
        moodHistoryRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(event -> {
            MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, moodDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupProfileInfo() {
        username.setText(userProfile.getFullName());
        userHandle.setText("@" + userProfile.getUsername());
    }

    private void setupClickListeners() {
        followersStats.setOnClickListener(v -> {
            ((HomeActivity) requireActivity()).navigateToFragment(
                    new FollowRequestsFragment(),
                    "Follow Requests"
            );
        });

        followingStats.setOnClickListener(v -> {
            ((HomeActivity) requireActivity()).navigateToFragment(
                    new FollowingFragment(),
                    "Following"
            );
        });
    }

    private void refreshCounts() {
        FirebaseDB.getInstance(requireContext())
                .getFollowersOfUser(userProfile.getId(), followers -> {
                    if (followers != null) {
                        followersStats.setText(followers.size() + " Followers");
                    } else {
                        followersStats.setText("0 Followers");
                    }
                });

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
    public void onFilterClicked() {
        FilterFragment filterFragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putBoolean("hide_event_type_filters", true);
        filterFragment.setArguments(args);
        filterFragment.setFilterListener(this);
        ((HomeActivity) requireActivity()).navigateToFragment(filterFragment, "Filter");
    }

    @Override
    public void onApplyFilter(FilterFragment.FilterCriteria criteria) {
        applyFilter(criteria);
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onResetFilter() {
        filteredMoodEvents = new ArrayList<>(fullMoodEvents);
        adapter.updateData(filteredMoodEvents);
    }

    private void applyFilter(FilterFragment.FilterCriteria criteria) {
        List<MoodEvent> newFilteredList = new ArrayList<>();
        Date now = new Date();

        for (MoodEvent event : fullMoodEvents) {
            boolean matches = true;

            if (!"All Time".equals(criteria.timePeriod)) {
                long diffMillis = now.getTime() - event.getTimestamp().toDate().getTime();
                long daysDiff = diffMillis / (1000 * 60 * 60 * 24);

                if ("Last Year".equals(criteria.timePeriod) && daysDiff > 365) {
                    matches = false;
                } else if ("Last Month".equals(criteria.timePeriod) && daysDiff > 30) {
                    matches = false;
                } else if ("Last Week".equals(criteria.timePeriod) && daysDiff > 7) {
                    matches = false;
                }
            }

            if (matches && criteria.emotionalState != null) {
                String eventMood = event.getEmotionalState().getName();
                if (!criteria.emotionalState.equalsIgnoreCase(eventMood)) {
                    matches = false;
                }
            }

            if (matches && criteria.triggerReason != null && !criteria.triggerReason.isEmpty()) {
                String trigger = event.getTrigger() == null ? "" : event.getTrigger().toLowerCase();
                if (!trigger.contains(criteria.triggerReason.toLowerCase())) {
                    matches = false;
                }
            }

            if (matches) {
                newFilteredList.add(event);
            }
        }

        filteredMoodEvents = newFilteredList;
        adapter.updateData(filteredMoodEvents);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).setToolbarTitle("Profile");
        refreshCounts();
    }
}