package com.example.team_16;

import android.app.Application;
import com.example.team_16.models.UserProfile;

/**
 * Application class for the Mood Tracker app
 * Manages global application state including the current user profile
 */
public class MoodTrackerApp extends Application {
    private UserProfile currentUserProfile;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Set the current user profile
     *
     * @param userProfile The user profile to set as current
     */
    public void setCurrentUserProfile(UserProfile userProfile) {
        this.currentUserProfile = userProfile;
    }

    /**
     * Get the current user profile
     *
     * @return The current user profile or null if no user is logged in
     */
    public UserProfile getCurrentUserProfile() {
        return currentUserProfile;
    }

    /**
     * Clear the current user profile (for logout)
     */
    public void clearCurrentUserProfile() {
        currentUserProfile = null;
    }
}