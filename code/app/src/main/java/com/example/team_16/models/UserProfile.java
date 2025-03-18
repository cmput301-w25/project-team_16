package com.example.team_16.models;

import android.util.Log;

import com.example.team_16.database.FirebaseDB;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a user in the mood tracking application
 */
public class
UserProfile {
    // Core user information
    private String id;
    private String username;
    private String fullName;
    private String email;

    // Firebase database interface
    private final FirebaseDB firebaseDB;

    // Mood-related components
    private PersonalMoodHistory personalMoodHistory;
    private MoodHistory followingMoodHistory;

    /**
     * Constructor for creating a new user profile
     *
     * @param firebaseDB Firebase database instance
     * @param id Unique user identifier
     * @param username User's unique username
     * @param fullName User's full name
     * @param email User's email address
     */
    // New follow-related fields
    private List<String> pendingFollow = new ArrayList<>();
    private List<String> userFollowing = new ArrayList<>();

    public UserProfile(FirebaseDB firebaseDB, String id, String username,
                       String fullName, String email) {
        this.firebaseDB = firebaseDB;
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.personalMoodHistory = new PersonalMoodHistory(id, firebaseDB);
        this.followingMoodHistory = new MoodHistory(id, MoodHistory.MODE_FOLLOWING, firebaseDB);
    }

    /**
     * Factory method to create UserProfile from Firebase data
     *
     * @param firebaseDB Firebase database instance
     * @param userId User ID to load
     * @param callback Callback to receive the created UserProfile
     */
    public static void loadFromFirebase(FirebaseDB firebaseDB, String userId,
                                        FirebaseDB.FirebaseCallback<UserProfile> callback) {
        firebaseDB.fetchUserById(userId, userData -> {
            if (userData != null) {
                String username = (String) userData.get("username");
                String fullName = (String) userData.get("fullName");
                String email = (String) userData.get("email");

                UserProfile profile = new UserProfile(firebaseDB, userId, username, fullName, email);
                profile.refreshFollowData(() -> callback.onCallback(profile));
            } else {
                callback.onCallback(null);
            }
        });
    }

    // Follow-related Methods
    // Follow-related Methods
    public void refreshFollowData(Runnable completion) {
        firebaseDB.getSentFollowRequests(this.id, requests -> {
            pendingFollow.clear();
            for (Map<String, Object> request : requests) {
                String toUserId = (String) request.get("toUserId");
                pendingFollow.add(toUserId);
            }

            firebaseDB.getFollowingList(this.id, followingList -> {
                userFollowing.clear();
                if (followingList != null) {
                    userFollowing.addAll(followingList);
                }
                if (completion != null) completion.run();
            });
        });
    }

    public List<String> getPendingFollow() {
        return new ArrayList<>(pendingFollow);
    }

    public List<String> getUserFollowing() {
        return new ArrayList<>(userFollowing);
    }

    public void searchUsersByUsername(String query, FirebaseDB.FirebaseCallback<List<Map<String, Object>>> callback) {
        firebaseDB.searchUsersByUsername(query, callback);
    }

    /**
     * Send a follow request to another user
     *
     * @param targetUserId ID of the user to follow
     * @param callback Callback to handle follow request result
     */
    public void sendFollowRequest(String targetUserId, FirebaseDB.FirebaseCallback<Boolean> callback) {
        firebaseDB.sendFollowRequest(this.id, targetUserId, success -> {
            if (success) {
                pendingFollow.add(targetUserId);
            }
            if (callback != null) callback.onCallback(success);
        });
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
     * Get list of users following this user
     *
     * @param callback Callback to receive list of follower user IDs
     */
    public void getFollowers(FirebaseDB.FirebaseCallback<List<String>> callback) {
        firebaseDB.getFollowersOfUser(this.id, callback);
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

    /**
     * Check if this user is following another user
     *
     * @param targetUserId ID of the user to check
     * @param callback Callback with boolean result
     */
    public void isFollowing(String targetUserId, FirebaseDB.FirebaseCallback<Boolean> callback) {
        getFollowingList(followingList -> {
            boolean isFollowing = followingList != null && followingList.contains(targetUserId);
            callback.onCallback(isFollowing);
        });
    }

    // Mood Event Methods

    /**
     * Add a mood event to personal mood history
     *
     * @param event Mood event to add
     * @param callback Callback to handle result
     */
    public void addMoodEvent(MoodEvent event, FirebaseDB.FirebaseCallback<Boolean> callback) {
        // Ensure the event is associated with this user
        event.setUserID(this.id);
        personalMoodHistory.addEvent(event, callback);
    }

    /**
     * Overloaded method without callback
     *
     * @param event Mood event to add
     */
    public void addMoodEvent(MoodEvent event) {
        addMoodEvent(event, null);
    }

    /**
     * Edit an existing mood event in personal mood history
     *
     * @param eventId ID of the event to edit
     * @param updates Updated mood event details
     * @param callback Callback to handle result
     */
    public void editMoodEvent(String eventId, MoodEvent updates, FirebaseDB.FirebaseCallback<Boolean> callback) {
        updates.setUserID(this.id);
        personalMoodHistory.editEvent(eventId, updates, callback);
    }

    /**
     * Overloaded method without callback
     *
     * @param eventId ID of the event to edit
     * @param updates Updated mood event details
     */
    public void editMoodEvent(String eventId, MoodEvent updates) {
        editMoodEvent(eventId, updates, null);
    }

    /**
     * Delete a mood event from personal mood history
     *
     * @param eventId ID of the event to delete
     * @param callback Callback to handle result
     */
    public void deleteMoodEvent(String eventId, FirebaseDB.FirebaseCallback<Boolean> callback) {
        personalMoodHistory.deleteEvent(eventId, callback);
    }

    /**
     * Overloaded method without callback
     *
     * @param eventId ID of the event to delete
     */
    public void deleteMoodEvent(String eventId) {
        deleteMoodEvent(eventId, null);
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
     * Refresh mood histories from Firebase
     */
    public void refreshMoodHistories() {
        personalMoodHistory.refresh();
        followingMoodHistory.refresh();
    }

    /**
     * Sync pending changes in personal mood history
     *
     * @param callback Callback to handle sync result
     */
    public void syncPendingChanges(FirebaseDB.FirebaseCallback<Boolean> callback) {
        personalMoodHistory.syncPendingChanges(callback);
    }

    /**
     * Overloaded method without callback
     */
    public void syncPendingChanges() {
        personalMoodHistory.syncPendingChanges(null);
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
        firebaseDB.updateUserProfile(this.id, fullName, email, success -> {
            if (success) {
                this.fullName = fullName;
                this.email = email;
            }
            if (callback != null) {
                callback.onCallback(success);
            }
        });
    }

    /**
     * Send a password reset email
     *
     * @param callback Callback to handle result
     */
    public void resetPassword(FirebaseDB.FirebaseCallback<Boolean> callback) {
        firebaseDB.sendPasswordResetEmail(this.email, callback);
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

    /**
     * Get the FirebaseDB instance associated with this user profile
     *
     * @return FirebaseDB instance
     */
    public FirebaseDB getFirebaseDB() {
        return firebaseDB;
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        firebaseDB.logout();
    }

    /**
     * String representation of the user
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "UserProfile{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}