package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Maps extends Fragment implements FilterableFragment, FilterFragment.FilterListener {

    private UserProfile userProfile;
    private List<MoodEvent> fullMoodEvents;
    private List<MoodEvent> filteredMoodEvents;

    public Maps() {
        // Required empty public constructor
    }

    public static Maps newInstance() {
        return new Maps();
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

        fullMoodEvents = new ArrayList<>(userProfile.getFollowingMoodHistory().getAllEvents());
        Collections.reverse(fullMoodEvents);
        filteredMoodEvents = new ArrayList<>(fullMoodEvents);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateMapWithEvents(filteredMoodEvents);
    }

    @Override
    public void onFilterClicked() {
        FilterFragment filterFragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putBoolean("show_only_nearby_event_type", true);
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
        resetFilters();
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

            if (matches && criteria.emotionalState != null) {
                if (!criteria.emotionalState.equalsIgnoreCase(event.getEmotionalState().getName())) {
                    matches = false;
                }
            }

            if (matches && !criteria.triggerReason.isEmpty()) {
                if (event.getTrigger() == null || !event.getTrigger().toLowerCase().contains(criteria.triggerReason.toLowerCase())) {
                    matches = false;
                }
            }

            if (matches && criteria.eventType != null) {
                if (criteria.eventType.equals("Nearby Events within 5km")) {
                    // Implement location check here
                    if (!isEventWithin5Km(event)) {
                        matches = false;
                    }
                }
            }

            if (matches) {
                filtered.add(event);
            }
        }

        filteredMoodEvents = filtered;
        updateMapWithEvents(filteredMoodEvents);
    }

    private boolean isEventWithin5Km(MoodEvent event) {
        // Replace with actual location check logic
        return true;
    }

    private void resetFilters() {
        filteredMoodEvents = new ArrayList<>(fullMoodEvents);
        updateMapWithEvents(filteredMoodEvents);
    }

    private void updateMapWithEvents(List<MoodEvent> events) {
        // Update  map markers here with the filtered events
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setToolbarTitle("Map");
        }
    }
}