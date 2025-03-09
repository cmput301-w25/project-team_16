package com.example.team_16.models;
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

    public String getEmoji() {
        Log.e("log", name);
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