package com.example.team_16;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;

@RunWith(JUnit4.class)
public class AddMoodTest {

    private EmotionalState happinessState;
    private EmotionalState angerState;
    
    @Before
    public void setUp() {
        // Create simple emotional states for testing
        happinessState = new EmotionalState("Happiness", "Happy feeling", "#FFFF00");
        angerState = new EmotionalState("Anger", "Angry feeling", "#FF0000");
    }
    
    @Test
    public void testMoodEventCreation() {
        // Create a new mood event
        MoodEvent moodEvent = new MoodEvent("12345", happinessState);
        
        // Verify the mood event has correct properties
        assertEquals("12345", moodEvent.getUserID());
        assertEquals(happinessState, moodEvent.getEmotionalState());
        assertNotNull(moodEvent.getTimestamp());
    }
    
    @Test
    public void testMoodEventWithTrigger() {
        // Create a mood event with a trigger
        MoodEvent moodEvent = new MoodEvent("12345", angerState);
        moodEvent.setTrigger("Traffic jam");
        
        // Verify the mood event properties
        assertEquals("12345", moodEvent.getUserID());
        assertEquals(angerState, moodEvent.getEmotionalState());
        assertEquals("Traffic jam", moodEvent.getTrigger());
    }
    
    @Test
    public void testMoodEventWithSocialSetting() {
        // Create a mood event with a social setting
        MoodEvent moodEvent = new MoodEvent("12345", happinessState);
        moodEvent.setSocialSetting("Crowd");
        
        // Verify the mood event properties
        assertEquals("12345", moodEvent.getUserID());
        assertEquals(happinessState, moodEvent.getEmotionalState());
        assertEquals("Crowd", moodEvent.getSocialSetting());
    }
    
    @Test
    public void testMoodEventWithLocation() {
        // Create a mood event with location
        MoodEvent moodEvent = new MoodEvent("12345", happinessState);
        double latitude = 53.5461;
        double longitude = -113.4938;
        moodEvent.setLatitude(latitude);
        moodEvent.setLongitude(longitude);
        
        // Verify the mood event properties
        assertEquals("12345", moodEvent.getUserID());
        assertEquals(happinessState, moodEvent.getEmotionalState());
        assertEquals(latitude, moodEvent.getLatitude(), 0.0001);
        assertEquals(longitude, moodEvent.getLongitude(), 0.0001);
    }
}
