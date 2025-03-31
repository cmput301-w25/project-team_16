package com.example.team_16;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;
import com.google.firebase.Timestamp;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class MoodEventTest {
    
    @Test
    public void testDefaultConstructor() {
        MoodEvent event = new MoodEvent();
        assertNull(event.getId());
        assertNull(event.getTimestamp());
        assertNull(event.getTrigger());
        assertNull(event.getEmotionalState());
        assertNull(event.getUserID());
        assertNull(event.getSocialSituation());
        assertNull(event.getPhotoFilename());
        assertEquals("Public", event.getPostType());
        assertNull(event.getLatitude());
        assertNull(event.getLongitude());
        assertNull(event.getPlaceName());
        assertFalse(event.isPrivate());
        assertNull(event.getPhotoUrl());
    }

    @Test
    public void testConstructorWithRequiredFields() {
        String userId = "user123";
        EmotionalState emotionalState = new EmotionalState("Happiness");
        
        MoodEvent event = new MoodEvent(userId, emotionalState);
        
        assertNull(event.getId());
        assertNotNull(event.getTimestamp());
        assertNull(event.getTrigger());
        assertEquals(emotionalState, event.getEmotionalState());
        assertEquals(userId, event.getUserID());
        assertNull(event.getSocialSituation());
        assertNull(event.getPhotoFilename());
        assertEquals("Public", event.getPostType());
        assertNull(event.getLatitude());
        assertNull(event.getLongitude());
        assertNull(event.getPlaceName());
        assertFalse(event.isPrivate());
        assertNull(event.getPhotoUrl());
    }

    @Test
    public void testConstructorWithAllFields() {
        String userId = "user123";
        EmotionalState emotionalState = new EmotionalState("Happiness");
        String trigger = "Good news";
        String socialSituation = "With friends";
        Double latitude = 53.5461;
        Double longitude = -113.4937;
        String placeName = "Edmonton";
        
        MoodEvent event = new MoodEvent(userId, emotionalState, trigger, socialSituation,
                latitude, longitude, placeName);
        
        assertNull(event.getId());
        assertNotNull(event.getTimestamp());
        assertEquals(trigger, event.getTrigger());
        assertEquals(emotionalState, event.getEmotionalState());
        assertEquals(userId, event.getUserID());
        assertEquals(socialSituation, event.getSocialSituation());
        assertNull(event.getPhotoFilename());
        assertEquals("Public", event.getPostType());
        assertEquals(latitude, event.getLatitude());
        assertEquals(longitude, event.getLongitude());
        assertEquals(placeName, event.getPlaceName());
        assertFalse(event.isPrivate());
        assertNull(event.getPhotoUrl());
    }

    @Test
    public void testCompleteConstructor() {
        String id = "event123";
        Timestamp timestamp = Timestamp.now();
        EmotionalState emotionalState = new EmotionalState("Happiness");
        String trigger = "Good news";
        String userId = "user123";
        String socialSituation = "With friends";
        Double latitude = 53.5461;
        Double longitude = -113.4937;
        String placeName = "Edmonton";
        
        MoodEvent event = new MoodEvent(id, timestamp, emotionalState, trigger,
                userId, socialSituation, latitude, longitude, placeName);
        
        assertEquals(id, event.getId());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(trigger, event.getTrigger());
        assertEquals(emotionalState, event.getEmotionalState());
        assertEquals(userId, event.getUserID());
        assertEquals(socialSituation, event.getSocialSituation());
        assertNull(event.getPhotoFilename());
        assertEquals("Public", event.getPostType());
        assertEquals(latitude, event.getLatitude());
        assertEquals(longitude, event.getLongitude());
        assertEquals(placeName, event.getPlaceName());
        assertFalse(event.isPrivate());
        assertNull(event.getPhotoUrl());
    }

    @Test
    public void testSettersAndGetters() {
        MoodEvent event = new MoodEvent();
        
        // Test ID
        String id = "event123";
        event.setId(id);
        assertEquals(id, event.getId());
        
        // Test Timestamp
        Timestamp timestamp = Timestamp.now();
        event.setTimestamp(timestamp);
        assertEquals(timestamp, event.getTimestamp());
        
        // Test Trigger
        String trigger = "Good news";
        event.setTrigger(trigger);
        assertEquals(trigger, event.getTrigger());
        
        // Test EmotionalState
        EmotionalState emotionalState = new EmotionalState("Happiness");
        event.setEmotionalState(emotionalState);
        assertEquals(emotionalState, event.getEmotionalState());
        
        // Test UserID
        String userId = "user123";
        event.setUserID(userId);
        assertEquals(userId, event.getUserID());
        
        // Test SocialSituation
        String socialSituation = "With friends";
        event.setSocialSituation(socialSituation);
        assertEquals(socialSituation, event.getSocialSituation());
        
        // Test PhotoFilename
        String photoFilename = "photo123.jpg";
        event.setPhotoFilename(photoFilename);
        assertEquals(photoFilename, event.getPhotoFilename());
        
        // Test PostType
        String postType = "Private";
        event.setPostType(postType);
        assertEquals(postType, event.getPostType());
        
        // Test Location
        Double latitude = 53.5461;
        Double longitude = -113.4937;
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        assertEquals(latitude, event.getLatitude());
        assertEquals(longitude, event.getLongitude());
        
        // Test PlaceName
        String placeName = "Edmonton";
        event.setPlaceName(placeName);
        assertEquals(placeName, event.getPlaceName());
        
        // Test Privacy
        event.setPrivate(true);
        assertTrue(event.isPrivate());
        
        // Test PhotoUrl
        String photoUrl = "https://example.com/photo123.jpg";
        event.setPhotoUrl(photoUrl);
        assertEquals(photoUrl, event.getPhotoUrl());
    }

    @Test
    public void testHasLocation() {
        MoodEvent event = new MoodEvent();
        assertFalse(event.hasLocation());
        
        event.setLatitude(53.5461);
        assertFalse(event.hasLocation());
        
        event.setLongitude(-113.4937);
        assertTrue(event.hasLocation());
        
        event.setLatitude(null);
        assertFalse(event.hasLocation());
        
        event.setLatitude(53.5461);
        event.setLongitude(null);
        assertFalse(event.hasLocation());
    }

    @Test
    public void testIsValid() {
        MoodEvent event = new MoodEvent();
        assertFalse(event.isValid());
        
        event.setEmotionalState(new EmotionalState("Happiness"));
        assertTrue(event.isValid());
        
        event.setEmotionalState(null);
        assertFalse(event.isValid());
    }
} 