package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_16.R;

public class FilterFragment extends Fragment {

    public interface FilterListener {
        void onApplyFilter(FilterCriteria criteria);
        void onResetFilter();
    }

    public static class FilterCriteria {
        public String timePeriod;
        public String emotionalState;
        public String triggerReason;
        public String eventType;
    }

    private FilterListener listener;
    private boolean hideEventTypeFilters = false;

    // UI Elements
    private TextView allTimeTextView, lastYearTextView, lastMonthTextView, lastWeekTextView;
    private Button angerButton, confusionButton, disgustButton, fearButton, happinessButton, sadnessButton, shameButton, surpriseButton;
    private TextView myOwnMoodHistoryTextView, eventsFromPeopleIFollowTextView, nearbyEventsTextView;
    private EditText triggerReasonEditText;
    private ViewGroup eventTypeSection;
    private Button resetButton, applyButton;

    private String selectedTimePeriod = "All Time";
    private String selectedEmotionalState = null;
    private String selectedEventType = null;
    private String triggerReason = "";

    public FilterFragment() {
        // constructor
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hideEventTypeFilters = getArguments().getBoolean("hide_event_type_filters", false);
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

        allTimeTextView = view.findViewById(R.id.allTime);
        lastYearTextView = view.findViewById(R.id.lastYear);
        lastMonthTextView = view.findViewById(R.id.lastMonth);
        lastWeekTextView = view.findViewById(R.id.lastWeek);

        angerButton = view.findViewById(R.id.anger_button);
        confusionButton = view.findViewById(R.id.confusion_button);
        disgustButton = view.findViewById(R.id.disgust_button);
        fearButton = view.findViewById(R.id.fear_button);
        happinessButton = view.findViewById(R.id.happiness_button);
        sadnessButton = view.findViewById(R.id.sadness_button);
        shameButton = view.findViewById(R.id.shame_button);
        surpriseButton = view.findViewById(R.id.surprise_button);

        myOwnMoodHistoryTextView = view.findViewById(R.id.myOwnMoodHistory);
        eventsFromPeopleIFollowTextView = view.findViewById(R.id.eventsFromPeopleIFollow);
        nearbyEventsTextView = view.findViewById(R.id.nearbyEvents);

        triggerReasonEditText = view.findViewById(R.id.search_bar);
        eventTypeSection = view.findViewById(R.id.event_type_section);

        resetButton = view.findViewById(R.id.resetButton);
        applyButton = view.findViewById(R.id.applyButton);

        // Set click listeners for time period selection
        allTimeTextView.setOnClickListener(v -> {
            selectedTimePeriod = "All Time";
            updateTimePeriodSelection();
        });
        lastYearTextView.setOnClickListener(v -> {
            selectedTimePeriod = "Last Year";
            updateTimePeriodSelection();
        });
        lastMonthTextView.setOnClickListener(v -> {
            selectedTimePeriod = "Last Month";
            updateTimePeriodSelection();
        });
        lastWeekTextView.setOnClickListener(v -> {
            selectedTimePeriod = "Last Week";
            updateTimePeriodSelection();
        });

        angerButton.setOnClickListener(v -> {
            selectedEmotionalState = "Anger";
            updateEmotionalStateSelection();
        });
        confusionButton.setOnClickListener(v -> {
            selectedEmotionalState = "Confusion";
            updateEmotionalStateSelection();
        });
        disgustButton.setOnClickListener(v -> {
            selectedEmotionalState = "Disgust";
            updateEmotionalStateSelection();
        });
        fearButton.setOnClickListener(v -> {
            selectedEmotionalState = "Fear";
            updateEmotionalStateSelection();
        });
        happinessButton.setOnClickListener(v -> {
            selectedEmotionalState = "Happiness";
            updateEmotionalStateSelection();
        });
        sadnessButton.setOnClickListener(v -> {
            selectedEmotionalState = "Sadness";
            updateEmotionalStateSelection();
        });
        shameButton.setOnClickListener(v -> {
            selectedEmotionalState = "Shame";
            updateEmotionalStateSelection();
        });
        surpriseButton.setOnClickListener(v -> {
            selectedEmotionalState = "Surprise";
            updateEmotionalStateSelection();
        });

        myOwnMoodHistoryTextView.setOnClickListener(v -> {
            selectedEventType = "My Own Mood History";
            updateEventTypeSelection();
        });
        eventsFromPeopleIFollowTextView.setOnClickListener(v -> {
            selectedEventType = "Events from People I Follow";
            updateEventTypeSelection();
        });
        nearbyEventsTextView.setOnClickListener(v -> {
            selectedEventType = "Nearby Events within 5km";
            updateEventTypeSelection();
        });

        resetButton.setOnClickListener(v -> {
            resetSelections();
            if (listener != null) {
                listener.onResetFilter();
            }
        });

        if (hideEventTypeFilters) {
            eventTypeSection.setVisibility(View.GONE);
            selectedEventType = null;
        }

        boolean showOnlyNearby = getArguments() != null && getArguments().getBoolean("show_only_nearby_event_type", false);

        if (showOnlyNearby) {
            myOwnMoodHistoryTextView.setVisibility(View.GONE);
            eventsFromPeopleIFollowTextView.setVisibility(View.GONE);
            // Keep nearbyEvents visible
        }


        applyButton.setOnClickListener(v -> {
            triggerReason = triggerReasonEditText.getText().toString().trim();

            if (selectedTimePeriod.equals("All Time")
                    && selectedEmotionalState == null
                    && selectedEventType == null
                    && triggerReason.isEmpty()) {

                getParentFragmentManager().popBackStack();
                return;
            }

            // Create and send filter criteria
            FilterCriteria criteria = new FilterCriteria();
            criteria.timePeriod = selectedTimePeriod;
            criteria.emotionalState = selectedEmotionalState;
            criteria.triggerReason = triggerReason;
            criteria.eventType = selectedEventType;

            if (listener != null) {
                listener.onApplyFilter(criteria);
            }

            getParentFragmentManager().popBackStack();
        });

        resetSelections();
    }

    private void resetSelections() {
        selectedTimePeriod = "All Time";
        selectedEmotionalState = null;
        selectedEventType = null;
        triggerReasonEditText.setText("");
        updateTimePeriodSelection();
        updateEmotionalStateSelection();
        updateEventTypeSelection();
    }

    private void updateTimePeriodSelection() {
        allTimeTextView.setBackgroundResource(selectedTimePeriod.equals("All Time") ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
        lastYearTextView.setBackgroundResource(selectedTimePeriod.equals("Last Year") ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
        lastMonthTextView.setBackgroundResource(selectedTimePeriod.equals("Last Month") ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
        lastWeekTextView.setBackgroundResource(selectedTimePeriod.equals("Last Week") ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
    }

    private void updateEmotionalStateSelection() {
        updateButtonState(angerButton, "Anger");
        updateButtonState(confusionButton, "Confusion");
        updateButtonState(disgustButton, "Disgust");
        updateButtonState(fearButton, "Fear");
        updateButtonState(happinessButton, "Happiness");
        updateButtonState(sadnessButton, "Sadness");
        updateButtonState(shameButton, "Shame");
        updateButtonState(surpriseButton, "Surprise");
    }

    private void updateButtonState(Button button, String emotion) {
        if (emotion.equals(selectedEmotionalState)) {
            button.setAlpha(1.0f);
        } else {
            button.setAlpha(selectedEmotionalState != null ? 0.5f : 1.0f);
            button.setBackgroundResource(R.drawable.stroke_background);
        }
    }

    private void updateEventTypeSelection() {
        myOwnMoodHistoryTextView.setBackgroundResource("My Own Mood History".equals(selectedEventType) ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
        eventsFromPeopleIFollowTextView.setBackgroundResource("Events from People I Follow".equals(selectedEventType) ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
        nearbyEventsTextView.setBackgroundResource("Nearby Events within 5km".equals(selectedEventType) ? R.drawable.stroke_background_selected : R.drawable.stroke_background);
    }

}
