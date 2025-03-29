package com.example.team_16.models;
import android.graphics.Color;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an emotional state with a display name, associated gradient, and text color.
 */
public class EmotionalState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;  // Display name

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
     * @return The display name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the resource ID for the gradient background associated with this emotional state.
     *
     * @return The resource ID for the gradient drawable
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
                // Return a default gradient if no matching emotion is found
                return com.example.team_16.R.drawable.gradient_default;
        }
    }

    /**
     * Gets the resource ID for the bottom background associated with this emotional state.
     * These backgrounds have a white base with 20% opacity gradient overlay.
     *
     * @return The resource ID for the bottom background drawable
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
     * Gets the text color associated with this emotional state.
     * Colors are darker variants of the gradient start color.
     *
     * @return The color as an int (including alpha)
     */
    public int getTextColor() {
        switch (name) {
            case "Happiness":
                return Color.parseColor("#594D01");  // Dark yellow-gold from FCD34D
            case "Surprise":
                return Color.parseColor("#593A01");  // Dark orange from F8AA6C
            case "Anger":
                return Color.parseColor("#590001");  // Dark red from EF4444
            case "Confusion":
                return Color.parseColor("#320159");  // Dark purple from BB80FF
            case "Disgust":
                return Color.parseColor("#015934");  // Dark green from 80FFA8
            case "Fear":
                return Color.parseColor("#353535");  // Dark gray from 898989
            case "Sadness":
                return Color.parseColor("#013159");  // Dark blue from 83B9FA
            case "Shame":
                return Color.parseColor("#590031");  // Dark pink from F392C7
            default:
                return Color.parseColor("#333333");  // Default dark gray
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionalState that = (EmotionalState) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @NonNull
    @Override
    public String toString() {
        return "EmotionalState{" +
                "name='" + name + '\'' +
                '}';
    }
}