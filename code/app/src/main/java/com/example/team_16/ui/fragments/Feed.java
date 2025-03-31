/**
 * Feed Fragment
 *
 * This fragment displays a personalized feed of mood events posted by users the current user follows.
 * It provides real-time filtering options and integrates with a FilterFragment to narrow down mood entries.
 *
 * Core Features:
 * - Displays up to 3 recent mood events per followed user
 * - Allows filtering by emotional state, trigger text, and time period (All Time, Last Year, etc.)
 * - Handles dynamic UI updates based on filtering results
 * - Navigates to:
 *   - MoodDetails fragment when a mood event is clicked
 *   - OtherUserProfileFragment when a user profile is clicked
 * - Shows an empty state view when no results are available
 *
 * Integration:
 * - Works closely with HomeActivity for toolbar and fragment navigation
 * - Communicates with FilterFragment using the `FilterableFragment` interface
 *
 * Usage:
 * Appears under the bottom navigation "Feed" tab to let users browse updates from people they follow.
 */

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Feed extends Fragment implements FilterableFragment, FilterFragment.FilterListener {

    private RecyclerView recyclerView;
    private UserProfile userProfile;
    private LinearLayout emptyState;

    private List<MoodEvent> fullMoodEvents;
    private List<MoodEvent> moodEvents;
    private FeedAdapter adapter;
    private FilterFragment.FilterCriteria currentCriteria = null;

    public Feed() {

    }

    /**
     * Creates a new instance of the Feed fragment.
     *
     * @return A new instance of Feed fragment
     */
    public static Feed newInstance() {
        return new Feed();
    }

    /**
     * Initializes the fragment and loads the user's following feed data.
     * Retrieves mood events from followed users and sorts them by timestamp.
     */
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

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The View for the fragment's UI
     */
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned.
     * Sets up the RecyclerView, adapter, and click listeners for mood events and user profiles.
     *
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
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
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity())
                        .navigateToFragment(moodDetailsFragment, "Mood Details");
            }
        });

        adapter.setOnProfileClickListener(userId -> {
            OtherUserProfileFragment fragment = OtherUserProfileFragment.newInstance(userId);
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity())
                        .navigateToFragment(fragment, "User Profile");
            }
        });

        updateEmptyState();
    }

    /**
     * Updates the visibility of the empty state view and RecyclerView based on the presence of mood events.
     */
    private void updateEmptyState() {
        if (moodEvents.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when the filter button is clicked.
     * Launches the FilterFragment with appropriate arguments.
     */
    @Override
    public void onFilterClicked() {
        FilterFragment filterFragment = new FilterFragment();

        Bundle args = new Bundle();
        args.putBoolean("hide_event_type_filters", true);
        filterFragment.setArguments(args);

        filterFragment.setFilterListener(this);
        ((HomeActivity) requireActivity()).navigateToFragment(filterFragment, "Filter");
    }

    /**
     * Called when filter criteria are applied.
     * Updates the displayed mood events based on the filter criteria.
     *
     * @param criteria The filter criteria to apply
     */
    @Override
    public void onApplyFilter(FilterFragment.FilterCriteria criteria) {
        currentCriteria = criteria;
        applyFilter(criteria);
        getParentFragmentManager().popBackStack();
    }

    /**
     * Called when the filter is reset.
     * Restores the original list of mood events and updates the UI.
     */
    @Override
    public void onResetFilter() {
        currentCriteria = null;
        moodEvents = new ArrayList<>(fullMoodEvents);
        if (adapter != null) {
            adapter.updateData(moodEvents);
        }
        updateEmptyState();
    }

    /**
     * Applies the given filter criteria to the mood events list.
     * Filters events based on time period, emotional state, and trigger reason.
     *
     * @param criteria The filter criteria to apply
     */
    private void applyFilter(FilterFragment.FilterCriteria criteria) {
        List<MoodEvent> filtered = new ArrayList<>();
        Date currentDate = new Date();

        for (MoodEvent event : fullMoodEvents) {
            boolean matches = true;

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

            if (matches && criteria.emotionalState != null) {
                if (!criteria.emotionalState.equalsIgnoreCase(event.getEmotionalState().getName())) {
                    matches = false;
                }
            }

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

    /**
     * Loads and refreshes the feed data from Firebase.
     * Groups events by user and limits to 3 events per user.
     * Maintains current filter state if one is active.
     */
    private void loadData() {
        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();

        // Refresh from Firebase first
        followingMoodHistory.refresh(() -> {
            List<MoodEvent> allEvents = followingMoodHistory.getAllEvents();
            allEvents.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            Map<String, List<MoodEvent>> groupedByUser = new LinkedHashMap<>();
            for (MoodEvent event : allEvents) {
                String userId = event.getUserID();
                if (!groupedByUser.containsKey(userId)) {
                    groupedByUser.put(userId, new ArrayList<>());
                }
                if (groupedByUser.get(userId).size() < 3) {
                    groupedByUser.get(userId).add(event);
                }
            }

            List<MoodEvent> limitedEvents = new ArrayList<>();
            for (List<MoodEvent> userEvents : groupedByUser.values()) {
                limitedEvents.addAll(userEvents);
            }

            limitedEvents.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            fullMoodEvents = new ArrayList<>(limitedEvents);
            moodEvents = new ArrayList<>(fullMoodEvents);

            if (currentCriteria != null) {
                applyFilter(currentCriteria);
            } else {
                if (adapter != null) {
                    adapter.updateData(moodEvents);
                    updateEmptyState();
                }
            }
        });
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