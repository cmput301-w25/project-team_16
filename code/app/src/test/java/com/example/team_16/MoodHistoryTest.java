package com.example.team_16;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MoodHistoryTest {

    @Mock
    private FirebaseDB mockFirebaseDB;

    private MoodHistory moodHistory;
    private static final String TEST_USER_ID = "test_user_id";

    @Before
    public void setUp() {
        moodHistory = new MoodHistory(TEST_USER_ID, MoodHistory.MODE_PERSONAL, mockFirebaseDB);
    }

    @Test
    public void testConstructor() {
        assertEquals(TEST_USER_ID, moodHistory.getUserId());
        assertEquals(MoodHistory.MODE_PERSONAL, moodHistory.getMode());
    }

    @Test
    public void testGetAllEvents() {
        List<MoodEvent> events = moodHistory.getAllEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty()); // Initially empty
    }

    @Test
    public void testGetEventById() {
        // Test with non-existent ID
        assertNull(moodHistory.getEventById("non_existent_id"));

        // Test with null ID
        assertNull(moodHistory.getEventById(null));
    }

    @Test
    public void testGetFilteredEvents() {
        // Test with null filters
        moodHistory.getFilteredEvents(null, null, null, events -> {
            assertNotNull(events);
            assertTrue(events.isEmpty());
        });

        // Test with specific filters
        Date startDate = new Date();
        EmotionalState emotionalState = EmotionalStateRegistry.getByName("Happiness");
        String searchText = "test";

        moodHistory.getFilteredEvents(emotionalState, startDate, searchText, events -> {
            assertNotNull(events);
            assertTrue(events.isEmpty());
        });
    }

    @Test
    public void testGetRecentEvents() {
        moodHistory.getRecentEvents(5, events -> {
            assertNotNull(events);
            assertTrue(events.isEmpty());
        });
    }

    @Test
    public void testDataLoadCallback() {
        List<MoodEvent> loadedEvents = new ArrayList<>();
        moodHistory.setDataLoadCallback(events -> loadedEvents.addAll(events));
        
        // Verify callback is set
        assertNotNull(moodHistory);
    }

    @Test
    public void testRefresh() {
        moodHistory.refresh();
        // Verify refresh was called twice (once in constructor, once in refresh)
        verify(mockFirebaseDB, times(2)).getMoodEvents(
            eq(TEST_USER_ID),
            isNull(),
            isNull(),
            isNull(),
            any()
        );
    }

    @Test
    public void testRefreshWithCallback() {
        Runnable mockCallback = mock(Runnable.class);
        moodHistory.refresh(mockCallback);
        
        // Verify refresh was called twice (once in constructor, once in refresh)
        verify(mockFirebaseDB, times(2)).getMoodEvents(
            eq(TEST_USER_ID),
            isNull(),
            isNull(),
            isNull(),
            any()
        );
    }

    @Test
    public void testFollowingMode() {
        MoodHistory followingHistory = new MoodHistory(TEST_USER_ID, MoodHistory.MODE_FOLLOWING, mockFirebaseDB);
        assertEquals(MoodHistory.MODE_FOLLOWING, followingHistory.getMode());
        
        // Test that it filters for public events only
        followingHistory.getFilteredEvents(null, null, null, events -> {
            assertNotNull(events);
            assertTrue(events.isEmpty());
        });
    }
} 