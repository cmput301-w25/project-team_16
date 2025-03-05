package com.example.team_16;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class EmotionalStateTest {
    @Test
    public void testBaseEmotionsExist() {
        // Test that all required emotions exist
        String[] requiredEmotions = {
                "Anger", "Confusion", "Disgust", "Fear",
                "Happiness", "Sadness", "Shame", "Surprise"
        };

        for (String emotion : requiredEmotions) {
            EmotionalState state = EmotionalStateRegistry.getByName(emotion);
            assertNotNull("Emotion '" + emotion + "' should exist", state);
            assertEquals("Emotion name should match", emotion, state.getName());
        }
    }

    @Test
    public void testCustomEmotionRegistration() {
        // Register a custom emotion
        EmotionalState custom = new EmotionalState("Excitement");
        EmotionalStateRegistry.register(custom);

        // Check it was registered properly
        EmotionalState retrieved = EmotionalStateRegistry.getByName("Excitement");
        assertNotNull("Custom emotion should be retrievable", retrieved);
        assertEquals("Retrieved emotion should be the same as registered", custom, retrieved);
    }
}
