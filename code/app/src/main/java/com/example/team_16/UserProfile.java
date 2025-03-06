package com.example.team_16;

import java.util.List;
import java.util.Map;
import android.content.Context;

/**
 * Represents a user in the mood tracking application
 */
public class UserProfile {
    // Core user information
    private String id;
    private String username;
    private String fullName;
    private String email;

    // Firebase database interface
    private FirebaseDB firebaseDB;

    // Mood-related components
    private PersonalMoodHistory personalMoodHistory;
    private MoodHistory followingMoodHistory;

    /**
     * Constructor for creating a new user profile
     *
     * @param context Android context for Firebase initialization
     * @param id Unique user identifier
     * @param username User's unique username
     * @param fullName User's full name
     * @param email User's email address
     */
    public UserProfile(Context context, String id, String username, String fullName, String email) {
        this.firebaseDB = FirebaseDB.getInstance(context);
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;

        // Initialize mood histories
        this.personalMoodHistory = new PersonalMoodHistory(id);
        this.followingMoodHistory = new MoodHistory(id, MoodHistory.MODE_FOLLOWING);
    }

    // Follow-related Methods

    /**
     * Send a follow request to another user
     *
     * @param targetUserId ID of the user to follow
     * @param callback Callback to handle follow request result
     */
    public void sendFollowRequest(String targetUserId, FirebaseDB.FirebaseCallback<Boolean> callback) {
        firebaseDB.sendFollowRequest(this.id, targetUserId, callback);
    }

    /**
     * Respond to a follow request
     *
     * @param requestId ID of the follow request
     * @param accept Whether to accept or reject the request
     * @param callback Callback to handle response result
     */
    public void respondToFollowRequest(String requestId, boolean accept, FirebaseDB.FirebaseCallback<Boolean> callback) {
        firebaseDB.respondToFollowRequest(requestId, accept, callback);
    }

    /**
     * Get list of users this user is following
     *
     * @param callback Callback to receive list of followed user IDs
     */
    public void getFollowingList(FirebaseDB.FirebaseCallback<List<String>> callback) {
        firebaseDB.getFollowingList(this.id, callback);
    }

    /**
     * Unfollow a user
     *
     * @param targetUserId ID of the user to unfollow
     * @param callback Callback to handle unfollow result
     */
    public void unfollowUser(String targetUserId, FirebaseDB.FirebaseCallback<Boolean> callback) {
        firebaseDB.unfollowUser(this.id, targetUserId, callback);
    }

    /**
     * Get pending follow requests for this user
     *
     * @param callback Callback to receive list of pending follow requests
     */
    public void getPendingFollowRequests(FirebaseDB.FirebaseCallback<List<Map<String, Object>>> callback) {
        firebaseDB.getPendingFollowRequests(this.id, callback);
    }

    // Mood Event Methods

    /**
     * Add a mood event to personal mood history
     *
     * @param event Mood event to add
     */
    public void addMoodEvent(MoodEvent event) {
        // Ensure the event is associated with this user
        event.setUserID(this.id);
        personalMoodHistory.addEvent(event);
    }

    /**
     * Edit an existing mood event in personal mood history
     *
     * @param eventId ID of the event to edit
     * @param updates Updated mood event details
     */
    public void editMoodEvent(String eventId, MoodEvent updates) {
        personalMoodHistory.editEvent(eventId, updates);
    }

    /**
     * Delete a mood event from personal mood history
     *
     * @param eventId ID of the event to delete
     */
    public void deleteMoodEvent(String eventId) {
        personalMoodHistory.deleteEvent(eventId);
    }

    /**
     * Get personal mood history
     *
     * @return PersonalMoodHistory for this user
     */
    public PersonalMoodHistory getPersonalMoodHistory() {
        return personalMoodHistory;
    }

    /**
     * Get following mood history
     *
     * @return MoodHistory for followed users
     */
    public MoodHistory getFollowingMoodHistory() {
        return followingMoodHistory;
    }

    /**
     * Sync pending changes in personal mood history
     */
    public void syncPendingChanges() {
        personalMoodHistory.syncPendingChanges();
    }

    /**
     * Check if there are pending changes in personal mood history
     *
     * @return true if there are pending changes, false otherwise
     */
    public boolean hasPendingChanges() {
        return personalMoodHistory.hasPendingChanges();
    }

    // User Profile Management

    /**
     * Update user profile information
     *
     * @param fullName New full name
     * @param email New email address
     * @param callback Callback to handle update result
     */
    public void updateProfile(String fullName, String email, FirebaseDB.FirebaseCallback<Boolean> callback) {
        this.fullName = fullName;
        this.email = email;
        firebaseDB.updateUserProfile(this.id, fullName, email, callback);
    }

    // Getters for user information

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}