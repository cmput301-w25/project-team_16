package com.example.team_16.models;

import android.util.Log;

import com.example.team_16.database.FirebaseDB;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PersonalMoodHistory extends MoodHistory with capabilities
 * for managing a user's own mood events (create, edit, delete)
 * and with offline capabilities for future implementations
 */
public class PersonalMoodHistory extends MoodHistory {
    private static final String TAG = "PersonalMoodHistory";

    // Track pending operations for offline functionality
    private final List<PendingOperation> pendingOperations;

    /**
     * Constructor for PersonalMoodHistory
     *
     * @param userId The user ID this history belongs to
     * @param firebaseDB The Firebase database instance
     */
    public PersonalMoodHistory(String userId, FirebaseDB firebaseDB) {
        super(userId, MODE_PERSONAL, firebaseDB);
        this.pendingOperations = new ArrayList<>();
    }

    /**
     * Add a new mood event
     *
     * @param event The mood event to add
     * @param callback Callback to handle result
     */
    public void addEvent(MoodEvent event, FirebaseDB.FirebaseCallback<Boolean> callback) {
        // Ensure event has the correct user ID
        event.setUserID(getUserId());

        // Ensure event has a valid ID (critical for offline mode)
        if (event.getId() == null || event.getId().isEmpty()) {
            String tempId = "local_" + UUID.randomUUID().toString();
            event.setId(tempId);
            Log.d(TAG, "Generated temporary ID for offline mood event: " + tempId);
        }

        // Ensure event has a timestamp
        if (event.getTimestamp() == null) {
            event.setTimestamp(Timestamp.now());
        }

        // Add to in-memory collection temporarily
        List<MoodEvent> events = getAllEvents();
        events.add(event);
        setMoodEvents(events);

        // Check if we're online
        FirebaseDB firebaseDB = getFirebaseDB();
        if (firebaseDB.isOnline()) {
            // Save to Firebase via FirebaseDB
            firebaseDB.addMoodEvent(event, success -> {
                if (success) {
                    // Successfully added to Firebase, refresh our cache
                    refresh();
                } else {
                    // Failed to add to Firebase, queue for later
                    queuePendingOperation(OperationType.ADD, event);
                }

                if (callback != null) {
                    callback.onCallback(success);
                }
            });
        } else {
            // Offline, queue for later
            queuePendingOperation(OperationType.ADD, event);
            if (callback != null) {
                callback.onCallback(true); // Optimistically return success
            }
        }
    }

    /**
     * Overloaded method without callback
     *
     * @param event The mood event to add
     */
    public void addEvent(MoodEvent event) {
        addEvent(event, null);
    }

    /**
     * Edit an existing mood event
     *
     * @param eventId The ID of the event to edit
     * @param updates The updated mood event data
     * @param callback Callback to handle result
     */
    public void editEvent(String eventId, MoodEvent updates, FirebaseDB.FirebaseCallback<Boolean> callback) {
        // Handle null eventId
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Cannot edit event with null or empty ID");
            if (callback != null) {
                callback.onCallback(false);
            }
            return;
        }

        // Ensure event has the correct ID and user ID
        updates.setId(eventId);
        updates.setUserID(getUserId());

        // Make sure timestamp is preserved
        if (updates.getTimestamp() == null) {
            updates.setTimestamp(Timestamp.now());
        }

        // Find and update the event in memory
        MoodEvent existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            // Update the event in our local cache
            List<MoodEvent> events = getAllEvents();
            int index = events.indexOf(existingEvent);
            if (index >= 0) {
                events.set(index, updates);
                setMoodEvents(events);
            }

            // Check if we're online
            FirebaseDB firebaseDB = getFirebaseDB();
            if (firebaseDB.isOnline()) {
                // Update in Firebase
                firebaseDB.updateMoodEvent(eventId, updates, success -> {
                    if (success) {
                        // Successfully updated in Firebase, refresh our cache
                        refresh();
                    } else {
                        // Failed to update in Firebase, queue for later
                        queuePendingOperation(OperationType.UPDATE, updates);
                    }

                    if (callback != null) {
                        callback.onCallback(success);
                    }
                });
            } else {
                // Offline, queue for later
                queuePendingOperation(OperationType.UPDATE, updates);
                if (callback != null) {
                    callback.onCallback(true); // Optimistically return success
                }
            }
        } else {
            Log.e(TAG, "Could not find event with ID: " + eventId + " to edit");
            if (callback != null) {
                callback.onCallback(false);
            }
        }
    }

    /**
     * Overloaded method without callback
     *
     * @param eventId The ID of the event to edit
     * @param updates The updated mood event data
     */
    public void editEvent(String eventId, MoodEvent updates) {
        editEvent(eventId, updates, null);
    }

    /**
     * Delete a mood event
     *
     * @param eventId The ID of the event to delete
     * @param callback Callback to handle result
     */
    public void deleteEvent(String eventId, FirebaseDB.FirebaseCallback<Boolean> callback) {
        // Handle null eventId
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Cannot delete event with null or empty ID");
            if (callback != null) {
                callback.onCallback(false);
            }
            return;
        }

        // Find and remove the event from memory
        MoodEvent existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            // Remove from our local cache
            List<MoodEvent> events = getAllEvents();
            events.remove(existingEvent);
            setMoodEvents(events);

            // Check if we're online
            FirebaseDB firebaseDB = getFirebaseDB();
            if (firebaseDB.isOnline()) {
                // Delete from Firebase
                firebaseDB.deleteMoodEvent(eventId, success -> {
                    if (!success) {
                        // Failed to delete from Firebase, queue for later
                        queuePendingOperation(OperationType.DELETE, existingEvent);
                    }

                    if (callback != null) {
                        callback.onCallback(success);
                    }
                });
            } else {
                // Offline, queue for later
                queuePendingOperation(OperationType.DELETE, existingEvent);
                if (callback != null) {
                    callback.onCallback(true); // Optimistically return success
                }
            }
        } else {
            Log.e(TAG, "Could not find event with ID: " + eventId + " to delete");
            if (callback != null) {
                callback.onCallback(false);
            }
        }
    }

    /**
     * Overloaded method without callback
     *
     * @param eventId The ID of the event to delete
     */
    public void deleteEvent(String eventId) {
        deleteEvent(eventId, null);
    }

    /**
     * Get a specific mood event by ID
     *
     * @param eventId The ID of the event to retrieve
     * @return The mood event or null if not found
     */
    @Override
    public MoodEvent getEventById(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return null;
        }

        // Check all events for matching ID
        for (MoodEvent event : getAllEvents()) {
            if (eventId.equals(event.getId())) {
                return event;
            }
        }
        return null;
    }

    /**
     * Enumeration of operation types for pending operations
     */
    private enum OperationType {
        ADD,
        UPDATE,
        DELETE
    }

    /**
     * Class to represent a pending operation for offline functionality
     */
    private static class PendingOperation {
        private final OperationType type;
        private final MoodEvent event;

        public PendingOperation(OperationType type, MoodEvent event) {
            this.type = type;
            this.event = event;
        }

        public OperationType getType() {
            return type;
        }

        public MoodEvent getEvent() {
            return event;
        }
    }

    /**
     * Queue a pending operation for later execution when online
     *
     * @param type The type of operation
     * @param event The mood event involved
     */
    private void queuePendingOperation(OperationType type, MoodEvent event) {
        // Ensure we don't queue operations for null events
        if (event == null) {
            Log.e(TAG, "Attempted to queue operation for null event");
            return;
        }

        // Make sure the event has a valid ID before queueing
        if (event.getId() == null || event.getId().isEmpty()) {
            String tempId = "local_" + UUID.randomUUID().toString();
            event.setId(tempId);
            Log.d(TAG, "Generated temporary ID for event in pending operation: " + tempId);
        }

        pendingOperations.add(new PendingOperation(type, event));
        Log.d(TAG, "Added pending operation: " + type + " for event ID: " + event.getId());
    }

    /**
     * Sync pending changes with Firebase
     * Executes all queued operations when online
     *
     * @param callback Callback to handle overall result
     */
    public void syncPendingChanges(FirebaseDB.FirebaseCallback<Boolean> callback) {
        if (pendingOperations.isEmpty()) {
            if (callback != null) {
                callback.onCallback(true);
            }
            return;
        }

        FirebaseDB firebaseDB = getFirebaseDB();
        if (!firebaseDB.isOnline()) {
            if (callback != null) {
                callback.onCallback(false);
            }
            return;
        }

        // Process each pending operation
        List<PendingOperation> operationsToProcess = new ArrayList<>(pendingOperations);
        pendingOperations.clear();

        // Simple counter to track when all operations are complete
        final int[] completedOperations = {0};
        final boolean[] allSuccessful = {true};

        // Keep a map of local IDs to server IDs for updating references
        Map<String, String> idMappings = new HashMap<>();

        for (PendingOperation operation : operationsToProcess) {
            // Skip invalid operations
            if (operation.getEvent() == null || operation.getEvent().getId() == null) {
                completedOperations[0]++;
                continue;
            }

            final String originalId = operation.getEvent().getId();
            boolean isLocalId = originalId.startsWith("local_");

            switch (operation.getType()) {
                case ADD:
                    firebaseDB.addMoodEvent(operation.getEvent(), new FirebaseDB.FirebaseCallback<Boolean>() {
                        @Override
                        public void onCallback(Boolean success) {
                            if (success && isLocalId) {
                                // Get the server-generated ID from the response
                                // Note: You'll need to modify FirebaseDB.addMoodEvent to return the generated ID
                                firebaseDB.getMoodEventFromID(operation.getEvent().getId(), serverEvent -> {
                                    if (serverEvent != null && serverEvent.getId() != null) {
                                        // Store mapping from local to server ID
                                        idMappings.put(originalId, serverEvent.getId());

                                        // Update our local cache with the new ID
                                        updateLocalEventId(originalId, serverEvent.getId());
                                    }

                                    if (!success) {
                                        allSuccessful[0] = false;
                                        // Re-queue failed operation
                                        queuePendingOperation(operation.getType(), operation.getEvent());
                                    }
                                    completedOperations[0]++;
                                    checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0],
                                            allSuccessful[0], idMappings, callback);
                                });
                            } else {
                                if (!success) {
                                    allSuccessful[0] = false;
                                    // Re-queue failed operation
                                    queuePendingOperation(operation.getType(), operation.getEvent());
                                }
                                completedOperations[0]++;
                                checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0],
                                        allSuccessful[0], idMappings, callback);
                            }
                        }
                    });
                    break;

                case UPDATE:
                    // If this is updating an event with a local ID, we need to use the server ID
                    String updateId = isLocalId && idMappings.containsKey(originalId) ?
                            idMappings.get(originalId) : originalId;

                    // Clone the event and update its ID if needed
                    MoodEvent updateEvent = cloneEvent(operation.getEvent());
                    if (isLocalId && idMappings.containsKey(originalId)) {
                        updateEvent.setId(idMappings.get(originalId));
                    }

                    firebaseDB.updateMoodEvent(updateId, updateEvent, success -> {
                        if (!success) {
                            allSuccessful[0] = false;
                            // Re-queue failed operation
                            queuePendingOperation(operation.getType(), operation.getEvent());
                        }
                        completedOperations[0]++;
                        checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0],
                                allSuccessful[0], idMappings, callback);
                    });
                    break;

                case DELETE:
                    // If this is deleting an event with a local ID, we may not need to delete it from server
                    if (isLocalId && !idMappings.containsKey(originalId)) {
                        // This was a local-only event that was deleted before being synced
                        // No need to delete from server
                        completedOperations[0]++;
                        checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0],
                                allSuccessful[0], idMappings, callback);
                    } else {
                        // Use the server ID if available
                        String deleteId = isLocalId && idMappings.containsKey(originalId) ?
                                idMappings.get(originalId) : originalId;

                        firebaseDB.deleteMoodEvent(deleteId, success -> {
                            if (!success) {
                                allSuccessful[0] = false;
                                // Re-queue failed operation
                                queuePendingOperation(operation.getType(), operation.getEvent());
                            }
                            completedOperations[0]++;
                            checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0],
                                    allSuccessful[0], idMappings, callback);
                        });
                    }
                    break;
            }
        }
    }

    /**
     * Helper method to check if all operations are complete
     */
    private void checkAllOperationsComplete(int totalOperations, int completedOperations,
                                            boolean allSuccessful, Map<String, String> idMappings,
                                            FirebaseDB.FirebaseCallback<Boolean> callback) {
        if (completedOperations == totalOperations) {
            // Apply ID mappings to any remaining events in our cache
            if (!idMappings.isEmpty()) {
                applyIdMappingsToCache(idMappings);
            }

            if (callback != null) {
                callback.onCallback(allSuccessful);
            }
            // Refresh our data if everything was successful
            if (allSuccessful) {
                refresh();
            }
        }
    }

    /**
     * Update a specific event ID in the local cache
     */
    private void updateLocalEventId(String oldId, String newId) {
        List<MoodEvent> events = getAllEvents();
        for (int i = 0; i < events.size(); i++) {
            MoodEvent event = events.get(i);
            if (event.getId().equals(oldId)) {
                event.setId(newId);
                events.set(i, event);
                break;
            }
        }
        setMoodEvents(events);
    }

    /**
     * Apply a map of ID updates to all events in the cache
     */
    private void applyIdMappingsToCache(Map<String, String> idMappings) {
        List<MoodEvent> events = getAllEvents();
        boolean changed = false;

        for (int i = 0; i < events.size(); i++) {
            MoodEvent event = events.get(i);
            String localId = event.getId();

            if (idMappings.containsKey(localId)) {
                event.setId(idMappings.get(localId));
                events.set(i, event);
                changed = true;
            }
        }

        if (changed) {
            setMoodEvents(events);
        }
    }

    /**
     * Create a deep copy of a MoodEvent
     */
    private MoodEvent cloneEvent(MoodEvent original) {
        MoodEvent clone = new MoodEvent();
        clone.setId(original.getId());
        clone.setUserID(original.getUserID());
        clone.setEmotionalState(original.getEmotionalState());
        clone.setTimestamp(original.getTimestamp());
        clone.setTrigger(original.getTrigger());
        clone.setSocialSituation(original.getSocialSituation());
        clone.setLocation(original.getLocation());
        clone.setPhotoUrl(original.getPhotoUrl());
        clone.setPhotoFilename(original.getPhotoFilename());
        return clone;
    }

    /**
     * Helper method to check if all operations are complete
     */
    private void checkAllOperationsComplete(int totalOperations, int completedOperations,
                                            boolean allSuccessful, FirebaseDB.FirebaseCallback<Boolean> callback) {
        if (completedOperations == totalOperations && callback != null) {
            callback.onCallback(allSuccessful);
            // Refresh our data if everything was successful
            if (allSuccessful) {
                refresh();
            }
        }
    }

    /**
     * Check if there are pending changes to sync
     *
     * @return true if there are pending operations, false otherwise
     */
    public boolean hasPendingChanges() {
        return !pendingOperations.isEmpty();
    }
}