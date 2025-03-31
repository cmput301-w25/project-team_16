package com.example.team_16;

import static org.junit.Assert.*;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class EmotionalStateRegistryTest {

    @Before
    public void setUp() {
        // Reset the registry before each test to ensure a clean state
        EmotionalStateRegistry.reset();
    }

    @After
    public void tearDown() {
        // Reset the registry after each test to clean up
        EmotionalStateRegistry.reset();
    }

    @Test
    public void testBaseEmotions() {
        // Test that all base emotions are registered by default
        Set<String> emotions = EmotionalStateRegistry.getAllNames();
        assertTrue(emotions.contains("Anger"));
        assertTrue(emotions.contains("Confusion"));
        assertTrue(emotions.contains("Disgust"));
        assertTrue(emotions.contains("Fear"));
        assertTrue(emotions.contains("Happiness"));
        assertTrue(emotions.contains("Sadness"));
        assertTrue(emotions.contains("Shame"));
        assertTrue(emotions.contains("Surprise"));
        assertEquals(8, emotions.size());
    }

    @Test
    public void testRegisterNewEmotion() {
        // Test registering a new emotional state
        EmotionalState newEmotion = new EmotionalState("Excited");
        EmotionalState previous = EmotionalStateRegistry.register(newEmotion);
        
        assertNull("No previous emotion should exist", previous);
        assertTrue("New emotion should be registered", EmotionalStateRegistry.exists("Excited"));
        assertEquals("Should retrieve the same emotion", newEmotion, EmotionalStateRegistry.getByName("Excited"));
    }

    @Test
    public void testRegisterExistingEmotion() {
        // Test replacing an existing emotional state
        EmotionalState original = new EmotionalState("Happiness");
        EmotionalState replacement = new EmotionalState("Happiness");
        
        EmotionalState previous = EmotionalStateRegistry.register(replacement);
        assertNotNull("Previous emotion should exist", previous);
        assertEquals("Previous emotion should match original", original.getName(), previous.getName());
    }

    @Test
    public void testGetByName() {
        // Test retrieving emotions by name
        EmotionalState happiness = EmotionalStateRegistry.getByName("Happiness");
        assertNotNull("Should find Happiness emotion", happiness);
        assertEquals("Happiness", happiness.getName());

        EmotionalState nonexistent = EmotionalStateRegistry.getByName("NonexistentEmotion");
        assertNull("Should return null for nonexistent emotion", nonexistent);
    }

    @Test
    public void testGetAll() {
        // Test getting all registered emotions
        Map<String, EmotionalState> allEmotions = EmotionalStateRegistry.getAll();
        assertEquals("Should have all base emotions", 8, allEmotions.size());
        assertTrue(allEmotions.containsKey("Happiness"));
        assertTrue(allEmotions.containsKey("Sadness"));
        // Verify the map is unmodifiable
        try {
            allEmotions.put("NewEmotion", new EmotionalState("NewEmotion"));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testExists() {
        // Test checking existence of emotions
        assertTrue(EmotionalStateRegistry.exists("Happiness"));
        assertFalse(EmotionalStateRegistry.exists("NonexistentEmotion"));
    }

    @Test
    public void testRemove() {
        // Test removing an emotional state
        EmotionalState removed = EmotionalStateRegistry.remove("Happiness");
        assertNotNull("Should return removed emotion", removed);
        assertEquals("Happiness", removed.getName());
        assertFalse("Emotion should no longer exist", EmotionalStateRegistry.exists("Happiness"));
        
        // Test removing non-existent emotion
        EmotionalState nonexistent = EmotionalStateRegistry.remove("NonexistentEmotion");
        assertNull("Should return null for nonexistent emotion", nonexistent);
    }

    @Test
    public void testReset() {
        // Add a custom emotion
        EmotionalStateRegistry.register(new EmotionalState("CustomEmotion"));
        assertTrue(EmotionalStateRegistry.exists("CustomEmotion"));
        
        // Reset the registry
        EmotionalStateRegistry.reset();
        
        // Verify custom emotion is gone and base emotions are restored
        assertFalse("Custom emotion should be removed", EmotionalStateRegistry.exists("CustomEmotion"));
        assertTrue("Base emotions should be restored", EmotionalStateRegistry.exists("Happiness"));
        assertEquals("Should have only base emotions", 8, EmotionalStateRegistry.getAllNames().size());
    }
} 