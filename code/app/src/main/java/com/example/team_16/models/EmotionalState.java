package com.example.team_16.models;
import android.graphics.Color;
import android.util.Log;

import java.util.Objects;

/**
 * Represents an emotional state with a display name and color.
 */
public class EmotionalState {
    private final String name;  // Display name
    private final int color;    // Color representation

    /**
     * Creates a new emotional state with the given name.
     *
     * @param name The display name of the emotional state
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name is empty or blank
     */
    public EmotionalState() {
        name = null;
        color = Color.GRAY; // Default color
    }

    public EmotionalState(String name) {
        this.name = Objects.requireNonNull(name, "Emotional state name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotional state name cannot be empty");
        }
        this.color = getColorForEmotion(name);
    }

    /**
     * Creates a new emotional state with the given name and specific color.
     *
     * @param name The display name of the emotional state
     * @param color The color to associate with this emotional state
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name is empty or blank
     */
    public EmotionalState(String name, int color) {
        this.name = Objects.requireNonNull(name, "Emotional state name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Emotional state name cannot be empty");
        }
        this.color = color;
    }

    /**
     * Gets the display name of this emotional state.
     *
     * @return The display name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color associated with this emotional state.
     *
     * @return The color as an integer
     */
    public int getColor() {
        return color;
    }

    /**
     * Returns the appropriate color for a given emotion based on the UI design.
     *
     * @param emotionName The name of the emotion
     * @return The color representation as an integer
     */
    private int getColorForEmotion(String emotionName) {
        switch (emotionName) {
            case "Anger":
                return Color.parseColor("#F44336");
            case "Confusion":
                return Color.parseColor("#673AB7");
            case "Disgust":
                return Color.parseColor("#8BC34A");
            case "Fear":
                return Color.parseColor("#455362");
            case "Happiness":
                return Color.parseColor("#FFEB3B");
            case "Sadness":
                return Color.parseColor("#2196F3");
            case "Shame":
                return Color.parseColor("#D96CD7");
            case "Surprise":
                return Color.parseColor("#FF9800");
            default:
                return Color.GRAY;
        }
    }

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
            default:
                return "";
        }
    }

    /**
     * Returns the hex color code string for this emotion
     *
     * @return A hex color string (e.g., "#F44336")
     */
    public String getColorHexString() {
        switch (name) {
            case "Anger":
                return "#F44336";
            case "Confusion":
                return "#673AB7";
            case "Disgust":
                return "#8BC34A";
            case "Fear":
                return "#455362";
            case "Happiness":
                return "#FFEB3B";
            case "Sadness":
                return "#2196F3";
            case "Shame":
                return "#D96CD7";
            case "Surprise":
                return "#FF9800";
            default:
                return "#808080"; // Gray as default
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionalState that = (EmotionalState) o;
        return color == that.color && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public String toString() {
        return "EmotionalState{" +
                "name='" + name + '\'' +
                ", color=#" + Integer.toHexString(color).toUpperCase() +
                '}';
    }
}