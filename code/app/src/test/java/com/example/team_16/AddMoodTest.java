package com.example.team_16;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.models.PersonalMoodHistory;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AddMoodTest {

    @Mock
    private FirebaseDB mockFirebaseDB;
    
    @Mock
    private PersonalMoodHistory mockPersonalMoodHistory;
    
    private UserProfile userProfile;
    private EmotionalState happinessState;
    private EmotionalState angerState;
    
    @Before
    public void setUp() {
        // Initialize emotional states
        happinessState = mock(EmotionalState.class);
        when(happinessState.getName()).thenReturn("Happiness");
        
        angerState = mock(EmotionalState.class);
        when(angerState.getName()).thenReturn("Anger");
        
        // Setup EmotionalStateRegistry mock
        EmotionalStateRegistry.initialize();
        
        // Create a user profile with mocked dependencies
        userProfile = new UserProfile(
            mockFirebaseDB, 
            "12345", 
            "testUser", 
            "Test User", 
            "test@example.com"
        );
        
        // Mock the personal mood history
        when(userProfile.getPersonalMoodHistory()).thenReturn(mockPersonalMoodHistory);
    }
    
    @Test
    public void testAddMoodEvent() {
        // Create a mood event
        MoodEvent moodEvent = new MoodEvent("12345", happinessState);
        
        // Set up the mock to capture the mood event added
        doAnswer(invocation -> {
            MoodEvent event = invocation.getArgument(0);
            assertEquals(happinessState, event.getEmotionalState());
            assertEquals("12345", event.getUserID());
            return null;
        }).when(mockPersonalMoodHistory).addEvent(any(MoodEvent.class), any());
        
        // Add the mood event
        userProfile.addMoodEvent(moodEvent);
        
        // Verify the mood event was added to the personal mood history
        verify(mockPersonalMoodHistory).addEvent(eq(moodEvent), any());
    }
    
    @Test
    public void testDeleteMoodEvent() {
        // Set up the mock to verify deletion
        doAnswer(invocation -> {
            String eventId = invocation.getArgument(0);
            assertEquals("test-event-id", eventId);
            return null;
        }).when(mockPersonalMoodHistory).deleteEvent(anyString(), any());
        
        // Delete a mood event
        userProfile.deleteMoodEvent("test-event-id");
        
        // Verify the mood event was deleted from the personal mood history
        verify(mockPersonalMoodHistory).deleteEvent(eq("test-event-id"), any());
    }
    
    @Test
    public void testEditMoodEvent() {
        // Create a mood event with updates
        MoodEvent updatedMoodEvent = new MoodEvent("12345", angerState);
        updatedMoodEvent.setTrigger("Feeling frustrated");
        
        // Set up the mock to verify editing
        doAnswer(invocation -> {
            String eventId = invocation.getArgument(0);
            MoodEvent event = invocation.getArgument(1);
            assertEquals("test-event-id", eventId);
            assertEquals(angerState, event.getEmotionalState());
            assertEquals("Feeling frustrated", event.getTrigger());
            return null;
        }).when(mockPersonalMoodHistory).editEvent(anyString(), any(MoodEvent.class), any());
        
        // Edit the mood event
        userProfile.editMoodEvent("test-event-id", updatedMoodEvent);
        
        // Verify the mood event was edited in the personal mood history
        verify(mockPersonalMoodHistory).editEvent(eq("test-event-id"), eq(updatedMoodEvent), any());
    }
}
