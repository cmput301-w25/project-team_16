package com.example.team_16.database;

import com.example.team_16.models.Comment;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

/**
 * Comprehensive Firebase Database Management for Mood Tracking App
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
    private static final String FOLLOW_REQUESTS_COLLECTION = "follow_requests";
    private static final String FOLLOWING_COLLECTION = "following";
    private static final String COMMENTS_SUBCOLLECTION = "comments";

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

                                    userData.put("locationPermission", false);
                                    userData.put("photoPermission", false);
                                    userData.put("cameraPermission", false);

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
                .whereEqualTo("userID", userId)
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

    // Methods related to image uploading

    private byte[] compressImage(Context context, Uri imageUri, int maxSize) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = 100;
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            do {
                outputStream.reset();
                bitmap.compress(format, quality, outputStream);
                quality -= 5;
            } while (outputStream.toByteArray().length > maxSize && quality > 10);

            if (outputStream.toByteArray().length > maxSize) {
                return null;
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e("FirebaseDB", "Error compressing image", e);
            return null;
        }
    }

    private void uploadImageToFirebase(Context context, Uri imageUri, FirebaseCallback<String> callback) {
        if (imageUri == null) {
            callback.onCallback(null);
            return;
        }

        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Compressing & Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Compress image to meet 64KB requirement
            byte[] compressedImage = compressImage(context, imageUri, 64 * 1024);

            if (compressedImage == null) {
                progressDialog.dismiss(); // Hide loading indicator
                Toast.makeText(context, "Image too large. Try a smaller image!", Toast.LENGTH_SHORT).show();
                callback.onCallback(null);
                return;
            }

            // Create a unique file name
            String fileName = "mood_images/" + System.currentTimeMillis() + ".jpg";
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child(fileName);

            // Upload compressed image
            storageRef.putBytes(compressedImage)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                progressDialog.dismiss(); // Hide loading indicator
                                callback.onCallback(uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e("FirebaseDB", "Failed to get image URL", e);
                                callback.onCallback(null);
                            }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.e("FirebaseDB", "Image upload failed", e);
                        callback.onCallback(null);
                    });

        } catch (Exception e) {
            progressDialog.dismiss(); // Hide loading indicator
            Log.e("FirebaseDB", "Compression error", e);
            callback.onCallback(null);
        }
    }

    public void addMoodEventWithImage(Context context, MoodEvent moodEvent, Uri imageUri, FirebaseCallback<Boolean> callback) {
        uploadImageToFirebase(context, imageUri, imageUrl -> {
            if (imageUrl != null) {
                moodEvent.setPhotoUrl(imageUrl);
            }
            addMoodEvent(moodEvent, callback);
        });
    }

    public void updateMoodEventWithImage(Context context, String eventId, MoodEvent updates, Uri imageUri, FirebaseCallback<Boolean> callback) {
        uploadImageToFirebase(context, imageUri, imageUrl -> {
            if (imageUrl != null) {
                updates.setPhotoUrl(imageUrl);
            }
            updateMoodEvent(eventId, updates, callback);
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
                        Map<String, Object> userData = documentSnapshot.getData();

                        // Ensure permissions default to false if missing
                        if (!userData.containsKey("locationPermission")) userData.put("locationPermission", false);
                        if (!userData.containsKey("photoPermission")) userData.put("photoPermission", false);
                        if (!userData.containsKey("cameraPermission")) userData.put("cameraPermission", false);

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
            String username,
            String profileImageUrl,
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
        if (profileImageUrl != null) {
            updates.put("profileImageUrl", profileImageUrl);
        }

        // new: Add the username fields
        // for updating the user profile info
        if (username != null && !username.trim().isEmpty()) {
            updates.put("username", username);
            updates.put("usernameLower", username.toLowerCase());  // for searching
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
     * Update a user's permission in Firestore.
     *
     * @param userId        The user's ID.
     * @param permissionKey The permission field name in Firestore.
     * @param isGranted     Whether the permission is granted or revoked.
     * @param callback      Callback to notify success or failure.
     */
    public void updateUserPermission(String userId, String permissionKey, boolean isGranted, FirebaseCallback<Boolean> callback) {
        DocumentReference userDoc = db.collection(USERS_COLLECTION).document(userId);

        if (isGranted) {
            // Store `true` in Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put(permissionKey, true);

            userDoc.set(updates, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> callback.onCallback(true))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseDB", "Error updating permission", e);
                        callback.onCallback(false);
                    });

        } else {
            // Remove the permission field from Firestore (effectively setting it to `false` by default)
            userDoc.update(permissionKey, FieldValue.delete())
                    .addOnSuccessListener(aVoid -> callback.onCallback(true))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseDB", "Error removing permission", e);
                        callback.onCallback(false);
                    });
        }
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

    public void addCommentToMoodEvent(String moodEventId, Comment comment, FirebaseCallback<Comment> callback) {
        DocumentReference newCommentRef = db.collection(MOODS_COLLECTION)
                .document(moodEventId)
                .collection(COMMENTS_SUBCOLLECTION)
                .document();

        // Ensure all fields are set before saving
        comment.setId(newCommentRef.getId());
        comment.setTimestamp(System.currentTimeMillis());

        // Create a map with ALL fields including profileImageUrl
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("id", comment.getId());
        commentData.put("userId", comment.getUserId());
        commentData.put("userName", comment.getUserName());
        commentData.put("text", comment.getText());
        commentData.put("timestamp", comment.getTimestamp());
        commentData.put("profileImageUrl", comment.getProfileImageUrl());  // Add this

        newCommentRef.set(commentData)  // Changed from set(comment) to set(commentData)
                .addOnSuccessListener(aVoid -> callback.onCallback(comment))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error adding comment", e);
                    callback.onCallback(null);
                });
    }

    public void fetchCommentsForMoodEvent(String moodEventId, FirebaseCallback<List<Comment>> callback) {
        db.collection(MOODS_COLLECTION)
                .document(moodEventId)
                .collection(COMMENTS_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        // Manually map all fields including profileImageUrl
                        Comment comment = new Comment(
                                doc.getString("userId"),
                                doc.getString("userName"),
                                doc.getString("text")
                        );
                        comment.setId(doc.getString("id"));
                        comment.setTimestamp(doc.getLong("timestamp"));
                        comment.setProfileImageUrl(doc.getString("profileImageUrl"));  // Add this

                        comments.add(comment);
                    }
                    callback.onCallback(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Error fetching comments", e);
                    callback.onCallback(new ArrayList<>());
                });
    }
    public void uploadProfileImage(Uri imageUri, String userId, FirebaseCallback<String> callback) {
        StorageReference storageRef = storage.getReference()
                .child("profileImages/" + userId + "_" + System.currentTimeMillis());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Log.d("FirebaseDB", "Image uploaded. Download URL: " + uri);
                            callback.onCallback(uri.toString());
                        })
                )
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Image upload failed", e);
                    callback.onCallback(null);
                });
    }

    public StorageReference getReference(String path) {
        return storage.getReference().child(path);
    }


}
