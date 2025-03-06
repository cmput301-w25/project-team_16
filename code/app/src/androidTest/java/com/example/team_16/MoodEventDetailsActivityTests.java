package com.example.team_16;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

public class MoodEventDetailsActivityTests {


    //Test One
    @Test
    public void allValues() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, MoodEventDetailsActivity.class);

        intent.putExtra("Mood_One", "Angry");
        intent.putExtra("Mood_Two", "Fear");
        intent.putExtra("First_Name_Last_Name", "Adam Smith");
        intent.putExtra("Username", "adamsmith");
        intent.putExtra("With_Amount", "Two People");
        intent.putExtra("Mood_Description", "Scary dog");
        intent.putExtra("Time", "09:30");
        intent.putExtra("Date", "2025-03-02");
        intent.putExtra("Location", "Vancouver, BC");
        intent.putExtra("Mood_Image_URL", "https://firebasestorage.googleapis.com/v0/b/moodtrackerteam16.firebasestorage.app/o/dog-puppy-on-garden-royalty-free-image-1586966191.avif?alt=media&token=dd513b51-ad8a-4096-8407-f3f743ba2206");
        intent.putExtra("Profile_Image_URL", "https://firebasestorage.googleapis.com/v0/b/moodtrackerteam16.firebasestorage.app/o/blank-profile-circle.png?alt=media&token=7f01cfff-bc66-4c73-982c-79e6f2e7e678");

        ActivityScenario<MoodEventDetailsActivity> scenario = ActivityScenario.launch(intent);

        // delay to analyze
        try {
            Thread.sleep(7000); // Simulating delay for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //Test Two
    @Test
    public void noImage() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, MoodEventDetailsActivity.class);

        intent.putExtra("Mood_One", "Happiness");
        intent.putExtra("Mood_Two", "Shame");
        intent.putExtra("First_Name_Last_Name", "John Doe");
        intent.putExtra("Username", "jdoe");
        intent.putExtra("With_Amount", "One Person");
        intent.putExtra("Mood_Description", "Lorem ipsum dolor");
        intent.putExtra("Time", "5:20");
        intent.putExtra("Date", "2025-02-25");
        intent.putExtra("Location", "Calgary, AB");
        intent.putExtra("Mood_Image_URL", "");
        intent.putExtra("Profile_Image_URL", "https://firebasestorage.googleapis.com/v0/b/moodtrackerteam16.firebasestorage.app/o/blank-profile-circle.png?alt=media&token=7f01cfff-bc66-4c73-982c-79e6f2e7e678");

        ActivityScenario<MoodEventDetailsActivity> scenario = ActivityScenario.launch(intent);

        // delay to analyze
        try {
            Thread.sleep(7000); // Simulating delay for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Test Three
    @Test
    public void noTriggerOrLocation() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, MoodEventDetailsActivity.class);

        intent.putExtra("Mood_One", "Surprised");
        intent.putExtra("Mood_Two", "Sad");
        intent.putExtra("First_Name_Last_Name", "David Doe");
        intent.putExtra("Username", "ddoe");
        intent.putExtra("With_Amount", "Crowd");
        intent.putExtra("Mood_Description", "");
        intent.putExtra("Time", "12:30");
        intent.putExtra("Date", "2024-09-25");
        intent.putExtra("Location", "");
        intent.putExtra("Mood_Image_URL", "https://firebasestorage.googleapis.com/v0/b/moodtrackerteam16.firebasestorage.app/o/moodimage2.jpg?alt=media&token=625fdc87-211f-429c-8a25-c044fb66b88a");
        intent.putExtra("Profile_Image_URL", "https://firebasestorage.googleapis.com/v0/b/moodtrackerteam16.firebasestorage.app/o/profile2.jpg?alt=media&token=66217d30-3609-48e9-8b6f-29bd38702f1a");

        ActivityScenario<MoodEventDetailsActivity> scenario = ActivityScenario.launch(intent);

        // delay to analyze
        try {
            Thread.sleep(7000); // Simulating delay for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
