/**
 * Extends MoodHistory to enable full CRUD operations (add, edit, delete) on a user's mood events.
 * Includes offline support by queuing changes locally and syncing with Firebase when reconnected.
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

        // Add to in-memory collection
        List<MoodEvent> events = getAllEvents();
        events.add(event);
        setMoodEvents(events);

        FirebaseDB firebaseDB = getFirebaseDB();
        if (firebaseDB.isOnline()) {
            firebaseDB.addMoodEvent(event, success -> {
                if (success) {
                    refresh();
                } else {
                    queuePendingOperation(OperationType.ADD, event);
                }

                if (callback != null) {
                    callback.onCallback(success);
                }
            });
        } else {
            queuePendingOperation(OperationType.ADD, event);
            if (callback != null) {
                callback.onCallback(true);
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
        // Ensure event has the correct ID and user ID
        updates.setId(eventId);
        updates.setUserID(getUserId());

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

            FirebaseDB firebaseDB = getFirebaseDB();
            if (firebaseDB.isOnline()) {
                firebaseDB.updateMoodEvent(eventId, updates, success -> {
                    if (success) {
                        refresh();
                    } else {
                        queuePendingOperation(OperationType.UPDATE, updates);
                    }

                    if (callback != null) {
                        callback.onCallback(success);
                    }
                });
            } else {
                queuePendingOperation(OperationType.UPDATE, updates);
                if (callback != null) {
                    callback.onCallback(true);
                }
            }
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
                        queuePendingOperation(OperationType.DELETE, existingEvent);
                    }

                    if (callback != null) {
                        callback.onCallback(success);
                    }
                });
            } else {
                queuePendingOperation(OperationType.DELETE, existingEvent);
                if (callback != null) {
                    callback.onCallback(true);
                }
            }
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