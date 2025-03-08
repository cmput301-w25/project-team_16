package com.example.team_16.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String username;
    // Lists to track outgoing follow requests and already followed users
    private List<String> pendingFollow;
    private List<String> userFollowing;

    // Required empty constructor for Firebase deserialization
    public User() {}

    public User(String id, String username) {
        this.id = id;
        this.username = username;
        this.pendingFollow = new ArrayList<>();
        this.userFollowing = new ArrayList<>();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getPendingFollow() {
        if (pendingFollow == null) {
            pendingFollow = new ArrayList<>();
        }
        return pendingFollow;
    }
    public void setPendingFollow(List<String> pendingFollow) { this.pendingFollow = pendingFollow; }

    public List<String> getUserFollowing() {
        if (userFollowing == null) {
            userFollowing = new ArrayList<>();
        }
        return userFollowing;
    }
    public void setUserFollowing(List<String> userFollowing) { this.userFollowing = userFollowing; }
}
