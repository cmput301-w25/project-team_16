package com.example.team_16.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EmotionalStateRegistry {
    private static final Map<String, EmotionalState> emotions = new HashMap<>();

    // Initialize the registry with the base emotional states
    static {
        registerBaseEmotions();
    }

    /**
     * Registers the base emotional states required by the system.
     */
    private static void registerBaseEmotions() {
        register(new EmotionalState("Anger"));
        register(new EmotionalState("Confusion"));
        register(new EmotionalState("Disgust"));
        register(new EmotionalState("Fear"));
        register(new EmotionalState("Happiness"));
        register(new EmotionalState("Sadness"));
        register(new EmotionalState("Shame"));
        register(new EmotionalState("Surprise"));
    }

    /**
     * Registers a new emotional state.
     * If an emotional state with the same name already exists, it will be replaced.
     *
     * @param emotion The emotional state to register
     * @return The previously registered emotional state with the same name, or null if none existed
     */
    public static EmotionalState register(EmotionalState emotion) {
        return emotions.put(emotion.getName(), emotion);
    }

    /**
     * Gets an emotional state by its name.
     *
     * @param name The name of the emotional state to retrieve
     * @return The emotional state, or null if no emotional state with the given name exists
     */
    public static EmotionalState getByName(String name) {
        return emotions.get(name);
    }

    /**
     * Gets all registered emotional states.
     *
     * @return An unmodifiable map of all emotional states, keyed by name
     */
    public static Map<String, EmotionalState> getAll() {
        return Collections.unmodifiableMap(emotions);
    }

    /**
     * Gets the names of all registered emotional states.
     *
     * @return An unmodifiable set of all emotional state names
     */
    public static Set<String> getAllNames() {
        return Collections.unmodifiableSet(emotions.keySet());
    }

    /**
     * Checks if an emotional state with the given name exists.
     *
     * @param name The name to check
     * @return true if an emotional state with the given name exists, false otherwise
     */
    public static boolean exists(String name) {
        return emotions.containsKey(name);
    }

    /**
     * Removes an emotional state by name.
     * Note: This method should be used with caution, as it allows removing base emotional states.
     *
     * @param name The name of the emotional state to remove
     * @return The removed emotional state, or null if no emotional state with the given name existed
     */
    public static EmotionalState remove(String name) {
        return emotions.remove(name);
    }

    /**
     * Resets the registry to only contain the base emotional states.
     * This removes any custom emotional states that have been registered.
     */
    public static void reset() {
        emotions.clear();
        registerBaseEmotions();
    }
}
