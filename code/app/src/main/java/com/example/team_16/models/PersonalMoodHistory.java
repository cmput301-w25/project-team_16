package com.example.team_16.models;

import java.util.List;

/**
 * PersonalMoodHistory extends MoodHistory with offline capabilities
 * for managing a user's own mood events
 * Note: Offline functionality will be implemented in future iterations
 */
public class PersonalMoodHistory extends MoodHistory {
    /**
     * Constructor for PersonalMoodHistory
     *
     * @param userId The user ID this history belongs to
     */
    public PersonalMoodHistory(String userId) {
        super(userId, MODE_PERSONAL);
    }

    /**
     * Add a new mood event
     *
     * @param event The mood event to add
     */
    public void addEvent(MoodEvent event) {
        // Add to in-memory collection
        List<MoodEvent> events = getAllEvents();
        events.add(event);
        updateMoodEvents(events);

        // Save to Firebase via FirebaseDB
        // TODO: Replace with actual FirebaseDB.getInstance().addMoodEvent(event, success -> {
        //    if (success) {
                // Successfully added to Firebase
                // In the future, this is where we'd update sync status
        //    }
        //});
    }

    /**
     * Edit an existing mood event
     *
     * @param eventId The ID of the event to edit
     * @param updates The updated mood event data
     */
    public void editEvent(String eventId, MoodEvent updates) {
        // Find and update the event in memory
        MoodEvent existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            // Update the event in our local cache
            List<MoodEvent> events = getAllEvents();
            int index = events.indexOf(existingEvent);
            if (index >= 0) {
                events.set(index, updates);
                updateMoodEvents(events);
            }

            // Update in Firebase
            // TODO: Replace with actual FirebaseDB.getInstance().updateMoodEvent(eventId, updates, success -> {
            //    if (success) {
                    // Successfully updated in Firebase
            //    }
            // });
        }
    }

    /**
     * Delete a mood event
     *
     * @param eventId The ID of the event to delete
     */
    public void deleteEvent(String eventId) {
        // Find and remove the event from memory
        MoodEvent existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            List<MoodEvent> events = getAllEvents();
            events.remove(existingEvent);
            updateMoodEvents(events);

            // Delete from Firebase
            // TODO: Replace with actual FirebaseDB.getInstance().deleteMoodEvent(eventId, success -> {
                //if (success) {
                    // Successfully deleted from Firebase
                //}
            //});
        }
    }

    /**
     * Sync pending changes with Firebase
     * (Placeholder for future offline functionality)
     */
    public void syncPendingChanges() {
        // TODO: Implement offline sync
    }

    /**
     * Check if there are pending changes to sync
     * (Placeholder for future offline functionality)
     */
    public boolean hasPendingChanges() {
        // TODO: Implement check for pending changes
        return false;
    }

    /**
     * Update the internal list of mood events
     */
    private void updateMoodEvents(List<MoodEvent> events) {
        // This would update the parent class's events
        // Assuming MoodHistory has this protected method
        setMoodEvents(events);
    }
}