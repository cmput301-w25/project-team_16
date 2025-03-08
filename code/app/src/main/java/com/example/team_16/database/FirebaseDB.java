package com.example.team_16.database;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Comprehensive Firebase Database Management for Mood Tracking App
 */
public class FirebaseDB {
    // Singleton instance
    private static FirebaseDB instance;

    // Firebase components
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String MOODS_COLLECTION = "mood_events";
    private static final String FOLLOW_REQUESTS_COLLECTION = "follow_requests";
    private static final String FOLLOWING_COLLECTION = "following";

    /**
     * Interface for callbacks
     */
    public interface FirebaseCallback<T> {
        void onCallback(T result);
    }

    /**
     * Private constructor for singleton pattern
     */
    private FirebaseDB(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Get singleton instance
     */
    public static synchronized FirebaseDB getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseDB(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Get current authenticated user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Check if device is online
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // Authentication Methods

    /**
     * Sign up a new user
     */
    public void signup(
            String fullName,
            String username,
            String email,
            String password,
            FirebaseCallback<Boolean> callback) {

        // First, check if username is unique
        db.collection(USERS_COLLECTION)
                .whereEqualTo("usernameLower", username.toLowerCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Username already exists
                        callback.onCallback(false);
                        return;
                    }

                    // Create user with email and password
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser firebaseUser = authResult.getUser();
                                if (firebaseUser != null) {
                                    // Create user profile in Firestore
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("id", firebaseUser.getUid());
                                    userData.put("fullName", fullName);
                                    userData.put("username", username);
                                    userData.put("usernameLower", username.toLowerCase());
                                    userData.put("email", email);

                                    db.collection(USERS_COLLECTION)
                                            .document(firebaseUser.getUid())
                                            .set(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                // Create following document for the user
                                                Map<String, Object> followingData = new HashMap<>();
                                                followingData.put("userId", firebaseUser.getUid());
                                                followingData.put("following", new ArrayList<>());

                                                db.collection(FOLLOWING_COLLECTION)
                                                        .document(firebaseUser.getUid())
                                                        .set(followingData)
                                                        .addOnSuccessListener(v -> callback.onCallback(true))
                                                        .addOnFailureListener(e -> callback.onCallback(false));
                                            })
                                            .addOnFailureListener(e -> callback.onCallback(false));
                                } else {
                                    callback.onCallback(false);
                                }
                            })
                            .addOnFailureListener(e -> callback.onCallback(false));
                })
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Sign in a user
     */
    public void login(String username, String password, FirebaseCallback<Boolean> callback) {
        // Find the user by username
        db.collection(USERS_COLLECTION)
                .whereEqualTo("usernameLower", username.toLowerCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No user found with this username
                        callback.onCallback(false);
                        return;
                    }

                    // Get the user's email
                    String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");

                    if (email == null) {
                        callback.onCallback(false);
                        return;
                    }

                    // Attempt to sign in with email and password
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> callback.onCallback(true))
                            .addOnFailureListener(e -> callback.onCallback(false));
                })
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Logout user
     */
    public void logout() {
        auth.signOut();
    }

    // Mood Event Methods

    /**
     * Add a mood event
     */
    public void addMoodEvent(MoodEvent moodEvent, FirebaseCallback<Boolean> callback) {
        db.collection(MOODS_COLLECTION)
                .add(moodEvent)
                .addOnSuccessListener(documentReference -> {
                    // Update the mood event with the generated ID
                    String id = documentReference.getId();
                    db.collection(MOODS_COLLECTION).document(id)
                            .update("id", id)
                            .addOnSuccessListener(aVoid -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error updating mood ID", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error adding mood event", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Get mood events with filtering
     */
    public void getMoodEvents(
            String userId,
            Date startDate,
            EmotionalState emotionalState,
            String searchText,
            FirebaseCallback<List<MoodEvent>> callback) {

        Query query = db.collection(MOODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // Apply date filter if startDate is provided
        if (startDate != null) {
            query = query.whereGreaterThan("timestamp", startDate);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MoodEvent> moodEvents = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);

                        // Apply emotional state filter
                        if (emotionalState != null && moodEvent.getEmotionalState() != emotionalState) {
                            continue;
                        }

                        // Apply text search filter
                        if (searchText != null && !searchText.isEmpty()) {
                            if (moodEvent.getTrigger() == null ||
                                    !moodEvent.getTrigger().toLowerCase().contains(searchText.toLowerCase())) {
                                continue;
                            }
                        }

                        moodEvents.add(moodEvent);
                    }
                    callback.onCallback(moodEvents);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting mood events", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Update a mood event
     */
    public void updateMoodEvent(String eventId, MoodEvent updates, FirebaseCallback<Boolean> callback) {
        db.collection(MOODS_COLLECTION).document(eventId)
                .set(updates)
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error updating mood event", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Delete a mood event
     */
    public void deleteMoodEvent(String eventId, FirebaseCallback<Boolean> callback) {
        db.collection(MOODS_COLLECTION).document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error deleting mood event", e);
                    callback.onCallback(false);
                });
    }

    // Follow Methods

    /**
     * Send a follow request
     */
    public void sendFollowRequest(String fromUserId, String toUserId, FirebaseCallback<Boolean> callback) {
        // Check if a pending request already exists
        db.collection(FOLLOW_REQUESTS_COLLECTION)
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Request already exists
                        callback.onCallback(false);
                        return;
                    }

                    // Create new follow request
                    Map<String, Object> followRequest = new HashMap<>();
                    followRequest.put("fromUserId", fromUserId);
                    followRequest.put("toUserId", toUserId);
                    followRequest.put("status", "pending");
                    followRequest.put("timestamp", System.currentTimeMillis());

                    db.collection(FOLLOW_REQUESTS_COLLECTION)
                            .add(followRequest)
                            .addOnSuccessListener(documentReference -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error sending follow request", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error checking existing follow requests", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Respond to a follow request
     */
    public void respondToFollowRequest(String requestId, boolean accept, FirebaseCallback<Boolean> callback) {
        db.collection(FOLLOW_REQUESTS_COLLECTION).document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onCallback(false);
                        return;
                    }

                    String fromUserId = documentSnapshot.getString("fromUserId");
                    String toUserId = documentSnapshot.getString("toUserId");

                    if (fromUserId == null || toUserId == null) {
                        callback.onCallback(false);
                        return;
                    }

                    // Update request status
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", accept ? "accepted" : "rejected");

                    db.collection(FOLLOW_REQUESTS_COLLECTION).document(requestId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                if (accept) {
                                    // Add to following collection
                                    addToFollowing(fromUserId, toUserId, callback);
                                } else {
                                    callback.onCallback(true);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error updating follow request", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error retrieving follow request", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Add user to following list
     */
    private void addToFollowing(String followerId, String followedId, FirebaseCallback<Boolean> callback) {
        db.collection(FOLLOWING_COLLECTION).document(followerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following == null) {
                        following = new ArrayList<>();
                    }

                    if (!following.contains(followedId)) {
                        following.add(followedId);
                    }

                    db.collection(FOLLOWING_COLLECTION).document(followerId)
                            .update("following", following)
                            .addOnSuccessListener(aVoid -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error updating following list", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error retrieving following list", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Get mood events for followed users
     */
    public void getFollowingMoodEvents(
            String userId,
            Date startDate,
            EmotionalState emotionalState,
            String searchText,
            FirebaseCallback<List<MoodEvent>> callback) {

        // First get the user's following list
        db.collection(FOLLOWING_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following == null || following.isEmpty()) {
                        callback.onCallback(new ArrayList<>());
                        return;
                    }

                    // Construct query for followed users' mood events
                    Query query = db.collection(MOODS_COLLECTION)
                            .whereIn("userId", following)
                            .orderBy("timestamp", Query.Direction.DESCENDING);

                    // Apply date filter if startDate is provided
                    if (startDate != null) {
                        query = query.whereGreaterThan("timestamp", startDate);
                    }

                    query.get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    MoodEvent moodEvent = doc.toObject(MoodEvent.class);

                                    // Apply emotional state filter
                                    if (emotionalState != null && moodEvent.getEmotionalState() != emotionalState) {
                                        continue;
                                    }

                                    // Apply text search filter
                                    if (searchText != null && !searchText.isEmpty()) {
                                        if (moodEvent.getTrigger() == null ||!moodEvent.getTrigger().toLowerCase().contains(searchText.toLowerCase())) {
                                            continue;
                                        }
                                    }

                                    moodEvents.add(moodEvent);
                                }
                                callback.onCallback(moodEvents);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error getting following mood events", e);
                                callback.onCallback(new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting following list", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Get list of users the current user is following
     */
    public void getFollowingList(String userId, FirebaseCallback<List<String>> callback) {
        db.collection(FOLLOWING_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    callback.onCallback(following != null ? following : new ArrayList<>());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting following list", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Unfollow a user
     */
    public void unfollowUser(String followerId, String followedId, FirebaseCallback<Boolean> callback) {
        db.collection(FOLLOWING_COLLECTION).document(followerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following == null) {
                        callback.onCallback(false);
                        return;
                    }

                    following.remove(followedId);

                    db.collection(FOLLOWING_COLLECTION).document(followerId)
                            .update("following", following)
                            .addOnSuccessListener(aVoid -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error updating following list", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error retrieving following list", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Get pending follow requests for a user
     */
    public void getPendingFollowRequests(String userId, FirebaseCallback<List<Map<String, Object>>> callback) {
        db.collection(FOLLOW_REQUESTS_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> requests = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> requestData = doc.getData();
                        if (requestData != null) {
                            requestData.put("requestId", doc.getId());
                            requests.add(requestData);
                        }
                    }
                    callback.onCallback(requests);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting follow requests", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Fetch user data by user ID
     */
    public void fetchUserById(String userId, FirebaseCallback<Map<String, Object>> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onCallback(documentSnapshot.getData());
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error fetching user", e);
                    callback.onCallback(null);
                });
    }

    /**
     * Get list of users who follow the specified user
     */
    public void getFollowersOfUser(String userId, FirebaseCallback<List<String>> callback) {
        db.collection(FOLLOWING_COLLECTION)
                .whereArrayContains("following", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followerIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        followerIds.add(doc.getId());
                    }
                    callback.onCallback(followerIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting followers", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Update user profile information
     *
     * @param userId ID of the user to update
     * @param fullName New full name
     * @param email New email address
     * @param callback Callback to handle update result
     */
    public void updateUserProfile(
            String userId,
            String fullName,
            String email,
            FirebaseCallback<Boolean> callback) {

        // Create a map of updates
        Map<String, Object> updates = new HashMap<>();

        // Only add fields that are not null or empty
        if (fullName != null && !fullName.trim().isEmpty()) {
            updates.put("fullName", fullName);
        }

        if (email != null && !email.trim().isEmpty()) {
            updates.put("email", email);
        }

        // Check if there are any updates to make
        if (updates.isEmpty()) {
            callback.onCallback(false);
            return;
        }

        // Update the user document in Firestore
        db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Optional: Update email in Firebase Authentication if email changed
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null && updates.containsKey("email")) {
                        currentUser.updateEmail(email)
                                .addOnSuccessListener(emailUpdateVoid -> callback.onCallback(true))
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseDB", "Error updating email in Authentication", e);
                                    callback.onCallback(false);
                                });
                    } else {
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error updating user profile", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Send a password reset email
     */
    public void sendPasswordResetEmail(String email, FirebaseCallback<Boolean> callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    // Reset email sent
                    callback.onCallback(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error sending password reset email", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Searches for users by username.
     */
    public void searchUsersByUsername(String username, FirebaseCallback<List<User>> callback) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("usernameLower", username.toLowerCase())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    callback.onCallback(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error searching users", e);
                    callback.onCallback(new ArrayList<>());
                });
    }


}