package com.example.team_16.models;

import com.example.team_16.database.FirebaseDB;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single mood event in the mood tracking application.
 * Each mood event captures the user's emotional state at a specific point in time,
 * along with optional contextual information such as triggers and social situation.
 */
public class MoodEvent implements Serializable {

    private static final long serialVersionUID = 1L;
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

    /** Whether post is public or private */
    private String postType = "Public"; // Default to Public

    /** Optional location information for this mood event */
    private Double latitude = null;
    private Double longitude = null;
    private String placeName = "";

    /** Optional photo for this mood event */
    private String photoUrl;

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
        this.postType = "Public";
    }

    /**
     * Creates a new mood event with all optional fields.
     *
     * @param userID The ID of the user creating this mood event
     * @param emotionalState The emotional state being recorded (required)
     * @param trigger What triggered this emotion (optional)
     * @param socialSituation The social context of this mood event (optional)
     * @param latitude The latitude of the selected location (optional)
     * @param longitude The longitude of the selected location (optional)
     * @param placeName The name of the selected location (optional)
     */
    public MoodEvent(String userID, EmotionalState emotionalState, String trigger, String socialSituation,
                     Double latitude, Double longitude, String placeName) {
        this.timestamp = Timestamp.now();
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.userID = userID;
        this.postType = "Public";
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
     * @param latitude The latitude of the selected location (optional)
     * @param longitude The longitude of the selected location (optional)
     * @param placeName The name of the selected location (optional)
     */
    public MoodEvent(String id, Timestamp timestamp, EmotionalState emotionalState, String trigger,
                     String userID, String socialSituation,
                     Double latitude, Double longitude, String placeName) {
        this.id = id;
        this.timestamp = timestamp;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.userID = userID;
        this.socialSituation = socialSituation;
        this.postType = "Public";
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : null;
    }

    public String getTrigger() {
        return trigger;
    }
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public EmotionalState getEmotionalState() {
        return emotionalState;
    }
    public void setEmotionalState(EmotionalState emotionalState) {
        this.emotionalState = emotionalState;
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSocialSituation() {
        return socialSituation;
    }
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
     * Gets the latitude of the selected location.
     *
     * @return The latitude as a double.
     */
    public Double getLatitude() { return latitude; }

    /**
     * Sets the latitude for the mood event's location.
     *
     * @param latitude The latitude coordinate.
     */
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    /**
     * Gets the longitude of the selected location.
     *
     * @return The longitude as a double.
     */
    public Double getLongitude() { return longitude; }

    /**
     * Sets the longitude for the mood event's location.
     *
     * @param longitude The longitude coordinate.
     */
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    /**
     * Gets the place name of the selected location.
     *
     * @return The place name as a String.
     */
    public String getPlaceName() { return placeName; }

    /**
     * Sets the place name for the mood event's location.
     *
     * @param photoUrl The url of the photo chosen.
     */
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    /**
     * Gets the place name of the selected location.
     *
     * @return The photo url as a String.
     */
    public String getPhotoUrl() { return photoUrl; }

    /**
     * Sets the place name for the mood event's location.
     *
     * @param placeName The name of the selected place.
     */
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    /**
     * Determines if the mood event has location data.
     *
     * @return true if location is set, false otherwise.
     */
    @Exclude
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    /**
     * Validates whether this mood event meets the minimum requirements.
     * A valid mood event must have an emotional state.
     *
     * @return true if this mood event is valid, false otherwise
     */
    public String getPostType() {
        return postType;
    }
    public void setPostType(String postType) {
        this.postType = postType;
    }

    @Exclude
    public boolean isValid() {
        return emotionalState != null;
    }

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

    @Exclude
    public MoodEvent copy() {
        MoodEvent copyEvent = new MoodEvent(id, timestamp, emotionalState, trigger, userID, socialSituation, latitude, longitude, placeName);
        copyEvent.setPostType(postType);
        return copyEvent;
    }

    @Exclude
    public String getFormattedDate() {
        if (timestamp == null) {
            return "No date";
        }
        Date date = timestamp.toDate();
        return java.text.DateFormat.getDateTimeInstance().format(date);
    }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id='" + id + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", emotionalState=" + (emotionalState != null ? emotionalState.getName() : "null") +
                ", postType=" + postType +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MoodEvent other = (MoodEvent) obj;
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        if (timestamp != null ? !timestamp.equals(other.timestamp) : other.timestamp != null) return false;
        if (emotionalState != other.emotionalState) return false;
        if (userID != null ? !userID.equals(other.userID) : other.userID != null) return false;
        if (trigger != null ? !trigger.equals(other.trigger) : other.trigger != null) return false;
        if (socialSituation != null ? !socialSituation.equals(other.socialSituation)
                : other.socialSituation != null) return false;
        return postType != null ? postType.equals(other.postType) : other.postType == null;
    }

    @Override
    public int hashCode() {
        int result = (id != null) ? id.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (emotionalState != null ? emotionalState.hashCode() : 0);
        result = 31 * result + (userID != null ? userID.hashCode() : 0);
        result = 31 * result + (postType != null ? postType.hashCode() : 0);
        return result;
    }
}
