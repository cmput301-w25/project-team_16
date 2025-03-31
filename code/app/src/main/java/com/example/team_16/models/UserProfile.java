/**
 * Represents a user's profile in the mood tracking application.
 * This class manages user data, mood history, and social features like following
 * and follow requests. It provides methods for profile updates, password resets,
 * and offline synchronization.
 *
 * Key Features:
 * - Manages user authentication and profile data
 * - Handles mood history (personal and following)
 * - Supports social features (following/followers)
 * - Provides offline data synchronization
 * - Manages profile image uploads
 *
 * Usage:
 * UserProfile instances are typically created during login/signup
 * and stored in the MoodTrackerApp application instance.
 *
 * Example:
 * <pre>
 * UserProfile profile = new UserProfile("user123", "John Doe", "john@example.com");
 * profile.setUsername("johndoe");
 * </pre>
 */
package com.example.team_16.models;


import androidx.annotation.NonNull;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.utils.MoodAnalytics;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.Map;

/**
 * Represents a user in the mood tracking application
 */
public class
UserProfile {

    // Core user information
    private final String id;

    private String username;
    private String fullName;
    private String email;
    private String profileImageUrl;

    private final FirebaseDB firebaseDB;


    // Mood-related components
    private final PersonalMoodHistory personalMoodHistory;
    private final MoodHistory followingMoodHistory;



    // New follow-related fields
    private final List<String> pendingFollow = new ArrayList<>();
    private final List<String> userFollowing = new ArrayList<>();

    public UserProfile(FirebaseDB firebaseDB, String id, String username,
                       String fullName, String email, String profileImageUrl) {
        this.firebaseDB = firebaseDB;
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.personalMoodHistory = new PersonalMoodHistory(id, firebaseDB);
        this.followingMoodHistory = new MoodHistory(id, MoodHistory.MODE_FOLLOWING, firebaseDB);
    }
    // Another constructor if profileImageUrl don't exist  at creation
    public UserProfile(FirebaseDB firebaseDB,
                       String id,
                       String username,
                       String fullName,
                       String email) {
        this(firebaseDB, id, username, fullName, email, null);
    }

    /**
     * Factory method to create UserProfile from Firebase data
     *
     * @param firebaseDB Firebase database instance
     * @param userId User ID to load
     * @param callback Callback to receive the created UserProfile
     */
    public static void loadFromFirebase(FirebaseDB firebaseDB,
                                        String userId,
                                        FirebaseDB.FirebaseCallback<UserProfile> callback) {
        firebaseDB.fetchUserById(userId, userData -> {
            if (userData != null) {
                String username = (String) userData.get("username");
                String fullName = (String) userData.get("fullName");
                String email = (String) userData.get("email");
                String profileImageUrl = (String) userData.get("profileImageUrl");

                UserProfile profile = new UserProfile(firebaseDB, userId, username,
                        fullName, email, profileImageUrl);
                profile.refreshFollowData(() -> callback.onCallback(profile));
            } else {
                callback.onCallback(null);
            }
        });
    }

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
     * @param username New username
     * @param profileImageUrl New profile image URL
     * @param callback Callback to handle update result
     */
    public void updateProfile(String fullName,
                              String email,
                              String username,
                              String profileImageUrl,
                              FirebaseDB.FirebaseCallback<Boolean> callback) {
        firebaseDB.updateUserProfile(this.id, fullName, email, username, profileImageUrl, success -> {
            if (success) {
                this.fullName = fullName;
                this.email = email;
                this.username = username;
                this.profileImageUrl = profileImageUrl;
            }
            if (callback != null) {
                callback.onCallback(success);
            }
        });
    }

    // Overload if you only want to update name/email/username but not the image
    public void updateProfile(String fullName,
                              String email,
                              String username,
                              FirebaseDB.FirebaseCallback<Boolean> callback) {
        updateProfile(fullName, email, username, this.profileImageUrl, callback);
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

    // new code: Setter for username to support updating the username in EditProfileFragment
    public void setUsername(String username) { this.username = username; }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }


    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
    public String getProfileImageUrl() { return profileImageUrl; }

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
    @NonNull
    @Override
    public String toString() {
        return "UserProfile{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }

    /**
     * Get monthly mood statistics for the current month
     * @param callback Callback to receive the statistics
     */
    public void getCurrentMonthStats(FirebaseDB.FirebaseCallback<Map<String, Object>> callback) {
        Calendar cal = Calendar.getInstance();
        getMonthlyStats(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, callback);
    }

    /**
     * Get monthly mood statistics for a specific month
     * @param year The year to analyze
     * @param month The month to analyze (1-12)
     * @param callback Callback to receive the statistics
     */
    public void getMonthlyStats(int year, int month, FirebaseDB.FirebaseCallback<Map<String, Object>> callback) {
        personalMoodHistory.getFilteredEvents(null, null, null, events -> {
            Map<String, Object> stats = MoodAnalytics.getMonthlyStats(events, year, month);

            // Count total entries manually
            long totalEntries = events.stream()
                    .filter(event -> {
                        Calendar eventDate = Calendar.getInstance();
                        eventDate.setTime(event.getDate());
                        return eventDate.get(Calendar.YEAR) == year && eventDate.get(Calendar.MONTH) + 1 == month;
                    })
                    .count();

            stats.put("totalEntries", totalEntries); // Ensure it exists

            callback.onCallback(stats);
        });
    }
      /**
     * Get mood trend for the current month
     * @param callback Callback to receive the trend data
     */
    public void getCurrentMonthTrend(FirebaseDB.FirebaseCallback<Map<Integer, EmotionalState>> callback) {
        Calendar cal = Calendar.getInstance();
        getMonthlyTrend(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, callback);
    }

    /**
     * Get mood trend for a specific month
     * @param year The year to analyze
     * @param month The month to analyze (1-12)
     * @param callback Callback to receive the trend data
     */
    public void getMonthlyTrend(int year, int month, FirebaseDB.FirebaseCallback<Map<Integer, EmotionalState>> callback) {
        personalMoodHistory.getFilteredEvents(null, null, null, events -> {
            Map<Integer, EmotionalState> trend = MoodAnalytics.getMoodTrend(events, year, month);
            callback.onCallback(trend);
        });
    }

    /**
     * Calculate mood stability score for the current month
     * @param callback Callback to receive the stability score
     */
    public void getCurrentMonthStability(FirebaseDB.FirebaseCallback<Double> callback) {
        Calendar cal = Calendar.getInstance();
        getMonthlyStability(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, callback);
    }

    /**
     * Calculate mood stability score for a specific month
     * @param year The year to analyze
     * @param month The month to analyze (1-12)
     * @param callback Callback to receive the stability score
     */
    public void getMonthlyStability(int year, int month, FirebaseDB.FirebaseCallback<Double> callback) {
        personalMoodHistory.getFilteredEvents(null, null, null, events -> {
            double stability = MoodAnalytics.calculateMoodStability(events, year, month);
            callback.onCallback(stability);
        });
    }
}