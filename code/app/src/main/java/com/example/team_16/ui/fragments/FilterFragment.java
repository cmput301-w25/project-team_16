package com.example.team_16.ui.fragments;

import android.graphics.Color;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
        public List<String> eventTypes = new ArrayList<>(); // Changed to List to support multiple selections
    }

    private FilterListener listener;
    private boolean hideEventTypeFilters = false;

    // UI Elements
    private Button allTimeButton, lastYearButton, lastMonthButton, lastWeekButton;
    private Button angerButton, confusionButton, disgustButton, fearButton, happinessButton, sadnessButton, shameButton, surpriseButton;
    private CheckBox myOwnMoodHistoryCheck, eventsFromPeopleIFollowCheck, nearbyEventsCheck;
    private EditText triggerReasonEditText;
    private LinearLayout eventTypeSection;
    private Button resetButton, applyButton;

    private String selectedTimePeriod = "All Time";
    private String selectedEmotionalState = null;
    private String triggerReason = "";
    private TextView eventsTextView;


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

        // Time period buttons
        allTimeButton = view.findViewById(R.id.allTime);
        lastYearButton = view.findViewById(R.id.lastYear);
        lastMonthButton = view.findViewById(R.id.lastMonth);
        lastWeekButton = view.findViewById(R.id.lastWeek);

        // Emotion buttons
        angerButton = view.findViewById(R.id.anger_button);
        confusionButton = view.findViewById(R.id.confusion_button);
        disgustButton = view.findViewById(R.id.disgust_button);
        fearButton = view.findViewById(R.id.fear_button);
        happinessButton = view.findViewById(R.id.happiness_button);
        sadnessButton = view.findViewById(R.id.sadness_button);
        shameButton = view.findViewById(R.id.shame_button);
        surpriseButton = view.findViewById(R.id.surprise_button);

        // Event checkboxes
        eventsTextView = view.findViewById(R.id.events_text);
        eventTypeSection = view.findViewById(R.id.event_type_section);
        myOwnMoodHistoryCheck = view.findViewById(R.id.myOwnMoodHistoryCheck);
        eventsFromPeopleIFollowCheck = view.findViewById(R.id.eventsFromPeopleIFollowCheck);
        nearbyEventsCheck = view.findViewById(R.id.nearbyEventsCheck);

        // Log if any important views are null
        if (eventTypeSection == null) {
            Log.e(TAG, "eventTypeSection is null");
        }
        if (myOwnMoodHistoryCheck == null) {
            Log.e(TAG, "myOwnMoodHistoryCheck is null");
        }
        if (eventsFromPeopleIFollowCheck == null) {
            Log.e(TAG, "eventsFromPeopleIFollowCheck is null");
        }
        if (nearbyEventsCheck == null) {
            Log.e(TAG, "nearbyEventsCheck is null");
        }

        triggerReasonEditText = view.findViewById(R.id.search_bar);
        resetButton = view.findViewById(R.id.resetButton);
        applyButton = view.findViewById(R.id.applyButton);

        // Set click listeners for time period selection
        if (allTimeButton != null) {
            allTimeButton.setOnClickListener(v -> {
                selectedTimePeriod = "All Time";
                updateTimePeriodSelection();
            });
        }

        if (lastYearButton != null) {
            lastYearButton.setOnClickListener(v -> {
                selectedTimePeriod = "Last Year";
                updateTimePeriodSelection();
            });
        }

        if (lastMonthButton != null) {
            lastMonthButton.setOnClickListener(v -> {
                selectedTimePeriod = "Last Month";
                updateTimePeriodSelection();
            });
        }

        if (lastWeekButton != null) {
            lastWeekButton.setOnClickListener(v -> {
                selectedTimePeriod = "Last Week";
                updateTimePeriodSelection();
            });
        }

        // Set up emotion button listeners
        setupEmotionButtonListeners();

        // Reset button listener
        if (resetButton != null) {
            resetButton.setOnClickListener(v -> {
                Animation scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
                resetButton.startAnimation(scaleUp);
                resetSelections();

                if (listener != null) {
                    listener.onResetFilter();
                }
            });
        }

        // Handle visibility conditions for event types
        handleEventTypesVisibility();

        // Apply button listener
        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                Animation scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
                applyButton.startAnimation(scaleUp);

                if (triggerReasonEditText != null) {
                    triggerReason = triggerReasonEditText.getText().toString().trim();
                } else {
                    triggerReason = "";
                }

                // Collect selected event types from checkboxes
                List<String> selectedEventTypes = collectSelectedEventTypes();

                if (selectedTimePeriod.equals("All Time")
                        && selectedEmotionalState == null
                        && selectedEventTypes.isEmpty()
                        && triggerReason.isEmpty()) {

                    getParentFragmentManager().popBackStack();
                    return;
                }

                FilterCriteria criteria = new FilterCriteria();
                criteria.timePeriod = selectedTimePeriod;
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

    private void setupEmotionButtonListeners() {
        if (angerButton != null) {
            angerButton.setOnClickListener(v -> {
                selectedEmotionalState = "Anger".equals(selectedEmotionalState) ? null : "Anger";
                updateEmotionalStateSelection();
            });
        }

        if (confusionButton != null) {
            confusionButton.setOnClickListener(v -> {
                selectedEmotionalState = "Confusion".equals(selectedEmotionalState) ? null : "Confusion";
                updateEmotionalStateSelection();
            });
        }

        if (disgustButton != null) {
            disgustButton.setOnClickListener(v -> {
                selectedEmotionalState = "Disgust".equals(selectedEmotionalState) ? null : "Disgust";
                updateEmotionalStateSelection();
            });
        }

        if (fearButton != null) {
            fearButton.setOnClickListener(v -> {
                selectedEmotionalState = "Fear".equals(selectedEmotionalState) ? null : "Fear";
                updateEmotionalStateSelection();
            });
        }

        if (happinessButton != null) {
            happinessButton.setOnClickListener(v -> {
                selectedEmotionalState = "Happiness".equals(selectedEmotionalState) ? null : "Happiness";
                updateEmotionalStateSelection();
            });
        }

        if (sadnessButton != null) {
            sadnessButton.setOnClickListener(v -> {
                selectedEmotionalState = "Sadness".equals(selectedEmotionalState) ? null : "Sadness";
                updateEmotionalStateSelection();
            });
        }

        if (shameButton != null) {
            shameButton.setOnClickListener(v -> {
                selectedEmotionalState = "Shame".equals(selectedEmotionalState) ? null : "Shame";
                updateEmotionalStateSelection();
            });
        }

        if (surpriseButton != null) {
            surpriseButton.setOnClickListener(v -> {
                selectedEmotionalState = "Surprise".equals(selectedEmotionalState) ? null : "Surprise";
                updateEmotionalStateSelection();
            });
        }
    }

    private void handleEventTypesVisibility() {
        // Check if we should hide the entire events section
        if (hideEventTypeFilters && eventTypeSection != null) {
            Log.d(TAG, "Hiding entire event type section");
            eventTypeSection.setVisibility(View.GONE);

            // Also hide the Events TextView
            if (eventsTextView != null) {
                eventsTextView.setVisibility(View.GONE);
            }

            return; // No need to configure individual checkboxes if the section is hidden
        }

        // If we reach here, the event type section should be visible
        if (eventTypeSection != null) {
            Log.d(TAG, "Showing event type section");
            eventTypeSection.setVisibility(View.VISIBLE);
        }

        // Check if "show_only_nearby_event_type" flag is active
        boolean showOnlyNearby = getArguments() != null && getArguments().getBoolean("show_only_nearby_event_type", false);

        // Following the requirement: all 3 events should either show together or none should be visible
        if (showOnlyNearby) {
            Log.d(TAG, "Show only nearby events - auto-checking it");
            // Even in "show only nearby" mode, we keep all checkboxes visible but auto-check nearby
            // Auto-check the nearby events option
            if (nearbyEventsCheck != null) {
                nearbyEventsCheck.setChecked(true);
            }
        } else {
            // Make sure all checkboxes are visible and initially unchecked
            if (myOwnMoodHistoryCheck != null) {
                myOwnMoodHistoryCheck.setChecked(false);
            }
            if (eventsFromPeopleIFollowCheck != null) {
                eventsFromPeopleIFollowCheck.setChecked(false);
            }
            if (nearbyEventsCheck != null) {
                nearbyEventsCheck.setChecked(false);
            }
        }
    }

    private List<String> collectSelectedEventTypes() {
        List<String> selectedEventTypes = new ArrayList<>();

        if (myOwnMoodHistoryCheck != null && myOwnMoodHistoryCheck.isChecked() &&
                myOwnMoodHistoryCheck.getVisibility() == View.VISIBLE) {
            selectedEventTypes.add("My Own Mood History");
        }

        if (eventsFromPeopleIFollowCheck != null && eventsFromPeopleIFollowCheck.isChecked() &&
                eventsFromPeopleIFollowCheck.getVisibility() == View.VISIBLE) {
            selectedEventTypes.add("Events from People I Follow");
        }

        if (nearbyEventsCheck != null && nearbyEventsCheck.isChecked() &&
                nearbyEventsCheck.getVisibility() == View.VISIBLE) {
            selectedEventTypes.add("Nearby Events within 5km");
        }

        return selectedEventTypes;
    }

    private void resetSelections() {
        // Reset time period selection
        selectedTimePeriod = "All Time";
        updateTimePeriodSelection();

        // Reset emotional state selection
        selectedEmotionalState = null;
        updateEmotionalStateSelection();

        // Reset search field
        if (triggerReasonEditText != null) {
            triggerReasonEditText.setText("");
        }

        // Reset checkboxes - with null checks
        if (myOwnMoodHistoryCheck != null) {
            myOwnMoodHistoryCheck.setChecked(false);
        }
        if (eventsFromPeopleIFollowCheck != null) {
            eventsFromPeopleIFollowCheck.setChecked(false);
        }

        // For Maps fragment, keep nearbyEventsCheck checked if showOnlyNearby is true
        boolean showOnlyNearby = getArguments() != null && getArguments().getBoolean("show_only_nearby_event_type", false);
        if (nearbyEventsCheck != null && showOnlyNearby) {
            // In Maps fragment, always keep the nearby events checked
            nearbyEventsCheck.setChecked(true);
        } else if (nearbyEventsCheck != null) {
            nearbyEventsCheck.setChecked(false);
        }
    }

    private void updateTimePeriodSelection() {
        // Update button backgrounds for time period selection with null checks
        updateButtonState((AppCompatButton) allTimeButton, "All Time");
        updateButtonState((AppCompatButton) lastYearButton, "Last Year");
        updateButtonState((AppCompatButton) lastMonthButton, "Last Month");
        updateButtonState((AppCompatButton) lastWeekButton, "Last Week");
    }

    private void updateEmotionalStateSelection() {
        // Update emotion buttons with null checks
        updateButtonState((AppCompatButton)angerButton, "Anger");
        updateButtonState((AppCompatButton)confusionButton, "Confusion");
        updateButtonState((AppCompatButton)disgustButton, "Disgust");
        updateButtonState((AppCompatButton)fearButton, "Fear");
        updateButtonState((AppCompatButton)happinessButton, "Happiness");
        updateButtonState((AppCompatButton)sadnessButton, "Sadness");
        updateButtonState((AppCompatButton)shameButton, "Shame");
        updateButtonState((AppCompatButton)surpriseButton, "Surprise");
    }

    private void updateButtonState(AppCompatButton button, String stateName) {
        if (button == null) return;

        boolean isSelected = isStateSelected(stateName);

        // Use just one drawable, but change its tint instead
        button.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button_background));


        // Apply distinct visual styling for selected vs unselected states
        if (isSelected) {
            // Make selected state more visually prominent
            DrawableCompat.setTint(button.getBackground(), ContextCompat.getColor(requireContext(), R.color.white));

            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            button.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150);
            button.setAlpha(1.0f);
            // Optional: add elevation for a "pressed" effect
            button.setElevation(0f);
        } else {
            // Unselected state

            // Consider avoiding alpha changes as they can make text harder to read
            // Instead of alpha, you could use a different background tint or style
            button.setAlpha(1.0f);
            // Optional: add elevation for depth
            button.setElevation(2f);
            button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150);

            // Reset background color and text color for time period buttons
            if (isTimePeriodButton(button)) {
                DrawableCompat.setTint(button.getBackground(), Color.parseColor("#ecd4d3"));
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.time_period_button_color));
            }
            else {
                button.setTextColor(Color.WHITE);
            }

        }
    }

    // Helper function to check if the button is a time period selection button
    private boolean isTimePeriodButton(AppCompatButton button) {
        return button == allTimeButton || button == lastYearButton ||
                button == lastMonthButton || button == lastWeekButton;
    }

    // Extract the selection logic for better readability
    private boolean isStateSelected(String stateName) {
        return (selectedTimePeriod != null && selectedTimePeriod.equals(stateName)) ||
                (selectedEmotionalState != null && selectedEmotionalState.equals(stateName));
    }


}