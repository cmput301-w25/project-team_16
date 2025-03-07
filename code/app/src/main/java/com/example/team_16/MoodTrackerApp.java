package com.example.team_16;

import android.app.Application;
import com.example.team_16.models.UserProfile;

public class MoodTrackerApp extends Application {
    private UserProfile currentUserProfile;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setCurrentUserProfile(UserProfile userProfile) {
        this.currentUserProfile = userProfile;
    }

    public UserProfile getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void clearCurrentUserProfile() {
        currentUserProfile = null;
    }
}