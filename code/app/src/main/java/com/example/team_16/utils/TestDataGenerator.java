package com.example.team_16.utils;

import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TestDataGenerator {
    // Triggers categorized by emotion with weighted probabilities
    private static final Map<String, String[]> EMOTION_TRIGGERS = new HashMap<>() {{
        put("Happiness", new String[]{
                "Had a productive day at work", "Caught up with old friends over coffee",
                "Finished all my assignments early", "Got positive feedback on my project",
                "Great workout at the gym", "Enjoyed a sunny weekend outdoors",
                "Cooked a delicious meal", "Quality time with family",
                "Achieved my daily goals", "Found a solution to a problem",
                "Got a good grade", "Relaxing evening with music",
                "Made progress on my thesis", "Had a great study session"
        });
        put("Sadness", new String[]{
                "Missing home", "Stressed about upcoming deadlines",
                "Feeling overwhelmed with coursework", "Bad grade on an assignment",
                "Homesick today", "Tired from lack of sleep",
                "Rainy day blues", "Missing my family",
                "Failed to meet a deadline", "Feeling burnt out from studying"
        });
        put("Anger", new String[]{
                "Frustrated with group project", "Bus was late again",
                "Lost my work due to technical issues", "Noisy neighbors while studying",
                "Someone took my reserved study spot", "Printer not working before deadline",
                "WiFi issues during online class", "Missed the bus",
                "Lab equipment malfunction", "Lost my student ID"
        });
        put("Surprise", new String[]{
                "Unexpected high grade", "Random catch-up with classmate",
                "Professor extended deadline", "Found my lost notes",
                "Surprise visit from friend", "Class cancelled - extra study time",
                "Got picked for research position", "Free food at campus event"
        });
        put("Disgust", new String[]{
                "Bad cafeteria food", "Messy shared kitchen",
                "Dirty study area", "Found old food in backpack",
                "Unclean lab equipment", "Moldy coffee in my mug"
        });
        put("Fear", new String[]{
                "Upcoming final exam", "Big presentation tomorrow",
                "Group project deadline approaching", "Late for important meeting",
                "Thesis defense preparation", "Important lab experiment",
                "Job interview preparation", "Waiting for grade results"
        });
        put("Confusion", new String[]{
                "Difficult lecture material", "Complex assignment instructions",
                "Unclear project requirements", "New software in lab",
                "Conflicting assignment deadlines", "Mixed messages from group members",
                "Complicated research paper", "New lab procedures"
        });
        put("Shame", new String[]{
                "Slept through morning class", "Forgot assignment deadline",
                "Said wrong thing in presentation", "Mixed up meeting times",
                "Sent wrong email to professor", "Lost borrowed notes",
                "Came unprepared to group meeting", "Failed to contribute to group work"
        });
    }};


    private static final String[] EMOTIONAL_STATE_NAMES = {
        "Happiness", "Sadness", "Anger", "Surprise",
        "Disgust", "Fear", "Confusion", "Shame"
    };

    // Emotion weights (higher number = more likely)
    private static final Map<String, Integer> EMOTION_WEIGHTS = new HashMap<String, Integer>() {{
        put("Happiness", 35);  // Most common
        put("Sadness", 15);
        put("Anger", 10);
        put("Surprise", 10);
        put("Disgust", 5);    // Less common
        put("Fear", 10);
        put("Confusion", 10);
        put("Shame", 5);      // Less common
    }};

    public static void generateFebruaryMoodEvents(UserProfile userProfile, Runnable onComplete) {
        List<MoodEvent> events = new ArrayList<>();
        Random random = new Random();
        Calendar calendar = Calendar.getInstance();
        
        // Set to February 2025
        calendar.set(2025, Calendar.FEBRUARY, 1);
        int daysInFebruary = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Generate events for each day
        for (int day = 1; day <= daysInFebruary; day++) {
            calendar.set(2025, Calendar.FEBRUARY, day);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            
            // More events on weekdays, fewer on weekends
            int maxEvents = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) ? 2 : 3;
            
            // 30% chance to skip a weekend day, 10% chance to skip a weekday
            if (random.nextFloat() < ((dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) ? 0.3 : 0.1)) {
                continue;
            }

            int eventsForDay = random.nextInt(maxEvents) + 1; // 1-2 on weekends, 1-3 on weekdays
            
            for (int i = 0; i < eventsForDay; i++) {
                // Set time based on event index
                if (eventsForDay == 1) {
                    // If only one event, random time during waking hours
                    calendar.set(Calendar.HOUR_OF_DAY, 8 + random.nextInt(14)); // 8 AM to 10 PM
                } else if (i == 0) {
                    // First event of multiple: morning (8 AM to 12 PM)
                    calendar.set(Calendar.HOUR_OF_DAY, 8 + random.nextInt(4));
                } else if (i == eventsForDay - 1) {
                    // Last event of multiple: evening (6 PM to 10 PM)
                    calendar.set(Calendar.HOUR_OF_DAY, 18 + random.nextInt(4));
                } else {
                    // Middle event: afternoon (1 PM to 5 PM)
                    calendar.set(Calendar.HOUR_OF_DAY, 13 + random.nextInt(4));
                }
                calendar.set(Calendar.MINUTE, random.nextInt(60));
                
                // Select emotion using weights
                String emotionName = selectWeightedEmotion(random);
                EmotionalState emotion = new EmotionalState(emotionName);
                
                // Select trigger appropriate for the emotion
                String[] appropriateTriggers = EMOTION_TRIGGERS.get(emotionName);
                assert appropriateTriggers != null;
                String trigger = appropriateTriggers[random.nextInt(appropriateTriggers.length)];
                
                // Select time-appropriate social situation
                String socialSituation = selectTimeSensitiveSocialSituation(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    dayOfWeek,
                    trigger
                );

                // Create a new MoodEvent with a unique ID
                String eventId = UUID.randomUUID().toString();
                Timestamp timestamp = new Timestamp(calendar.getTime());
                
                MoodEvent event = new MoodEvent(
                    eventId,
                    timestamp,
                    emotion,
                    trigger,
                    userProfile.getId(),
                    socialSituation,
                    null,  // latitude
                    null,  // longitude
                    null   // placeName
                );
                
                // Set additional fields
                event.setPostType("Public");  // Default to public posts
                event.setPhotoUrl(null);      // No photos for test data
                event.setPhotoFilename(null);
                event.setPrivate(false);
                
                events.add(event);
            }
        }

        // Add events in sequence with small delays to avoid overwhelming Firebase
        addEventsSequentially(userProfile, events, 0, onComplete);
    }

    private static String selectWeightedEmotion(Random random) {
        int totalWeight = EMOTION_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (Map.Entry<String, Integer> entry : EMOTION_WEIGHTS.entrySet()) {
            currentWeight += entry.getValue();
            if (randomWeight < currentWeight) {
                return entry.getKey();
            }
        }
        
        return EMOTIONAL_STATE_NAMES[0]; // Fallback to happiness
    }

    private static String selectTimeSensitiveSocialSituation(int hour, int dayOfWeek, String trigger) {
        // Early morning (8-10 AM)
        if (hour >= 8 && hour < 10) {
            return "Alone";  // Most likely getting ready or commuting
        }
        
        // Class hours (10 AM - 4 PM on weekdays)
        if (hour >= 10 && hour < 16 && !(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)) {
            if (trigger.contains("class") || trigger.contains("lecture")) {
                return "In class";
            } else if (trigger.contains("lab")) {
                return "In lab group";
            } else if (trigger.contains("group")) {
                return "With project team";
            }
            return "With classmates";
        }
        
        // Evening (6 PM onwards)
        if (hour >= 18) {
            if (trigger.contains("roommate")) {
                return "With roommates";
            } else if (trigger.contains("study")) {
                return "In study group";
            } else if (trigger.contains("friend")) {
                return "With friends";
            }
            return "Alone";
        }
        
        // Default social situations based on trigger context
        if (trigger.contains("family")) {
            return "With family";
        } else if (trigger.contains("event")) {
            return "At campus event";
        } else if (trigger.contains("project") || trigger.contains("group")) {
            return "With project team";
        }
        
        return "Alone";  // Default to alone if no specific context
    }

    private static void addEventsSequentially(UserProfile userProfile, List<MoodEvent> events, int index, Runnable onComplete) {
        if (index >= events.size()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        MoodEvent event = events.get(index);
        userProfile.addMoodEvent(event, success -> {
            if (success) {
                // Add next event after a small delay
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.postDelayed(() -> {
                    addEventsSequentially(userProfile, events, index + 1, onComplete);
                }, 200); // Increased delay to 200ms to be safer
            } else {
                // If failed, try next event immediately
                addEventsSequentially(userProfile, events, index + 1, onComplete);
            }
        });
    }
}