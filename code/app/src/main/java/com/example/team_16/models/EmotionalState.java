package com.example.team_16.models;
import android.graphics.Color;
import android.util.Log;

import java.util.Objects;

/**
 * Represents an emotional state with a display name.
 */
public class EmotionalState {
    private final String name;  // Display name

    /**
     * Creates a new emotional state with the given name.
     *
     * @param name The display name of the emotional state
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name is empty or blank
     */

    public EmotionalState() {
        name = null;
    }

    public EmotionalState(String name) {
        this.name = Objects.requireNonNull(name, "Emotional state name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotional state name cannot be empty");
        }
    }

    /**
     * Gets the display name of this emotional state.
     *
     * @return The display name
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionalState that = (EmotionalState) o;
        return Objects.equals(name, that.name);
    }

    /**
     * Return emoji based on emoji name
     */
    public String getEmoji() {
        switch (name) {
            case "Happiness":
                return "😊";
            case "Surprise":
                return "😱";
            case "Anger":
                return "😡";
            case "Confusion":
                return "😵‍💫";
            case "Disgust":
                return "🤢";
            case "Fear":
                return "😨";
            case "Sadness":
                return "☹️";
            case "Shame":
                return "😳";
        }
        return "";

    }

    /**
     * Returns a color value associated with this emotional state
     * These colors match the UI colors used in the app's layout
     *
     * @return The color as an integer value from android.graphics.Color
     */
    public int getColor() {
        switch (name) {
            case "Happiness":
                return Color.parseColor("#FFEB3B");    // Yellow
            case "Surprise":
                return Color.parseColor("#FF9800");    // Orange
            case "Anger":
                return Color.parseColor("#F44336");    // Red
            case "Confusion":
                return Color.parseColor("#673AB7");    // Deep Purple
            case "Disgust":
                return Color.parseColor("#8BC34A");    // Light Green
            case "Fear":
                return Color.parseColor("#455362");    // Dark Slate Gray
            case "Sadness":
                return Color.parseColor("#2196F3");    // Blue
            case "Shame":
                return Color.parseColor("#D96CD7");    // Pink/Purple
            default:
                return Color.GRAY;                     // Default color
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "EmotionalState{" +
                "name='" + name + '\'' +
                '}';
    }
}