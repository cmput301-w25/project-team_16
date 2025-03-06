package com.example.team_16;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * FirebaseDB class to handle all Firebase interactions
 */
public class FirebaseDB {
    // Singleton instance
    private static FirebaseDB instance;

    // Firebase components
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;
    private final Context context;

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String MOODS_COLLECTION = "mood_events";

    /**
     * Private constructor for singleton pattern
     */
    private FirebaseDB(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
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
     * Get singleton instance (use only after initialization)
     */
    public static FirebaseDB getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FirebaseDB not initialized. Call getInstance(Context) first.");
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

    /**
     * Interface for callbacks
     */
    public interface FirebaseCallback<T> {
        void onCallback(T result);
    }

    /**
     * Get a user's mood events
     */
    public void getMoodEvents(String userId, FirebaseCallback<List<MoodEvent>> callback) {
        db.collection(MOODS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MoodEvent> moodEvents = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);
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
     * Get mood events from users the current user follows
     */
    public void getFollowingMoodEvents(String userId, FirebaseCallback<List<MoodEvent>> callback) {
        // First get the user's following list
        db.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    if (userProfile == null || userProfile.getFollowing() == null || userProfile.getFollowing().isEmpty()) {
                        callback.onCallback(new ArrayList<>());
                        return;
                    }

                    // Get mood events from followed users
                    db.collection(MOODS_COLLECTION)
                            .whereIn("userId", userProfile.getFollowing())
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    MoodEvent moodEvent = doc.toObject(MoodEvent.class);
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
                    Log.e("FirebaseDB", "Error getting user profile", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    /**
     * Add a mood event to Firestore
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
     * Update a mood event in Firestore
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
     * Delete a mood event from Firestore
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

    /**
     * Get user profile from Firestore
     */
    public void getUserProfile(String userId, FirebaseCallback<UserProfile> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    callback.onCallback(userProfile);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting user profile", e);
                    callback.onCallback(null);
                });
    }

    /**
     * Update user profile in Firestore
     */
    public void updateUserProfile(String userId, UserProfile updates, FirebaseCallback<Boolean> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .set(updates)
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error updating user profile", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Follow a user
     */
    public void followUser(String userId, String targetUserId, FirebaseCallback<Boolean> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .update("following", FieldValue.arrayUnion(targetUserId))
                .addOnSuccessListener(aVoid -> {
                    // Update the target user's followers list
                    db.collection(USERS_COLLECTION).document(targetUserId)
                            .update("followers", FieldValue.arrayUnion(userId))
                            .addOnSuccessListener(aVoid2 -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error updating followers", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error updating following", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Unfollow a user
     */
    public void unfollowUser(String userId, String targetUserId, FirebaseCallback<Boolean> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .update("following", FieldValue.arrayRemove(targetUserId))
                .addOnSuccessListener(aVoid -> {
                    // Update the target user's followers list
                    db.collection(USERS_COLLECTION).document(targetUserId)
                            .update("followers", FieldValue.arrayRemove(userId))
                            .addOnSuccessListener(aVoid2 -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseDB", "Error updating followers", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error updating following", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Search for users by username
     */
    public void searchUsers(String query, FirebaseCallback<List<UserProfile>> callback) {
        String searchQuery = query.toLowerCase();
        db.collection(USERS_COLLECTION)
                .whereGreaterThanOrEqualTo("usernameLower", searchQuery)
                .whereLessThanOrEqualTo("usernameLower", searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> users = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserProfile user = doc.toObject(UserProfile.class);
                        users.add(user);
                    }
                    callback.onCallback(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error searching users", e);
                    callback.onCallback(new ArrayList<>());
                });
    }


    // Authentication methods (login, signup, etc.)

    /**
     * Login user with email and password
     */
    public void login(String email, String password, FirebaseCallback<Boolean> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Login failed", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Create new user account
     */
    public void signup(String email, String password, String username, FirebaseCallback<Boolean> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Create user profile in Firestore
                        UserProfile userProfile = new UserProfile(
                                user.getUid(),
                                username,
                                email,
                                new ArrayList<>(), // empty following list
                                new ArrayList<>()  // empty followers list
                        );

                        // Add lowercase username for case-insensitive search
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", userProfile.getId());
                        userData.put("username", username);
                        userData.put("usernameLower", username.toLowerCase());
                        userData.put("email", email);
                        userData.put("following", new ArrayList<>());
                        userData.put("followers", new ArrayList<>());

                        db.collection(USERS_COLLECTION).document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseDB", "Error creating user profile", e);
                                    callback.onCallback(false);
                                });
                    } else {
                        callback.onCallback(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Signup failed", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Reset password
     */
    public void resetPassword(String email, FirebaseCallback<Boolean> callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Password reset failed", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Logout user
     */
    public void logout() {
        auth.signOut();
    }
}