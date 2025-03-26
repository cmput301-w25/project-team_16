package com.example.team_16.models;

import com.example.team_16.database.FirebaseDB;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Represents a single mood event in the mood tracking application.
 * Each mood event captures the user's emotional state at a specific point in time,
 * along with optional contextual information such as triggers and social situation.
 */
public class MoodEvent {
    /** Unique identifier for the mood event */
    private String id;

    /** Timestamp of when the mood event occurred, automatically set by Firestore */
    @ServerTimestamp
    private Timestamp timestamp;

    /** Optional description of what triggered this emotional state */
    private String trigger;

    /** The emotional state recorded for this mood event (required) */
    private EmotionalState emotionalState;

    /** Reference to the user who created this mood event */
    private String userID;

    /** Optional description of the social context (e.g., "With friends") */
    private String socialSituation;
    private String photoFilename;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public MoodEvent() {
    }

    /**
     * Creates a new mood event with required fields only.
     *
     * @param userID The ID of the user creating this mood event
     * @param emotionalState The emotional state being recorded (required)
     */
    public MoodEvent(String userID, EmotionalState emotionalState) {
        this.timestamp = Timestamp.now();
        this.emotionalState = emotionalState;
        this.userID = userID;
    }

    /**
     * Creates a new mood event with all optional fields.
     *
     * @param userID The ID of the user creating this mood event
     * @param emotionalState The emotional state being recorded (required)
     * @param trigger What triggered this emotion (optional)
     * @param socialSituation The social context of this mood event (optional)
     */
    public MoodEvent(String userID, EmotionalState emotionalState, String trigger, String socialSituation) {
        this.timestamp = Timestamp.now();
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.userID = userID;
    }

    /**
     * Complete constructor with all fields including ID and timestamp.
     * Primarily used for testing or when reconstructing from database.
     *
     * @param id The unique identifier for this mood event
     * @param timestamp When this mood event occurred
     * @param emotionalState The emotional state being recorded
     * @param trigger What triggered this emotion
     * @param userID The ID of the user who created this mood event
     * @param socialSituation The social context of this mood event
     */
    public MoodEvent(String id, Timestamp timestamp, EmotionalState emotionalState, String trigger,
                     String userID, String socialSituation) {
        this.id = id;
        this.timestamp = timestamp;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.userID = userID;
        this.socialSituation = socialSituation;
    }

    /**
     * Gets the unique identifier of this mood event.
     *
     * @return The ID of this mood event
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this mood event.
     *
     * @param id The ID to set for this mood event
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the timestamp when this mood event occurred.
     *
     * @return The timestamp of this mood event
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for this mood event.
     *
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Converts the Firestore Timestamp to a Java Date object for UI display.
     * This method is not stored in Firestore.
     *
     * @return A Java Date representation of the timestamp, or null if timestamp is null
     */
    @Exclude
    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : null;
    }

    /**
     * Gets what triggered this emotional state.
     *
     * @return The trigger description, or null if not specified
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Sets what triggered this emotional state.
     *
     * @param trigger The trigger description to set
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /**
     * Gets the emotional state recorded for this mood event.
     *
     * @return The emotional state
     */
    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    /**
     * Sets the emotional state for this mood event.
     *
     * @param emotionalState The emotional state to set
     */
    public void setEmotionalState(EmotionalState emotionalState) {
        this.emotionalState = emotionalState;
    }

    /**
     * Gets the ID of the user who created this mood event.
     *
     * @return The user ID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the ID of the user who created this mood event.
     *
     * @param userID The user ID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Gets the social situation associated with this mood event.
     *
     * @return The social situation description, or null if not specified
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Sets the social situation for this mood event.
     *
     * @param socialSituation The social situation description to set
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getPhotoFilename() {
        return photoFilename;
    }

    public void setPhotoFilename(String photoFilename) {
        this.photoFilename = photoFilename;
    }

    /**
     * Validates whether this mood event meets the minimum requirements.
     * A valid mood event must have an emotional state.
     *
     * @return true if this mood event is valid, false otherwise
     */
    @Exclude
    public boolean isValid() {
        // Emotional state is the only required field for a valid mood event
        return emotionalState != null;
    }

    /**
     * Helper method to save this mood event to Firestore.
     *
     * @param firebaseDB The FirebaseDB instance to use for saving
     * @param callback Callback to handle the result
     */
    @Exclude
    public void saveToFirestore(FirebaseDB firebaseDB, FirebaseDB.FirebaseCallback<Boolean> callback) {
        if (!isValid()) {
            if (callback != null) {
                callback.onCallback(false);
            }
            return;
        }

        firebaseDB.addMoodEvent(this, callback);
    }

    /**
     * Helper method to update this mood event in Firestore.
     *
     * @param firebaseDB The FirebaseDB instance to use for updating
     * @param callback Callback to handle the result
     */
    @Exclude
    public void updateInFirestore(FirebaseDB firebaseDB, FirebaseDB.FirebaseCallback<Boolean> callback) {
        if (!isValid() || id == null) {
            if (callback != null) {
                callback.onCallback(false);
            }
            return;
        }

        firebaseDB.updateMoodEvent(id, this, callback);
    }

    /**
     * Helper method to delete this mood event from Firestore.
     *
     * @param firebaseDB The FirebaseDB instance to use for deleting
     * @param callback Callback to handle the result
     */
    @Exclude
    public void deleteFromFirestore(FirebaseDB firebaseDB, FirebaseDB.FirebaseCallback<Boolean> callback) {
        if (id == null) {
            if (callback != null) {
                callback.onCallback(false);
            }
            return;
        }

        firebaseDB.deleteMoodEvent(id, callback);
    }

    /**
     * Creates a copy of this mood event.
     *
     * @return A new MoodEvent with the same values
     */
    @Exclude
    public MoodEvent copy() {
        return new MoodEvent(id, timestamp, emotionalState, trigger, userID, socialSituation);
    }

    /**
     * Format timestamp as human-readable date string.
     *
     * @return Formatted date string or "No date" if timestamp is null
     */
    @Exclude
    public String getFormattedDate() {
        if (timestamp == null) {
            return "No date";
        }

        Date date = timestamp.toDate();
        return java.text.DateFormat.getDateTimeInstance().format(date);
    }

    /**
     * Returns a simple string representation of this mood event.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "MoodEvent{" +
                "id='" + id + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", emotionalState=" + (emotionalState != null ? emotionalState.getName() : "null") +
                '}';
    }

    /**
     * Checks whether this mood event equals another object.
     *
     * @param obj The object to compare with
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MoodEvent other = (MoodEvent) obj;

        // If both have IDs, compare IDs
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        // Otherwise compare all fields
        if (timestamp != null ? !timestamp.equals(other.timestamp) : other.timestamp != null)
            return false;
        if (emotionalState != other.emotionalState)
            return false;
        if (userID != null ? !userID.equals(other.userID) : other.userID != null)
            return false;
        if (trigger != null ? !trigger.equals(other.trigger) : other.trigger != null)
            return false;
        return socialSituation != null ? socialSituation.equals(other.socialSituation) : other.socialSituation == null;
    }

    /**
     * Generates a hash code for this mood event.
     *
     * @return Hash code value
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (emotionalState != null ? emotionalState.hashCode() : 0);
        result = 31 * result + (userID != null ? userID.hashCode() : 0);
        return result;
    }
}