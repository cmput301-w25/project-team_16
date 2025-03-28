package com.example.team_16.models;

public class Comment {
    private String id;         // Firestore doc ID
    private String userId;     // UID of the user who made the comment
    private String userName;   // Display name or username
    private String text;       // The comment text
    private long timestamp;    // Time of creation (System.currentTimeMillis())

    // Required empty constructor for Firebase
    public Comment() {}

    // Convenience constructor
    public Comment(String userId, String userName, String text) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = System.currentTimeMillis(); // or set via addCommentToMoodEvent
    }

    // Getters / Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
