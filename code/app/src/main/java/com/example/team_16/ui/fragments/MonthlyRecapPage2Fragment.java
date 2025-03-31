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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyRecapPage2Fragment extends Fragment {
    private static final String TAG = "MonthlyRecapPage2";
    private UserProfile userProfile;
    private Calendar lastCompletedMonth;
    private SimpleDateFormat monthFormat;
    private TextView tvJourneyTitle;
    private TextView tvJourneyDescription;
    private LineChart moodTrendChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastCompletedMonth = Calendar.getInstance();
        lastCompletedMonth.add(Calendar.MONTH, -1);
        monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_recap_page2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvJourneyTitle = view.findViewById(R.id.tvJourneyTitle);
        tvJourneyDescription = view.findViewById(R.id.tvJourneyDescription);
        moodTrendChart = view.findViewById(R.id.moodTrendChart);

        // Set default values
        String monthName = monthFormat.format(lastCompletedMonth.getTime());
        tvJourneyTitle.setText(String.format("Your %s Journey", monthName));
        tvJourneyDescription.setText("Here's how your mood changed throughout the month");

        // Configure empty chart
        moodTrendChart.setNoDataText("No mood data available for this month");
        moodTrendChart.setNoDataTextColor(Color.BLACK);

        // If userProfile is already set, update UI
        if (userProfile != null) {
            updateMoodTrend();
        }
    }

    private void updateMoodTrend() {
        if (userProfile == null) {
            Log.e(TAG, "updateMoodTrend called but userProfile is null");
            return;
        }

        Log.d(TAG, "Fetching monthly trend data...");

        userProfile.getMonthlyTrend(
                lastCompletedMonth.get(Calendar.YEAR),
                lastCompletedMonth.get(Calendar.MONTH) + 1,
                trend -> {
                    if (trend == null || trend.isEmpty()) {
                        Log.e(TAG, "Monthly trend data is null or empty");
                        // Clear any existing chart data
                        moodTrendChart.clear();
                        moodTrendChart.setNoDataText("No mood data available for this month");
                        moodTrendChart.setNoDataTextColor(Color.BLACK);
                        moodTrendChart.invalidate();
                        return;
                    }

                    Log.d(TAG, "Got monthly trend data with " + trend.size() + " entries");

                    List<Entry> entries = new ArrayList<>();
                    Float lastKnownMood = null; // No default, find first available mood

                    // Find the first recorded mood in the month
                    for (int day = 1; day <= lastCompletedMonth.getActualMaximum(Calendar.DAY_OF_MONTH); day++) {
                        if (trend.containsKey(day)) {
                            lastKnownMood = getMoodValue(trend.get(day));
                            break; // Stop after finding the first mood
                        }
                    }

                    // Use a neutral default (e.g., 3) if no mood was found in the entire month
                    if (lastKnownMood == null) lastKnownMood = 3f;

                    // Populate the chart data
                    for (int day = 1; day <= lastCompletedMonth.getActualMaximum(Calendar.DAY_OF_MONTH); day++) {
                        if (trend.containsKey(day)) {
                            lastKnownMood = getMoodValue(trend.get(day)); // Update if mood exists
                        }
                        entries.add(new Entry(day, lastKnownMood)); // Fill in missing days
                    }

                    // Create and configure line chart
                    LineDataSet dataSet = new LineDataSet(entries, "Mood Trend");
                    dataSet.setColor(Color.BLUE);
                    dataSet.setValueTextSize(12f);
                    dataSet.setDrawCircles(true);
                    dataSet.setCircleColor(Color.BLUE);
                    dataSet.setCircleRadius(5f);
                    dataSet.setDrawValues(false);
                    dataSet.setLineWidth(2f);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Use cubic bezier for smooth curves
                    dataSet.setDrawFilled(false); // Disable filling under the line
                    dataSet.setDrawHorizontalHighlightIndicator(false); // Disable highlighting horizontal lines

                    LineData data = new LineData(dataSet);
                    moodTrendChart.setData(data);
                    moodTrendChart.setDescription(null);
                    moodTrendChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    moodTrendChart.getXAxis().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.format("Day %.0f", value);
                        }
                    });
                    moodTrendChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return getMoodNameFromValue(value);
                        }
                    });
                    moodTrendChart.getAxisRight().setEnabled(false);
                    moodTrendChart.setTouchEnabled(true);
                    moodTrendChart.setDragEnabled(true);
                    moodTrendChart.setScaleEnabled(true);
                    moodTrendChart.setPinchZoom(true);
                    moodTrendChart.animateX(1000);
                    moodTrendChart.invalidate();
                }
        );
    }

    private float getMoodValue(EmotionalState state) {
        if (state == null) return 3f;
        String moodName = state.getName();

        switch (moodName) {
            case "Happiness": return 7f;
            case "Surprise": return 6f;
            case "Anger": return 5f;
            case "Confusion": return 4f;
            case "Disgust": return 3f;
            case "Fear": return 2f;
            case "Sadness": return 1f;
            case "Shame": return 0f;
            default: return 3f;
        }
    }

    private String getMoodNameFromValue(float value) {
        int intValue = Math.round(value);
        switch (intValue) {
            case 7: return "Happiness";
            case 6: return "Surprise";
            case 5: return "Anger";
            case 4: return "Confusion";
            case 3: return "Disgust";
            case 2: return "Fear";
            case 1: return "Sadness";
            case 0: return "Shame";
            default: return "";
        }
    }

    public void setUserProfile(UserProfile userProfile) {
        Log.d(TAG, "setUserProfile called");
        this.userProfile = userProfile;
        if (isAdded() && getView() != null) {
            updateMoodTrend();
        }
    }
}
