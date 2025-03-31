/**
 * Manages a user's mood history, providing access to their personal or followed users' mood events.
 * Supports fetching all, recent, or filtered events from Firebase based on mode.
 *
 * Designed for read-only operations; editing/deleting is handled by PersonalMoodHistory.
 */

package com.example.team_16.models;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoodHistory {
    // Constants for history modes
    public static final int MODE_PERSONAL = 1;
    public static final int MODE_FOLLOWING = 2;

    private final String userId;
    private final int mode;
    private List<MoodEvent> moodEvents;
    private final FirebaseDB firebaseDB;

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

    public String getUserId() {
        return userId;
    }
    public int getMode() {
        return mode;
    }
    protected FirebaseDB getFirebaseDB() {
        return firebaseDB;
    }

    private void loadEvents() {
        if (mode == MODE_PERSONAL) {
            firebaseDB.getMoodEvents(
                    userId,
                    null,
                    null,
                    null,
                    events -> {
                        setMoodEvents(events);
                        notifyDataLoaded();
                    }
            );
        } else if (mode == MODE_FOLLOWING) {
            firebaseDB.getFollowingMoodEvents(
                    userId,
                    null,
                    null,
                    null,
                    events -> {
                        List<MoodEvent> publicEvents = new ArrayList<>();
                        for (MoodEvent e : events) {
                            if ("Public".equalsIgnoreCase(e.getPostType())) {
                                publicEvents.add(e);
                            }
                        }
                        setMoodEvents(publicEvents);
                        notifyDataLoaded();
                    }
            );
        }
    }

    public interface DataLoadCallback {
        void onDataLoaded(List<MoodEvent> events);
    }

    private DataLoadCallback dataLoadCallback;

    public void setDataLoadCallback(DataLoadCallback callback) {
        this.dataLoadCallback = callback;
    }

    private void notifyDataLoaded() {
        if (dataLoadCallback != null) {
            dataLoadCallback.onDataLoaded(moodEvents);
        }
    }

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
            firebaseDB.getFollowingMoodEvents(
                    userId,
                    startDate,
                    emotionalState,
                    searchText,
                    events -> {
                        List<MoodEvent> publicEvents = new ArrayList<>();
                        for (MoodEvent e : events) {
                            if ("Public".equalsIgnoreCase(e.getPostType())) {
                                publicEvents.add(e);
                            }
                        }
                        callback.onCallback(publicEvents);
                    }
            );
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
        for (MoodEvent event : moodEvents) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Get most recent events, limited by count
     *
     * @param limit Maximum number of events to return
     * @param callback Callback to receive results
     */
    public void getRecentEvents(int limit, FirebaseDB.FirebaseCallback<List<MoodEvent>> callback) {
        Date oneWeekAgo = new Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000);

        if (mode == MODE_PERSONAL) {
            firebaseDB.getMoodEvents(
                    userId,
                    oneWeekAgo,
                    null,
                    null,
                    events -> {
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
                    null,
                    null,
                    events -> {
                        List<MoodEvent> publicEvents = new ArrayList<>();
                        for (MoodEvent e : events) {
                            if ("Public".equalsIgnoreCase(e.getPostType())) {
                                publicEvents.add(e);
                            }
                        }
                        if (publicEvents.size() > limit) {
                            publicEvents = publicEvents.subList(0, limit);
                        }
                        callback.onCallback(publicEvents);
                    }
            );
        }
    }
    protected void setMoodEvents(List<MoodEvent> events) {
        this.moodEvents = new ArrayList<>(events);
    }
}
