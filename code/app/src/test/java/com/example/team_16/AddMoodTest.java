package com.example.team_16;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class AddMoodTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseDB firebaseDB;
    private UserProfile userProfile;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        firebaseDB = new FirebaseDB(context);
        userProfile = new UserProfile(firebaseDB, "12345", "testUser", "Test User", "test@example.com");
    }

    @Test
    public void testMoodSelection() {
        onView(withId(R.id.happiness_button)).perform(click());
        onView(withText("Selected mood: Happiness")).check(matches(isDisplayed()));
    }

    @Test
    public void testOnlyOneMoodIsSelected() {
        onView(withId(R.id.anger_button)).perform(click());
        onView(withText("Selected mood: Anger")).check(matches(isDisplayed()));

        onView(withId(R.id.happiness_button)).perform(click());
        onView(withText("Selected mood: Anger")).check(doesNotExist());
        onView(withText("Selected mood: Happiness")).check(matches(isDisplayed()));
    }

    @Test
    public void testOnlyOneSocialSettingIsSelected() {
        onView(withId(R.id.alone_button)).perform(click());
        onView(withText("Selected setting: Alone")).check(matches(isDisplayed()));

        onView(withId(R.id.crowd_button)).perform(click());
        onView(withText("Selected setting: Alone")).check(doesNotExist());
        onView(withText("Selected setting: Crowd")).check(matches(isDisplayed()));
    }

    @Test
    public void testSaveMoodWithoutSelection_ShowsError() {
        onView(withId(R.id.save_mood_button)).perform(click());
        onView(withText("Please select a mood before saving.")).check(matches(isDisplayed()));
    }

    @Test
    public void testMoodDeletion() {
        EmotionalState state = EmotionalStateRegistry.getByName("Happiness");
        MoodEvent moodEvent = new MoodEvent("testUser", state);
        userProfile.addMoodEvent(moodEvent);

        onView(withId(R.id.happiness_button)).perform(click());
        onView(withText("Selected mood: Happiness")).check(matches(isDisplayed()));

        onView(withId(R.id.delete_entry_button)).perform(click());
        onView(withText("Selected mood: Happiness")).check(doesNotExist());
    }

    @Test
    public void testEnteringTriggerText() {
        onView(withId(R.id.trigger_text)).perform(replaceText("Feeling great today!"));
        onView(withId(R.id.trigger_text)).check(matches(withText("Feeling great today!")));
    }
}