package com.example.team_16;

import com.example.team_16.models.EmotionalState;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmotionalStateTest {
    
    @Test
    public void testConstructor() {
        EmotionalState state = new EmotionalState("Happiness");
        assertEquals("Happiness", state.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullName() {
        new EmotionalState(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyName() {
        new EmotionalState("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBlankName() {
        new EmotionalState("   ");
    }

    @Test
    public void testDefaultConstructor() {
        EmotionalState state = new EmotionalState();
        assertNull(state.getName());
    }

    @Test
    public void testGetGradientResourceId() {
        EmotionalState happiness = new EmotionalState("Happiness");
        EmotionalState surprise = new EmotionalState("Surprise");
        EmotionalState anger = new EmotionalState("Anger");
        EmotionalState confusion = new EmotionalState("Confusion");
        EmotionalState disgust = new EmotionalState("Disgust");
        EmotionalState fear = new EmotionalState("Fear");
        EmotionalState sadness = new EmotionalState("Sadness");
        EmotionalState shame = new EmotionalState("Shame");
        EmotionalState unknown = new EmotionalState("Unknown");

        // Verify each emotion returns its specific gradient
        assertNotEquals(happiness.getGradientResourceId(), surprise.getGradientResourceId());
        assertNotEquals(anger.getGradientResourceId(), confusion.getGradientResourceId());
        assertNotEquals(disgust.getGradientResourceId(), fear.getGradientResourceId());
        assertNotEquals(sadness.getGradientResourceId(), shame.getGradientResourceId());
        
        // Unknown emotion should return default gradient
        assertEquals(unknown.getGradientResourceId(), new EmotionalState("AnotherUnknown").getGradientResourceId());
    }

    @Test
    public void testGetBottomGradientResourceId() {
        EmotionalState happiness = new EmotionalState("Happiness");
        EmotionalState surprise = new EmotionalState("Surprise");
        EmotionalState anger = new EmotionalState("Anger");
        EmotionalState confusion = new EmotionalState("Confusion");
        EmotionalState disgust = new EmotionalState("Disgust");
        EmotionalState fear = new EmotionalState("Fear");
        EmotionalState sadness = new EmotionalState("Sadness");
        EmotionalState shame = new EmotionalState("Shame");
        EmotionalState unknown = new EmotionalState("Unknown");

        // Verify each emotion returns its specific bottom gradient
        assertNotEquals(happiness.getBottomGradientResourceId(), surprise.getBottomGradientResourceId());
        assertNotEquals(anger.getBottomGradientResourceId(), confusion.getBottomGradientResourceId());
        assertNotEquals(disgust.getBottomGradientResourceId(), fear.getBottomGradientResourceId());
        assertNotEquals(sadness.getBottomGradientResourceId(), shame.getBottomGradientResourceId());
        
        // Unknown emotion should return default bottom gradient
        assertEquals(unknown.getBottomGradientResourceId(), new EmotionalState("AnotherUnknown").getBottomGradientResourceId());
    }

    @Test
    public void testGetEmoji() {
        EmotionalState happiness = new EmotionalState("Happiness");
        EmotionalState surprise = new EmotionalState("Surprise");
        EmotionalState anger = new EmotionalState("Anger");
        EmotionalState confusion = new EmotionalState("Confusion");
        EmotionalState disgust = new EmotionalState("Disgust");
        EmotionalState fear = new EmotionalState("Fear");
        EmotionalState sadness = new EmotionalState("Sadness");
        EmotionalState shame = new EmotionalState("Shame");
        EmotionalState unknown = new EmotionalState("Unknown");

        // Verify each emotion returns its specific emoji
        assertEquals("😊", happiness.getEmoji());
        assertEquals("😱", surprise.getEmoji());
        assertEquals("😡", anger.getEmoji());
        assertEquals("😵‍💫", confusion.getEmoji());
        assertEquals("🤢", disgust.getEmoji());
        assertEquals("😨", fear.getEmoji());
        assertEquals("☹️", sadness.getEmoji());
        assertEquals("😳", shame.getEmoji());
        assertEquals("", unknown.getEmoji());
    }

    @Test
    public void testEquals() {
        EmotionalState state1 = new EmotionalState("Happiness");
        EmotionalState state2 = new EmotionalState("Happiness");
        EmotionalState state3 = new EmotionalState("Sadness");

        // Test equality
        assertEquals(state1, state2);
        assertNotEquals(state1, state3);
        assertNotEquals(state1, null);
        assertNotEquals(state1, "Happiness");
    }

    @Test
    public void testHashCode() {
        EmotionalState state1 = new EmotionalState("Happiness");
        EmotionalState state2 = new EmotionalState("Happiness");
        EmotionalState state3 = new EmotionalState("Sadness");

        // Test hash codes
        assertEquals(state1.hashCode(), state2.hashCode());
        assertNotEquals(state1.hashCode(), state3.hashCode());
    }

    @Test
    public void testToString() {
        EmotionalState state = new EmotionalState("Happiness");
        assertEquals("EmotionalState{name='Happiness'}", state.toString());
    }
} 