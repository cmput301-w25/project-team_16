package com.example.team_16.models;

import java.util.ArrayList;
import java.util.List;

public class MoodHistory {
    // Constants for history modes
    public static final int MODE_PERSONAL = 1;
    public static final int MODE_FOLLOWING = 2;

    // Attributes
    private final String userId;           // User ID this history belongs to
    private final int mode;                // Either PERSONAL or FOLLOWING
    private List<MoodEvent> moodEvents;    // Cached events

    /**
     * Constructor for a specific user's mood history
     *
     * @param userId The user ID this history belongs to
     * @param mode Either MODE_PERSONAL or MODE_FOLLOWING
     */
    public MoodHistory(String userId, int mode) {
        this.userId = userId;
        this.mode = mode;
        this.moodEvents = new ArrayList<>();
        loadEvents();
    }

    /**
     * Loads the appropriate events based on the mode
     */
    private void loadEvents() {
        if (mode == MODE_PERSONAL) {
            // For personal history, load user's own events
            // TODO: call FirebaseDB method to get user's events
        } else if (mode == MODE_FOLLOWING) {
            // For following mode, load followed users' events
            // TODO: call FirebaseDB method to get followed users' events
        }
    }

    /**
     * Refreshes the events list from Firebase
     */
    public void refresh() {
        loadEvents();
    }

    //public List<MoodEvent> getFilteredEvents(MoodFilter filter) {
        // TODO: call MoodFilter to apply filter to moodEvents and return filtered list
    //}

    /**
     * Get all events (unfiltered)
     */
    public List<MoodEvent> getAllEvents() {
        return new ArrayList<>(moodEvents);
    }

    /**
     * Get event by ID
     */
    public MoodEvent getEventById(String eventId) {
        return moodEvents.stream()
                .filter(event -> event.getId().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Protected method to update the mood events list
     * Only subclasses can call this method
     */
    protected void setMoodEvents(List<MoodEvent> events) {
        this.moodEvents = new ArrayList<>(events);
    }

}
