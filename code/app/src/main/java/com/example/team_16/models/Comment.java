/**
 * Represents a comment made by a user on a mood event.
 * This class stores information about who made the comment,
 * when it was made, and the comment text.
 *
 * Key Features:
 * - Stores user information (ID, name, profile image)
 * - Includes timestamp for when the comment was made
 * - Supports comment text content
 * - Handles profile image URL for display
 *
 * Usage:
 * Comments are typically created when a user comments on
 * another user's mood event in the MoodDetails fragment.
 *
 * Example:
 * <pre>
 * Comment comment = new Comment("user123", "John Doe", "Great to see you're happy!");
 * comment.setProfileImageUrl("https://example.com/profile.jpg");
 * </pre>
 */

package com.example.team_16.models;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String text;
    private long timestamp;
    private String profileImageUrl;

    public Comment() {}

    /**
     * Constructs a new Comment with the specified user and text.
     *
     * @param userId The ID of the user making the comment
     * @param userName The name of the user making the comment
     * @param text The comment text
     */
    public Comment(String userId, String userName, String text) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the ID of the user who made the comment.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Sets the ID of the user who made the comment.
     *
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the name of the user who made the comment.
     *
     * @return The user name
     */
    public String getUserName() {
        return userName;
    }
    /**
     * Sets the name of the user who made the comment.
     *
     * @param userName The user name to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the text content of the comment.
     *
     * @return The comment text
     */
    public String getText() {
        return text;
    }
    /**
     * Sets the text content of the comment.
     *
     * @param text The comment text to set
     */
    public void setText(String text) {
        this.text = text;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    /**
     * Sets the URL of the commenter's profile image.
     *
     * @param profileImageUrl The profile image URL to set
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
