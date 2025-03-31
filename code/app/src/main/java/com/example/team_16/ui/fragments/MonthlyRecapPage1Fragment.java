package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_16.R;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthlyRecapPage1Fragment extends Fragment {
    private static final String TAG = "MonthlyRecapPage1";
    private UserProfile userProfile;
    private Calendar lastCompletedMonth;
    private SimpleDateFormat monthYearFormat;
    private TextView tvTopMoodEmoji;
    private TextView tvWelcomeMessage;
    private TextView tvMonthMessage;
    private TextView tvMoodSummary;
    private TextView tvExplorationMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastCompletedMonth = Calendar.getInstance();
        lastCompletedMonth.add(Calendar.MONTH, -1);
        monthYearFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_recap_page1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvTopMoodEmoji = view.findViewById(R.id.tvTopMoodEmoji);
        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage);
        tvMonthMessage = view.findViewById(R.id.tvMonthMessage);
        tvMoodSummary = view.findViewById(R.id.tvMoodSummary);
        tvExplorationMessage = view.findViewById(R.id.tvExplorationMessage);

        // Set default values
        setDefaultValues();

        // If userProfile is already set, update UI
        if (userProfile != null) {
            updateUI();
        }
    }

    private void setDefaultValues() {
        tvTopMoodEmoji.setText("😐");
        tvWelcomeMessage.setText("Hey there!");
        tvMonthMessage.setText("Welcome to your mood recap");
        tvMoodSummary.setText("We don't have enough data about your moods this month.");
        tvExplorationMessage.setText("Let's explore how your month went and what affected your mood!");
    }

    private void updateUI() {
        if (userProfile == null) {
            Log.e(TAG, "updateUI called but userProfile is null");
            return;
        }

        Log.d(TAG, "Fetching monthly stats...");

        // Set welcome message right away with username
        tvWelcomeMessage.setText(String.format("Hey %s!", userProfile.getUsername()));

        // Set month message right away
        String monthName = monthYearFormat.format(lastCompletedMonth.getTime());
        tvMonthMessage.setText(String.format("Welcome to your %s mood recap", monthName));

        userProfile.getMonthlyStats(
                lastCompletedMonth.get(Calendar.YEAR),
                lastCompletedMonth.get(Calendar.MONTH) + 1,
                stats -> {
                    if (stats == null) {
                        Log.e(TAG, "Monthly stats are null");
                        return;
                    }

                    // Get top mood
                    EmotionalState topMood = (EmotionalState) stats.get("topMood");
                    Long totalEntries = (Long) stats.get("totalEntries");

                    if (topMood != null) {
                        Log.d(TAG, "Top mood: " + topMood.getName() + ", emoji: " + topMood.getEmoji());

                        // Set emoji
                        tvTopMoodEmoji.setText(topMood.getEmoji());

                        // Set mood summary message
                        tvMoodSummary.setText(String.format("This month, you mostly felt %s and logged your mood %d times.",
                                topMood.getName().toLowerCase(), totalEntries != null ? totalEntries : 0));
                    } else {
                        Log.e(TAG, "Top mood is null");
                        tvTopMoodEmoji.setText("😐");
                        tvMoodSummary.setText("We don't have enough data about your moods this month.");
                    }
                }
        );
    }

    public void setUserProfile(UserProfile userProfile) {
        Log.d(TAG, "setUserProfile called");
        this.userProfile = userProfile;
        if (isAdded() && getView() != null) {
            updateUI();
        }
    }
}