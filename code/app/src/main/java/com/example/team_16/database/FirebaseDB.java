package com.example.team_16.database;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
            FirebaseCallback<String> callback) {

        // First, check if username is unique
        db.collection(USERS_COLLECTION)
                .whereEqualTo("usernameLower", username.toLowerCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Username already exists
                        callback.onCallback("Username is already taken. Please choose another.");
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
                                            .addOnSuccessListener(aVoid -> callback.onCallback("Signup successful!"))
                                            .addOnFailureListener(e -> callback.onCallback("Error creating profile. Please try again."));
                                } else {
                                    callback.onCallback("Signup failed. Please try again.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (e.getMessage().contains("email")) {
                                    callback.onCallback("Email is already in use. Try another.");
                                } else {
                                    callback.onCallback("Signup failed. " + e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> callback.onCallback("Error checking username availability."));
    }

    /**
     * Sign in a user
     */
    public void login(String username, String password, FirebaseCallback<String> callback) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("usernameLower", username.toLowerCase())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onCallback("No account found with this username.");
                        return;
                    }

                    String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");

                    if (email == null) {
                        callback.onCallback("Error retrieving account email.");
                        return;
                    }

                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> callback.onCallback("Login successful!"))
                            .addOnFailureListener(e -> {
                                if (e.getMessage().contains("password")) {
                                    callback.onCallback("Incorrect password. Try again.");
                                } else {
                                    callback.onCallback("Login failed. Incorrect Password");
                                }
                            });
                })
                .addOnFailureListener(e -> callback.onCallback("Error checking account. Try again later."));
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
     * Get mood event from known id
     */
    public void getMoodEventFromID (String id, FirebaseCallback<MoodEvent> callback) {
        DocumentReference docRef = db.collection(MOODS_COLLECTION).document(id);
        AtomicReference<MoodEvent> moodEvent = new AtomicReference<>(new MoodEvent());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                moodEvent.set(documentSnapshot.toObject(MoodEvent.class));
                callback.onCallback(moodEvent.get());
            } else {
                Log.d("Firestore", "No document found with the given id.");
                callback.onCallback(moodEvent.get());
            }

        }).addOnFailureListener(e -> {
            Log.w("Firestore", "Error getting document", e);
            callback.onCallback(moodEvent.get());
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
        DocumentReference followingRef = db.collection(FOLLOWING_COLLECTION).document(followerId);

        // Use arrayUnion to atomically add to the list
        followingRef.update("following", FieldValue.arrayUnion(followedId))
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    // Handle case where document doesn't exist yet
                    if (e instanceof FirebaseFirestoreException &&
                            ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND) {

                        // Create new document with initial list
                        Map<String, Object> newFollowing = new HashMap<>();
                        newFollowing.put("following", Collections.singletonList(followedId));

                        followingRef.set(newFollowing)
                                .addOnSuccessListener(aVoid2 -> callback.onCallback(true))
                                .addOnFailureListener(e2 -> {
                                    Log.e("FirebaseDB", "Error creating following document", e2);
                                    callback.onCallback(false);
                                });
                    } else {
                        Log.e("FirebaseDB", "Error updating following list", e);
                        callback.onCallback(false);
                    }
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

        db.collection(FOLLOWING_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");

                    if (following == null || following.isEmpty()) {
                        callback.onCallback(new ArrayList<>());
                        return;
                    }

                    Query query = db.collection(MOODS_COLLECTION)
                            .whereIn("userID" , following);
                    if (startDate != null) {
                        query = query.whereGreaterThan("timestamp", startDate);
                    }
                    query.get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<MoodEvent> moodEvents = new ArrayList<>();
                                String k = "g";

                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    k = "k";
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

    // Add to FirebaseDB class
    public void getSentFollowRequests(String userId, FirebaseCallback<List<Map<String, Object>>> callback) {
        db.collection(FOLLOW_REQUESTS_COLLECTION)
                .whereEqualTo("fromUserId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> requests = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        data.put("requestId", doc.getId());
                        requests.add(data);
                    }
                    callback.onCallback(requests);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error getting sent requests", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

    public void searchUsersByUsername(String query, FirebaseCallback<List<Map<String, Object>>> callback) {
        String queryLower = query.toLowerCase();
        db.collection(USERS_COLLECTION)
                .whereGreaterThanOrEqualTo("usernameLower", queryLower)
                .whereLessThanOrEqualTo("usernameLower", queryLower + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> users = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> userData = doc.getData();
                        userData.put("id", doc.getId());
                        users.add(userData);
                    }
                    callback.onCallback(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error searching users", e);
                    callback.onCallback(new ArrayList<>());
                });
    }

}
