package com.example.team_16.utils;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing mood data and generating statistics.
 */
public class MoodAnalytics {

    /**
     * Analyzes mood events for a given month and returns statistics.
     * @param moodEvents List of mood events to analyze
     * @param year The year to analyze
     * @param month The month to analyze (1-12)
     * @return Map containing various statistics about the moods
     */
    public static Map<String, Object> getMonthlyStats(List<MoodEvent> moodEvents, int year, int month) {
        Map<String, Object> stats = new HashMap<>();

        // Filter events for the specified month
        List<MoodEvent> monthlyEvents = moodEvents.stream()
                .filter(event -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(event.getDate());
                    return cal.get(Calendar.YEAR) == year &&
                            cal.get(Calendar.MONTH) + 1 == month;
                })
                .collect(Collectors.toList());

        if (monthlyEvents.isEmpty()) {
            return stats;
        }

        // Calculate mood breakdown
        Map<EmotionalState, Long> moodBreakdown = monthlyEvents.stream()
                .collect(Collectors.groupingBy(
                        MoodEvent::getEmotionalState,
                        Collectors.counting()
                ));

        // Find top mood
        EmotionalState topMood = moodBreakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Calculate most active days
        Map<Integer, Long> dayActivity = monthlyEvents.stream()
                .collect(Collectors.groupingBy(
                        event -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(event.getDate());
                            return cal.get(Calendar.DAY_OF_MONTH);
                        },
                        Collectors.counting()
                ));

        // Find most active day
        int mostActiveDay = dayActivity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        // Calculate average moods per day
        double avgMoodsPerDay = (double) monthlyEvents.size() /
                Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

        // Calculate mood consistency (percentage of days with at least one mood)
        long daysWithMoods = dayActivity.size();
        double moodConsistency = (double) daysWithMoods /
                Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) * 100;

        // Find most common triggers
        Map<String, Long> triggerBreakdown = monthlyEvents.stream()
                .filter(event -> event.getTrigger() != null && !event.getTrigger().isEmpty())
                .collect(Collectors.groupingBy(
                        MoodEvent::getTrigger,
                        Collectors.counting()
                ));

        // Find most common social situations
        Map<String, Long> socialSituationBreakdown = monthlyEvents.stream()
                .filter(event -> event.getSocialSituation() != null && !event.getSocialSituation().isEmpty())
                .collect(Collectors.groupingBy(
                        MoodEvent::getSocialSituation,
                        Collectors.counting()
                ));

        // Add all statistics to the result map
        stats.put("totalMoods", monthlyEvents.size());
        stats.put("moodBreakdown", moodBreakdown);
        stats.put("topMood", topMood);
        stats.put("mostActiveDay", mostActiveDay);
        stats.put("avgMoodsPerDay", avgMoodsPerDay);
        stats.put("moodConsistency", moodConsistency);
        stats.put("triggerBreakdown", triggerBreakdown);
        stats.put("socialSituationBreakdown", socialSituationBreakdown);

        return stats;
    }

    /**
     * Gets the mood trend over time for a given month.
     * @param moodEvents List of mood events to analyze
     * @param year The year to analyze
     * @param month The month to analyze (1-12)
     * @return Map of day numbers to their most frequent mood
     */
    public static Map<Integer, EmotionalState> getMoodTrend(List<MoodEvent> moodEvents, int year, int month) {
        Map<Integer, Map<EmotionalState, Long>> dailyMoods = new HashMap<>();

        // Group moods by day
        moodEvents.stream()
                .filter(event -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(event.getDate());
                    return cal.get(Calendar.YEAR) == year &&
                            cal.get(Calendar.MONTH) + 1 == month;
                })
                .forEach(event -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(event.getDate());
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    dailyMoods.computeIfAbsent(day, k -> new HashMap<>())
                            .merge(event.getEmotionalState(), 1L, Long::sum);
                });

        // Find most frequent mood for each day
        return dailyMoods.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(null)
                ));
    }

    /**
     * Calculates the mood stability score for a given month.
     * A higher score indicates more consistent moods.
     * @param moodEvents List of mood events to analyze
     * @param year The year to analyze
     * @param month The month to analyze (1-12)
     * @return Stability score between 0 and 100
     */
    public static double calculateMoodStability(List<MoodEvent> moodEvents, int year, int month) {
        List<MoodEvent> monthlyEvents = moodEvents.stream()
                .filter(event -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(event.getDate());
                    return cal.get(Calendar.YEAR) == year &&
                            cal.get(Calendar.MONTH) + 1 == month;
                })
                .collect(Collectors.toList());

        if (monthlyEvents.isEmpty()) {
            return 0.0;
        }

        // Count mood transitions
        int transitions = 0;
        EmotionalState previousMood = null;

        for (MoodEvent event : monthlyEvents) {
            if (previousMood != null && !previousMood.equals(event.getEmotionalState())) {
                transitions++;
            }
            previousMood = event.getEmotionalState();
        }

        // Calculate stability score (lower transitions = higher stability)
        double maxPossibleTransitions = monthlyEvents.size() - 1;
        return maxPossibleTransitions > 0 ?
                (1.0 - (transitions / maxPossibleTransitions)) * 100 : 100.0;
    }
}