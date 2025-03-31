/**
 * Represents an emotional state in the mood tracking application.
 * This class encapsulates the visual and textual representation of a mood,
 * including its display name, gradient colors, emoji, and text styling.
 *
 * Key Features:
 * - Supports both base and custom emotional states
 * - Provides gradient color themes for UI display
 * - Includes emoji representation for visual feedback
 * - Handles text color styling for consistent UI
 *
 * Usage:
 * Emotional states are typically created through the EmotionalStateRegistry
 * and used to represent user moods in MoodEvents.
 *
 * Example:
 * <pre>
 * EmotionalState happy = new EmotionalState("Happy", R.drawable.gradient_happy, R.color.happy_text);
 * </pre>
 */

package com.example.team_16.models;
import android.graphics.Color;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class EmotionalState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;

    /**
     * Creates a new emotional state with the given name.
     *
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
     * @return The name of the emotional state
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the resource ID for the top gradient background.
     *
     * @return The gradient resource ID
     */
    public int getGradientResourceId() {
        switch (name) {
            case "Happiness":
                return com.example.team_16.R.drawable.gradient_happiness;
            case "Surprise":
                return com.example.team_16.R.drawable.gradient_surprise;
            case "Anger":
                return com.example.team_16.R.drawable.gradient_anger;
            case "Confusion":
                return com.example.team_16.R.drawable.gradient_confusion;
            case "Disgust":
                return com.example.team_16.R.drawable.gradient_disgust;
            case "Fear":
                return com.example.team_16.R.drawable.gradient_fear;
            case "Sadness":
                return com.example.team_16.R.drawable.gradient_sadness;
            case "Shame":
                return com.example.team_16.R.drawable.gradient_shame;
            default:
                return com.example.team_16.R.drawable.gradient_default;
        }
    }

    /**
     * Gets the resource ID for the bottom gradient background.
     *
     * @return The bottom gradient resource ID
     */
    public int getBottomGradientResourceId() {
        switch (name) {
            case "Happiness":
                return com.example.team_16.R.drawable.bottom_gradient_happiness;
            case "Surprise":
                return com.example.team_16.R.drawable.bottom_gradient_surprise;
            case "Anger":
                return com.example.team_16.R.drawable.bottom_gradient_anger;
            case "Confusion":
                return com.example.team_16.R.drawable.bottom_gradient_confusion;
            case "Disgust":
                return com.example.team_16.R.drawable.bottom_gradient_disgust;
            case "Fear":
                return com.example.team_16.R.drawable.bottom_gradient_fear;
            case "Sadness":
                return com.example.team_16.R.drawable.bottom_gradient_sadness;
            case "Shame":
                return com.example.team_16.R.drawable.bottom_gradient_shame;
            default:
                // Return a default bottom background if no matching emotion is found
                return com.example.team_16.R.drawable.bottom_gradient_default;
        }
    }

    /**
     * Gets the color resource ID for text styling.
     *
     * @return The text color resource ID
     */
    public int getTextColor() {
        switch (name) {
            case "Happiness":
                return Color.parseColor("#594D01");
            case "Surprise":
                return Color.parseColor("#593A01");
            case "Anger":
                return Color.parseColor("#590001");
            case "Confusion":
                return Color.parseColor("#320159");
            case "Disgust":
                return Color.parseColor("#015934");
            case "Fear":
                return Color.parseColor("#353535");
            case "Sadness":
                return Color.parseColor("#013159");
            case "Shame":
                return Color.parseColor("#590031");
            default:
                return Color.parseColor("#333333");
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
        }
        return "";
    }

    /**
     * Compares this EmotionalState with another object for equality.
     * Two EmotionalStates are considered equal if they have the same name.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionalState that = (EmotionalState) o;
        return Objects.equals(name, that.name);
    }

    /**
     * Generates a hash code for this EmotionalState.
     * The hash code is based on the name of the emotional state.
     *
     * @return The hash code for this EmotionalState
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns a string representation of this EmotionalState.
     *
     * @return A string containing the name and emoji of the emotional state
     */
    @Override
    public String toString() {
        return "EmotionalState{" +
                "name='" + name + '\'' +
                '}';
    }
}