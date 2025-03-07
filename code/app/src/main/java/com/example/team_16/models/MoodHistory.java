package com.example.team_16.models;

import com.example.team_16.database.FirebaseDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Base class for managing collections of mood events
 * This class only handles fetching/retrieving mood events, not modifying them
 */
public class MoodHistory {
    // Constants for history modes
    public static final int MODE_PERSONAL = 1;
    public static final int MODE_FOLLOWING = 2;

    // Attributes
    private final String userId;           // User ID this history belongs to
    private final int mode;                // Either PERSONAL or FOLLOWING
    private List<MoodEvent> moodEvents;    // Cached events
    private final FirebaseDB firebaseDB;   // Firebase database reference

    /**
     * Constructor for a specific user's mood history
     *
     * @param userId The user ID this history belongs to
     * @param mode Either MODE_PERSONAL or MODE_FOLLOWING
     * @param firebaseDB The FirebaseDB instance to use
     */
    public MoodHistory(String userId, int mode, FirebaseDB firebaseDB) {
        this.userId = userId;
        this.mode = mode;
        this.moodEvents = new ArrayList<>();
        this.firebaseDB = firebaseDB;
        loadEvents();
    }

    /**
     * Get the user ID this history belongs to
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the mode of this history
     *
     * @return either MODE_PERSONAL or MODE_FOLLOWING
     */
    public int getMode() {
        return mode;
    }

    /**
     * Get the FirebaseDB instance
     *
     * @return the FirebaseDB instance
     */
    protected FirebaseDB getFirebaseDB() {
        return firebaseDB;
    }

    /**
     * Loads the appropriate events based on the mode
     */
    private void loadEvents() {
        if (mode == MODE_PERSONAL) {
            // For personal history, load user's own events
            firebaseDB.getMoodEvents(
                    userId,
                    null,   // No date filter
                    null,   // No emotional state filter
                    null,   // No search text
                    events -> {
                        setMoodEvents(events);
                        notifyDataLoaded();
                    }
            );
        } else if (mode == MODE_FOLLOWING) {
            // For following mode, load followed users' events
            firebaseDB.getFollowingMoodEvents(
                    userId,
                    null,   // No date filter
                    null,   // No emotional state filter
                    null,   // No search text
                    events -> {
                        setMoodEvents(events);
                        notifyDataLoaded();
                    }
            );
        }
    }

    /**
     * Interface for data load callback
     */
    public interface DataLoadCallback {
        void onDataLoaded(List<MoodEvent> events);
    }

    private DataLoadCallback dataLoadCallback;

    /**
     * Set a callback to be notified when data is loaded
     *
     * @param callback The callback to be notified
     */
    public void setDataLoadCallback(DataLoadCallback callback) {
        this.dataLoadCallback = callback;
    }

    /**
     * Notify that data has been loaded
     */
    private void notifyDataLoaded() {
        if (dataLoadCallback != null) {
            dataLoadCallback.onDataLoaded(moodEvents);
        }
    }

    /**
     * Refreshes the events list from Firebase
     */
    public void refresh() {
        loadEvents();
    }

    /**
     * Get filtered events based on criteria
     *
     * @param emotionalState Filter by emotional state (optional)
     * @param startDate Filter by date after this (optional)
     * @param searchText Filter by trigger text containing this (optional)
     * @param callback Callback to receive filtered events
     */
    public void getFilteredEvents(
            EmotionalState emotionalState,
            Date startDate,
            String searchText,
            FirebaseDB.FirebaseCallback<List<MoodEvent>> callback) {

        if (mode == MODE_PERSONAL) {
            firebaseDB.getMoodEvents(userId, startDate, emotionalState, searchText, callback);
        } else {
            firebaseDB.getFollowingMoodEvents(userId, startDate, emotionalState, searchText, callback);
        }
    }

    /**
     * Get all events (unfiltered)
     *
     * @return A copy of the cached mood events list
     */
    public List<MoodEvent> getAllEvents() {
        return new ArrayList<>(moodEvents);
    }

    /**
     * Get event by ID
     *
     * @param eventId The ID of the event to find
     * @return The found event or null if not found
     */
    public MoodEvent getEventById(String eventId) {
        return moodEvents.stream()
                .filter(event -> event.getId().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get most recent events, limited by count
     *
     * @param limit Maximum number of events to return
     * @param callback Callback to receive results
     */
    public void getRecentEvents(int limit, FirebaseDB.FirebaseCallback<List<MoodEvent>> callback) {
        // Get events from the last week
        Date oneWeekAgo = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);

        if (mode == MODE_PERSONAL) {
            firebaseDB.getMoodEvents(
                    userId,
                    oneWeekAgo,
                    null,  // No emotional state filter
                    null,  // No text search
                    events -> {
                        // Limit the number of events if needed
                        List<MoodEvent> limitedEvents = events;
                        if (events.size() > limit) {
                            limitedEvents = events.subList(0, limit);
                        }
                        callback.onCallback(limitedEvents);
                    }
            );
        } else {
            firebaseDB.getFollowingMoodEvents(
                    userId,
                    oneWeekAgo,
                    null,  // No emotional state filter
                    null,  // No text search
                    events -> {
                        // Limit the number of events if needed
                        List<MoodEvent> limitedEvents = events;
                        if (events.size() > limit) {
                            limitedEvents = events.subList(0, limit);
                        }
                        callback.onCallback(limitedEvents);
                    }
            );
        }
    }

    /**
     * Protected method to update the mood events list
     * Only this class and subclasses can call this method
     *
     * @param events The new list of events
     */
    protected void setMoodEvents(List<MoodEvent> events) {
        this.moodEvents = new ArrayList<>(events);
    }
}