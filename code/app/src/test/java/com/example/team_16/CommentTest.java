package com.example.team_16;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.team_16.models.Comment;

public class CommentTest {
    
    @Test
    public void testDefaultConstructor() {
        Comment comment = new Comment();
        assertNull(comment.getId());
        assertNull(comment.getUserId());
        assertNull(comment.getUserName());
        assertNull(comment.getText());
        assertEquals(0, comment.getTimestamp());
        assertNull(comment.getProfileImageUrl());
    }

    @Test
    public void testConstructorWithRequiredFields() {
        String userId = "user123";
        String userName = "John Doe";
        String text = "Great mood!";
        
        Comment comment = new Comment(userId, userName, text);
        
        assertNull(comment.getId());
        assertEquals(userId, comment.getUserId());
        assertEquals(userName, comment.getUserName());
        assertEquals(text, comment.getText());
        assertTrue(comment.getTimestamp() > 0); // Should be current timestamp
        assertNull(comment.getProfileImageUrl());
    }

    @Test
    public void testSettersAndGetters() {
        Comment comment = new Comment();
        
        // Test ID
        String id = "comment123";
        comment.setId(id);
        assertEquals(id, comment.getId());
        
        // Test User ID
        String userId = "user123";
        comment.setUserId(userId);
        assertEquals(userId, comment.getUserId());
        
        // Test User Name
        String userName = "John Doe";
        comment.setUserName(userName);
        assertEquals(userName, comment.getUserName());
        
        // Test Text
        String text = "Great mood!";
        comment.setText(text);
        assertEquals(text, comment.getText());
        
        // Test Timestamp
        long timestamp = System.currentTimeMillis();
        comment.setTimestamp(timestamp);
        assertEquals(timestamp, comment.getTimestamp());
        
        // Test Profile Image URL
        String profileImageUrl = "https://example.com/profile.jpg";
        comment.setProfileImageUrl(profileImageUrl);
        assertEquals(profileImageUrl, comment.getProfileImageUrl());
    }

    @Test
    public void testTimestampInitialization() {
        Comment comment = new Comment("user123", "John Doe", "Test comment");
        long currentTime = System.currentTimeMillis();
        assertTrue(comment.getTimestamp() <= currentTime);
        assertTrue(comment.getTimestamp() > 0);
    }
} 