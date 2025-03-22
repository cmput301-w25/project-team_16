package com.example.team_16.models;

/**
 * Model class representing a comment on a mood event
 * TODO: This is a placeholder class that will be fully implemented when the comments feature is developed
 */
public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String text;
    private long timestamp;

    /**
     * Default constructor for Firebase
     */
    public Comment() {
        // Required empty constructor for Firebase
    }

    /**
     * Constructor for creating a new comment
     */
    public Comment(String userId, String userName, String text, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters and Setters

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

// TODO: Add additional fields and methods as needed when implementing the comments feature
}