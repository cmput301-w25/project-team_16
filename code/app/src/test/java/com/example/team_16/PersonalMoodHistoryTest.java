package com.example.team_16;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.PersonalMoodHistory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PersonalMoodHistoryTest {

    @Mock
    private FirebaseDB mockFirebaseDB;

    private PersonalMoodHistory personalMoodHistory;
    private static final String TEST_USER_ID = "test_user_id";

    @Before
    public void setUp() {
        personalMoodHistory = new PersonalMoodHistory(TEST_USER_ID, mockFirebaseDB);
    }

    @Test
    public void testAddEvent() {
        EmotionalState emotionalState = EmotionalStateRegistry.getByName("Happiness");
        MoodEvent event = new MoodEvent(TEST_USER_ID, emotionalState);

        // Add the event
        personalMoodHistory.addEvent(event, success -> {
            assertTrue(success);
            assertNotNull(event.getId());
            assertEquals(TEST_USER_ID, event.getUserID());
        });
    }

    @Test
    public void testEditEvent() {
        EmotionalState emotionalState = EmotionalStateRegistry.getByName("Happiness");
        MoodEvent originalEvent = new MoodEvent(TEST_USER_ID, emotionalState);
        originalEvent.setId("test_event_id");

        // Add the original event using the public method
        personalMoodHistory.addEvent(originalEvent);

        MoodEvent updatedEvent = originalEvent.copy();
        updatedEvent.setTrigger("Updated trigger");

        // Edit the event
        personalMoodHistory.editEvent(originalEvent.getId(), updatedEvent, success -> {
            assertTrue(success);
            assertEquals(TEST_USER_ID, updatedEvent.getUserID());
            assertEquals("Updated trigger", updatedEvent.getTrigger());
        });
    }

    @Test
    public void testDeleteEvent() {
        String eventId = "test_event_id";
        EmotionalState emotionalState = EmotionalStateRegistry.getByName("Happiness");
        MoodEvent event = new MoodEvent(TEST_USER_ID, emotionalState);
        event.setId(eventId);

        // Add the event using the public method
        personalMoodHistory.addEvent(event);

        // Delete the event
        personalMoodHistory.deleteEvent(eventId, success -> assertTrue(success));
    }

    @Test
    public void testSyncPendingChanges() {
        // Mock online status
        when(mockFirebaseDB.isOnline()).thenReturn(true);

        // Add a pending operation
        EmotionalState emotionalState = EmotionalStateRegistry.getByName("Happiness");
        MoodEvent event = new MoodEvent(TEST_USER_ID, emotionalState);
        event.setId("test_event_id");
        personalMoodHistory.addEvent(event);

        personalMoodHistory.syncPendingChanges(success -> {
            assertTrue(success);
            assertFalse(personalMoodHistory.hasPendingChanges());
        });
    }

    @Test
    public void testSyncPendingChangesOffline() {
        // Mock offline status
        when(mockFirebaseDB.isOnline()).thenReturn(false);

        // Add a pending operation
        EmotionalState emotionalState = EmotionalStateRegistry.getByName("Happiness");
        MoodEvent event = new MoodEvent(TEST_USER_ID, emotionalState);
        event.setId("test_event_id");
        personalMoodHistory.addEvent(event);

        personalMoodHistory.syncPendingChanges(success -> {
            assertFalse(success);
            // Pending changes should remain if offline
            assertTrue(personalMoodHistory.hasPendingChanges());
        });
    }

    @Test
    public void testHasPendingChanges() {
        // Initially should have no pending changes
        assertFalse(personalMoodHistory.hasPendingChanges());
    }

    @Test
    public void testGetMonthlyEvents() {
        int year = 2024;
        int month = 3;
        Object callback = new Object();

        // Test the method call
        personalMoodHistory.getMonthlyEvents(year, month, callback);
    }
} 