package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;

import com.example.team_16.ui.adapters.FeedAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Feed extends Fragment implements FilterableFragment, FilterFragment.FilterListener {

    private RecyclerView recyclerView;
    private UserProfile userProfile;
    private LinearLayout emptyState;

    private List<MoodEvent> fullMoodEvents;
    private List<MoodEvent> moodEvents;
    private FeedAdapter adapter;
    private FilterFragment.FilterCriteria currentCriteria = null;

    public Feed() {
        // constructor
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

        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
        List<MoodEvent> events = followingMoodHistory.getAllEvents();
        Collections.reverse(events);
        fullMoodEvents = new ArrayList<>(events);
        moodEvents = new ArrayList<>(fullMoodEvents);
    }

    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyState = view.findViewById(R.id.emptyState);
        recyclerView = view.findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(true);

        adapter = new FeedAdapter(getContext(), moodEvents);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(event -> {
            MoodDetails moodDetailsFragment = MoodDetails.newInstance(event.getId());

            // Replace direct fragment transaction with navigateToFragment
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToFragment(moodDetailsFragment, "Mood Details");
            }
        });
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (moodEvents.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
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
        currentCriteria = criteria;
        applyFilter(criteria);
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onResetFilter() {
        currentCriteria = null;
        moodEvents = new ArrayList<>(fullMoodEvents);
        if (adapter != null) {
            adapter.updateData(moodEvents);
        }
        updateEmptyState();
    }

    private void applyFilter(FilterFragment.FilterCriteria criteria) {
        List<MoodEvent> filtered = new ArrayList<>();
        Date currentDate = new Date();

        for (MoodEvent event : fullMoodEvents) {
            boolean matches = true;

            // Time Period Filter
            if (!criteria.timePeriod.equals("All Time")) {
                Date eventDate = event.getTimestamp().toDate();
                long diff = currentDate.getTime() - eventDate.getTime();
                long daysDiff = diff / (1000 * 60 * 60 * 24);

                if (criteria.timePeriod.equals("Last Year") && daysDiff > 365) {
                    matches = false;
                } else if (criteria.timePeriod.equals("Last Month") && daysDiff > 30) {
                    matches = false;
                } else if (criteria.timePeriod.equals("Last Week") && daysDiff > 7) {
                    matches = false;
                }
            }

            // Emotional State Filter
            if (matches && criteria.emotionalState != null) {
                if (!criteria.emotionalState.equalsIgnoreCase(event.getEmotionalState().getName())) {
                    matches = false;
                }
            }

            // Trigger Reason Filter
            if (matches && !TextUtils.isEmpty(criteria.triggerReason)) {
                if (event.getTrigger() == null || !event.getTrigger().toLowerCase().contains(criteria.triggerReason.toLowerCase())) {
                    matches = false;
                }
            }


            if (matches) {
                filtered.add(event);
            }
        }

        moodEvents = filtered;
        if (adapter != null) {
            adapter.updateData(moodEvents);
        }
        updateEmptyState();
    }
    private void loadData() {
        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
        List<MoodEvent> events = followingMoodHistory.getAllEvents();
        Collections.reverse(events);
        fullMoodEvents = new ArrayList<>(events);
        moodEvents = new ArrayList<>(fullMoodEvents);

        if (currentCriteria != null) {
            applyFilter(currentCriteria);
        } else {
            if (adapter != null) {
                adapter.updateData(moodEvents);
                updateEmptyState();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadData();
        HomeActivity homeActivity = (HomeActivity) requireActivity();
        if (homeActivity.getCurrentNavItemId() == R.id.nav_feed) {
            homeActivity.setToolbarTitle("Feed");
        }
    }

}