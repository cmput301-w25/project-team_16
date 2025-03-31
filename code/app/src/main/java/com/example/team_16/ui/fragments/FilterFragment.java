/**
 * FilterFragment.java
 *
 * This Fragment provides a flexible UI for users to filter mood events based on:
 * - Time Period (All Time, Last Year, Last Month, Last Week)
 * - Emotional State (Happiness, Sadness, etc.)
 * - Trigger Reason (text input)
 * - Event Types (Own Mood History, Events from People I Follow, Nearby Events)
 *
 * It supports two key use cases:
 * - Full filter UI (with all event type checkboxes)
 * - Condensed version (e.g., for Mood History screen, by hiding event types via Bundle argument)
 *
 * Usage:
 * - Implement `FilterListener` in the parent fragment or activity to handle filter apply/reset.
 * - Call `setFilterListener()` before showing the fragment.
 * - Optionally pass arguments via Bundle:
 *     - `hide_event_type_filters` (boolean) → to hide the checkboxes
 *     - `show_only_nearby_event_type` (boolean) → to default-select "Nearby Events"
 *
 * On apply, a `FilterCriteria` object containing the selected filters is returned.
 * On reset, all filters are cleared and the UI is restored to default state.
 *
 * Example:
 * Bundle args = new Bundle();
 * args.putBoolean("hide_event_type_filters", true);
 * FilterFragment fragment = new FilterFragment();
 * fragment.setArguments(args);
 * fragment.setFilterListener(...);
 * getParentFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
 */

package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.example.team_16.R;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment {
    private static final String TAG = "FilterFragment";

    public interface FilterListener {
        void onApplyFilter(FilterCriteria criteria);

        void onResetFilter();
    }

    public static class FilterCriteria {
        public String timePeriod;
        public String emotionalState;
        public String triggerReason;
        public List<String> eventTypes = new ArrayList<>();
    }

    private FilterListener listener;
    private boolean hideEventTypeFilters = false;

    private Button[] timePeriodButtons;
    private final String[] timePeriodNames = {
            "All Time", "Last Year", "Last Month", "Last Week"
    };

    private Button[] emotionButtons;
    private final String[] emotionNames = {
            "Anger", "Confusion", "Disgust", "Fear",
            "Happiness", "Sadness", "Shame", "Surprise"
    };

    private CheckBox myOwnMoodHistoryCheck, eventsFromPeopleIFollowCheck, nearbyEventsCheck;
    private EditText triggerReasonEditText;
    private Button resetButton, applyButton;

    private LinearLayout eventTypeSection;
    private TextView eventsTextView;

    private String selectedTimePeriod = null;
    private String selectedEmotionalState = null;
    private String triggerReason = "";

    public FilterFragment() {
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            hideEventTypeFilters = getArguments().getBoolean("hide_event_type_filters", false);
            Log.d(TAG, "hideEventTypeFilters: " + hideEventTypeFilters);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button allTimeButton = view.findViewById(R.id.allTime);
        Button lastYearButton = view.findViewById(R.id.lastYear);
        Button lastMonthButton = view.findViewById(R.id.lastMonth);
        Button lastWeekButton = view.findViewById(R.id.lastWeek);

        timePeriodButtons = new Button[]{
                allTimeButton, lastYearButton, lastMonthButton, lastWeekButton
        };

        Button angerButton = view.findViewById(R.id.anger_button);
        Button confusionButton = view.findViewById(R.id.confusion_button);
        Button disgustButton = view.findViewById(R.id.disgust_button);
        Button fearButton = view.findViewById(R.id.fear_button);
        Button happinessButton = view.findViewById(R.id.happiness_button);
        Button sadnessButton = view.findViewById(R.id.sadness_button);
        Button shameButton = view.findViewById(R.id.shame_button);
        Button surpriseButton = view.findViewById(R.id.surprise_button);

        emotionButtons = new Button[]{
                angerButton, confusionButton, disgustButton, fearButton,
                happinessButton, sadnessButton, shameButton, surpriseButton
        };

        for (Button btn : timePeriodButtons) {
            btn.setAlpha(1.0f);
        }
        for (Button btn : emotionButtons) {
            btn.setAlpha(1.0f);
        }

        for (int i = 0; i < timePeriodButtons.length; i++) {
            final int index = i;
            timePeriodButtons[i].setOnClickListener(v -> {
                if (timePeriodNames[index].equals(selectedTimePeriod)) {
                    selectedTimePeriod = null;
                    timePeriodButtons[index].setAlpha(0.5f);
                } else {
                    selectedTimePeriod = timePeriodNames[index];
                    highlightSelectedTimePeriod(index);
                }
            });
        }

        for (int i = 0; i < emotionButtons.length; i++) {
            final int index = i;
            emotionButtons[i].setOnClickListener(v -> {
                if (emotionNames[index].equals(selectedEmotionalState)) {
                    selectedEmotionalState = null;
                    emotionButtons[index].setAlpha(0.5f);
                } else {
                    selectedEmotionalState = emotionNames[index];
                    highlightSelectedEmotion(index);
                }
            });
        }

        eventsTextView = view.findViewById(R.id.events_text);
        eventTypeSection = view.findViewById(R.id.event_type_section);
        myOwnMoodHistoryCheck = view.findViewById(R.id.myOwnMoodHistoryCheck);
        eventsFromPeopleIFollowCheck = view.findViewById(R.id.eventsFromPeopleIFollowCheck);
        nearbyEventsCheck = view.findViewById(R.id.nearbyEventsCheck);

        handleEventTypesVisibility();

        triggerReasonEditText = view.findViewById(R.id.search_bar);

        resetButton = view.findViewById(R.id.resetButton);
        applyButton = view.findViewById(R.id.applyButton);

        if (resetButton != null) {
            resetButton.setOnClickListener(v -> {
                resetSelections();

                if (listener != null) {
                    listener.onResetFilter();
                }
            });
        }

        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {

                if (triggerReasonEditText != null) {
                    triggerReason = triggerReasonEditText.getText().toString().trim();
                } else {
                    triggerReason = "";
                }

                List<String> selectedEventTypes = collectSelectedEventTypes();

                if ((selectedTimePeriod == null || selectedTimePeriod.equals("All Time")) &&
                        selectedEmotionalState == null &&
                        selectedEventTypes.isEmpty() &&
                        triggerReason.isEmpty()) {
                    getParentFragmentManager().popBackStack();
                    return;
                }

                FilterCriteria criteria = new FilterCriteria();
                criteria.timePeriod = (selectedTimePeriod == null) ? "All Time" : selectedTimePeriod;
                criteria.emotionalState = selectedEmotionalState;
                criteria.triggerReason = triggerReason;
                criteria.eventTypes = selectedEventTypes;

                if (listener != null) {
                    listener.onApplyFilter(criteria);
                }

                getParentFragmentManager().popBackStack();
            });
        }

        resetSelections();
    }

    private void highlightSelectedTimePeriod(int selectedIndex) {
        for (int i = 0; i < timePeriodButtons.length; i++) {
            if (i == selectedIndex) {
                timePeriodButtons[i].setAlpha(1.0f);
            } else {
                timePeriodButtons[i].setAlpha(0.5f);
            }
        }
    }

    private void highlightSelectedEmotion(int selectedIndex) {
        for (int i = 0; i < emotionButtons.length; i++) {
            if (i == selectedIndex) {
                emotionButtons[i].setAlpha(1.0f);
            } else {
                emotionButtons[i].setAlpha(0.5f);
            }
        }
    }

    private void handleEventTypesVisibility() {
        if (hideEventTypeFilters && eventTypeSection != null) {
            Log.d(TAG, "Hiding entire event type section");
            eventTypeSection.setVisibility(View.GONE);

            if (eventsTextView != null) {
                eventsTextView.setVisibility(View.GONE);
            }
            return;
        }

        if (eventTypeSection != null) {
            eventTypeSection.setVisibility(View.VISIBLE);
        }

        boolean showOnlyNearby = getArguments() != null && getArguments().getBoolean("show_only_nearby_event_type", false);
        if (showOnlyNearby && nearbyEventsCheck != null) {
            nearbyEventsCheck.setChecked(true);
        }
    }

    private List<String> collectSelectedEventTypes() {
        List<String> selectedEventTypes = new ArrayList<>();

        if (myOwnMoodHistoryCheck != null && myOwnMoodHistoryCheck.isChecked()) {
            selectedEventTypes.add("My Own Mood History");
        }
        if (eventsFromPeopleIFollowCheck != null && eventsFromPeopleIFollowCheck.isChecked()) {
            selectedEventTypes.add("Events from People I Follow");
        }
        if (nearbyEventsCheck != null && nearbyEventsCheck.isChecked()) {
            selectedEventTypes.add("Nearby Events within 5km");
        }

        return selectedEventTypes;
    }

    private void resetSelections() {
        selectedTimePeriod = null;
        if (timePeriodButtons != null) {
            for (Button btn : timePeriodButtons) {
                btn.setAlpha(1.0f);
            }

            selectedEmotionalState = null;
            if (emotionButtons != null) {
                for (Button btn : emotionButtons) {
                    btn.setAlpha(1.0f);
                }
            }

            if (triggerReasonEditText != null) {
                triggerReasonEditText.setText("");
            }

            boolean showOnlyNearby = getArguments() != null &&
                    getArguments().getBoolean("show_only_nearby_event_type", false);

            if (myOwnMoodHistoryCheck != null) {
                myOwnMoodHistoryCheck.setChecked(false);
            }
            if (eventsFromPeopleIFollowCheck != null) {
                eventsFromPeopleIFollowCheck.setChecked(false);
            }
            if (nearbyEventsCheck != null) {
                nearbyEventsCheck.setChecked(showOnlyNearby);
            }
        }

    }
}
