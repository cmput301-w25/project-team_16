package com.example.team_16.ui.fragments;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MonthlyRecapPage3Fragment extends Fragment {
    private static final String TAG = "MonthlyRecapPage3";
    private UserProfile userProfile;
    private Calendar lastCompletedMonth;
    private TextView tvEmotionalLandscapeTitle;
    private TextView tvPersonalizedObservation;
    private PieChart moodPieChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastCompletedMonth = Calendar.getInstance();
        lastCompletedMonth.add(Calendar.MONTH, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_recap_page3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvEmotionalLandscapeTitle = view.findViewById(R.id.tvEmotionalLandscapeTitle);
        tvPersonalizedObservation = view.findViewById(R.id.tvPersonalizedObservation);
        moodPieChart = view.findViewById(R.id.moodPieChart);

        // Set title
        tvEmotionalLandscapeTitle.setText("Your Emotional Landscape");

        // Default message
        tvPersonalizedObservation.setText("No mood data available for this month.");

        // Configure empty pie chart
        moodPieChart.setNoDataText("No chart data available");
        moodPieChart.setNoDataTextColor(Color.BLACK);

        // If userProfile is already set, update UI
        if (userProfile != null) {
            updateMoodBreakdown();
        }
    }

    private void updateMoodBreakdown() {
        if (userProfile == null) {
            Log.e(TAG, "updateMoodBreakdown called but userProfile is null");
            return;
        }

        Log.d(TAG, "Fetching monthly stats for mood breakdown...");

        userProfile.getMonthlyStats(
                lastCompletedMonth.get(Calendar.YEAR),
                lastCompletedMonth.get(Calendar.MONTH) + 1,
                stats -> {
                    if (stats == null) {
                        Log.e(TAG, "Monthly stats are null");
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Map<EmotionalState, Long> moodBreakdown = (Map<EmotionalState, Long>) stats.get("moodBreakdown");

                    if (moodBreakdown == null || moodBreakdown.isEmpty()) {
                        Log.e(TAG, "Mood breakdown is null or empty");
                        moodPieChart.clear();
                        moodPieChart.setNoDataText("No chart data available.");
                        moodPieChart.setNoDataTextColor(Color.BLACK);
                        moodPieChart.invalidate();
                        tvPersonalizedObservation.setText("No mood data available for this month.");
                        return;
                    }

                    // Calculate total for percentage
                    long total = 0;
                    for (Long count : moodBreakdown.values()) {
                        total += count;
                    }

                    final long totalMoods = total; // Make final for use in lambda

                    // Create pie chart entries
                    List<PieEntry> entries = new ArrayList<>();
                    for (Map.Entry<EmotionalState, Long> entry : moodBreakdown.entrySet()) {
                        float percentage = (entry.getValue() * 100f) / totalMoods;
                        entries.add(new PieEntry(percentage, entry.getKey().getName()));
                    }

                    // Create and configure pie chart
                    PieDataSet dataSet = new PieDataSet(entries, "Mood Distribution");
                    dataSet.setColors(getMoodColors());
                    dataSet.setValueTextSize(14f);
                    dataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.format("%.1f%%", value);
                        }
                    });

                    PieData data = new PieData(dataSet);
                    moodPieChart.setData(data);
                    moodPieChart.setDescription(null);
                    moodPieChart.setHoleRadius(40f);
                    moodPieChart.setTransparentCircleRadius(45f);
                    moodPieChart.setEntryLabelTextSize(14f);

                    // Add check to make sure the animation happens only after the data is fully loaded
                    if (isAdded() && getView() != null) {
                        moodPieChart.animateY(1000); // Animation after the chart data is set
                    }

                    moodPieChart.invalidate();

                    // Generate personalized observation for dominant emotion
                    EmotionalState topMood = (EmotionalState) stats.get("topMood");
                    if (topMood != null) {
                        float percentage = ((float) moodBreakdown.get(topMood) / totalMoods) * 100;
                        tvPersonalizedObservation.setText(generateObservation(topMood.getName(), percentage));
                    } else {
                        tvPersonalizedObservation.setText("No dominant emotional pattern detected this month.");
                    }
                }
        );
    }


    private String generateObservation(String mood, float percentage) {
        switch (mood) {
            case "Happiness":
                return String.format("You experienced happiness for %.1f%% of your month! That's great news!", percentage);
            case "Sadness":
                return String.format("You felt sadness for %.1f%% of your month. Remember it's okay to have these feelings.", percentage);
            case "Fear":
                return String.format("You experienced fear or anxiety %.1f%% of the time. Remember to use your coping strategies.", percentage);
            case "Anger":
                return String.format("Anger made up %.1f%% of your emotional experiences. Notice what triggers this emotion.", percentage);
            case "Confusion":
                return String.format("You felt confused %.1f%% of the time. Perhaps try breaking down complex situations.", percentage);
            case "Disgust":
                return String.format("Disgust appeared %.1f%% of the time. Notice what situations trigger this response.", percentage);
            case "Shame":
                return String.format("You experienced shame %.1f%% of the time. Remember to practice self-compassion.", percentage);
            case "Surprise":
                return String.format("Surprise made up %.1f%% of your month! You had many unexpected moments.", percentage);
            default:
                return String.format("You experienced %s for %.1f%% of your month.", mood, percentage);
        }
    }

    private int[] getMoodColors() {
        return new int[]{
                Color.parseColor("#FCD34D"), // Happiness - Yellow
                Color.parseColor("#83B9FA"), // Sadness - Blue
                Color.parseColor("#EF4444"), // Anger - Red
                Color.parseColor("#BB80FF"), // Confusion - Purple
                Color.parseColor("#80FFA8"), // Disgust - Green
                Color.parseColor("#F392C7"), // Shame - Pink
                Color.parseColor("#F8AA6C"), // Surprise - Orange
                Color.parseColor("#898989")  // Fear - Gray
        };
    }

    public void setUserProfile(UserProfile userProfile) {
        Log.d(TAG, "setUserProfile called");
        this.userProfile = userProfile;
        if (isAdded() && getView() != null) {
            updateMoodBreakdown();
        }
    }
}