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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.ui.adapters.StatItemAdapter;
import com.example.team_16.models.UserProfile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonthlyRecapPage4Fragment extends Fragment {
    private static final String TAG = "MonthlyRecapPage4";
    private UserProfile userProfile;
    private Calendar lastCompletedMonth;
    private RecyclerView rvTriggers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastCompletedMonth = Calendar.getInstance();
        lastCompletedMonth.add(Calendar.MONTH, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_recap_page4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView tvTriggersTitle = view.findViewById(R.id.tvTriggersTitle);
        TextView tvTriggersDescription = view.findViewById(R.id.tvTriggersDescription);
        rvTriggers = view.findViewById(R.id.rvTriggers);

        // Setup RecyclerView
        rvTriggers.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set title and description
        tvTriggersTitle.setText(R.string.what_affected_your_mood);
        tvTriggersDescription.setText(R.string.these_were_your_top_mood_influencers_this_month);

        // If userProfile is already set, update UI
        if (userProfile != null) {
            updateTriggers();
        } else {
            // Show empty state
            rvTriggers.setAdapter(new StatItemAdapter(
                    new ArrayList<>(),
                    "No triggers recorded this month"
            ));
        }
    }

    private void updateTriggers() {
        if (userProfile == null) {
            Log.e(TAG, "updateTriggers called but userProfile is null");
            return;
        }

        Log.d(TAG, "Fetching monthly stats for triggers...");

        userProfile.getMonthlyStats(
                lastCompletedMonth.get(Calendar.YEAR),
                lastCompletedMonth.get(Calendar.MONTH) + 1,
                stats -> {
                    if (stats == null) {
                        Log.e(TAG, "Monthly stats are null");
                        rvTriggers.setAdapter(new StatItemAdapter(
                                new ArrayList<>(),
                                "No triggers recorded this month"
                        ));
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Long> triggerBreakdown = (Map<String, Long>) stats.get("triggerBreakdown");
                    if (triggerBreakdown == null || triggerBreakdown.isEmpty()) {
                        Log.e(TAG, "Trigger breakdown is null or empty");
                        rvTriggers.setAdapter(new StatItemAdapter(
                                new ArrayList<>(),
                                "No triggers recorded this month"
                        ));
                        return;
                    }

                    Log.d(TAG, "Got trigger breakdown with " + triggerBreakdown.size() + " entries");

                    // Sort triggers by count and limit to top 5
                    List<Map.Entry<String, Long>> sortedTriggers = triggerBreakdown.entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .limit(5)  // Show top 5 triggers
                            .collect(Collectors.toList());

                    // Set the adapter with the sorted triggers
                    rvTriggers.setAdapter(new StatItemAdapter(
                            sortedTriggers,
                            "No triggers recorded this month"
                    ));
                }
        );
    }

    public void setUserProfile(UserProfile userProfile) {
        Log.d(TAG, "setUserProfile called");
        this.userProfile = userProfile;
        if (isAdded() && getView() != null) {
            updateTriggers();
        }
    }
}