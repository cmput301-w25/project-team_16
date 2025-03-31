/**
 * Extends MoodHistory to provide full CRUD operations for a user's personal mood events.
 * This class supports adding, editing, and deleting mood events, with offline capabilities
 * through a queue of pending operations.
 *
 * Key Features:
 * - Full CRUD operations for mood events
 * - Offline support with operation queuing
 * - Automatic synchronization with Firebase when online
 * - Handles image uploads for mood events
 * - Manages pending operations queue
 *
 * Usage:
 * This class is typically used as part of a UserProfile to manage the user's
 * personal mood entries. It provides write operations that the base MoodHistory
 * class does not support.
 *
 * Example:
 * <pre>
 * PersonalMoodHistory history = new PersonalMoodHistory();
 * history.addEvent(newMoodEvent, success -> {
 *     if (success) {
 *         // Event added successfully
 *     }
 * });
 * </pre>
 */

package com.example.team_16.models;

import com.example.team_16.database.FirebaseDB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PersonalMoodHistory extends MoodHistory {
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
        event.setUserID(getUserId());

    
        if (event.getId() == null || event.getId().isEmpty()) {
            event.setId(UUID.randomUUID().toString());
        }

        // Add to in-memory list
        List<MoodEvent> events = getAllEvents();
        events.add(event);
        setMoodEvents(events);

        // Directly write to Firestore (Firestore handles offline queuing)
        FirebaseDB firebaseDB = getFirebaseDB();
        firebaseDB.addMoodEvent(event, success -> {
            if (callback != null) {
                callback.onCallback(success);
            }
        });
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
        updates.setId(eventId);
        updates.setUserID(getUserId());

        // Update in-memory list
        MoodEvent existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            List<MoodEvent> events = getAllEvents();
            int index = events.indexOf(existingEvent);
            if (index >= 0) {
                events.set(index, updates);
                setMoodEvents(events);
            }

            // Directly update Firestore
            getFirebaseDB().updateMoodEvent(eventId, updates, callback);
        } else if (callback != null) {
            callback.onCallback(false);
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
        MoodEvent existingEvent = getEventById(eventId);
        if (existingEvent != null) {
            List<MoodEvent> events = getAllEvents();
            events.remove(existingEvent);
            setMoodEvents(events);

            // Directly delete from Firestore
            getFirebaseDB().deleteMoodEvent(eventId, callback);
        } else if (callback != null) {
            callback.onCallback(false);
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

    public void getMonthlyEvents(int year, int month, Object o) {
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
        pendingOperations.add(new PendingOperation(type, event));
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

        List<PendingOperation> operationsToProcess = new ArrayList<>(pendingOperations);
        pendingOperations.clear();

        final int[] completedOperations = {0};
        final boolean[] allSuccessful = {true};

        for (PendingOperation operation : operationsToProcess) {
            switch (operation.getType()) {
                case ADD:
                    firebaseDB.addMoodEvent(operation.getEvent(), success -> {
                        if (!success) {
                            allSuccessful[0] = false;
                            queuePendingOperation(operation.getType(), operation.getEvent());
                        }
                        completedOperations[0]++;
                        checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0], allSuccessful[0], callback);
                    });
                    break;

                case UPDATE:
                    firebaseDB.updateMoodEvent(operation.getEvent().getId(), operation.getEvent(), success -> {
                        if (!success) {
                            allSuccessful[0] = false;
                            queuePendingOperation(operation.getType(), operation.getEvent());
                        }
                        completedOperations[0]++;
                        checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0], allSuccessful[0], callback);
                    });
                    break;

                case DELETE:
                    firebaseDB.deleteMoodEvent(operation.getEvent().getId(), success -> {
                        if (!success) {
                            allSuccessful[0] = false;
                            queuePendingOperation(operation.getType(), operation.getEvent());
                        }
                        completedOperations[0]++;
                        checkAllOperationsComplete(operationsToProcess.size(), completedOperations[0], allSuccessful[0], callback);
                    });
                    break;
            }
        }
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