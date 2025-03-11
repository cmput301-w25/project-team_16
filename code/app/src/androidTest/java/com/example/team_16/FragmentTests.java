package com.example.team_16;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
//import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static org.hamcrest.Matchers.allOf;


import android.widget.Toast;

import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Test Cases to verify the correct movement between the Login page to SignUp and Reset Password.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class FragmentTests {

    FirebaseFirestore db;
    int profileNum;

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {

        db = FirebaseFirestore.getInstance();

    }

    /**
     * Test One: Test signup and addMood fragments by creating new account and creating two new mood events in it
     */
    @Test
    public void t1addTwoMoodEventsToNewUser() {

        CountDownLatch latch = new CountDownLatch(1);

        db.collection("following").document("bgoJmto3W3SB8WRzJQSl0B2t7M63")
                .update("following", new ArrayList<String>());

        db.collection("TestIDNums").document("TestIDNumsDocument")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    profileNum = (documentSnapshot.getLong("Num")).intValue();
                    db.collection("TestIDNums").document("TestIDNumsDocument")
                            .update("Num", profileNum + 1)
                            .addOnSuccessListener(aVoid -> {
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    latch.countDown();
                });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Firestore operation timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test interrupted", e);
        }

        onView(withId(R.id.signUpButton)).perform(click());
        delay(5);
        onView(withId(R.id.name))
                .perform(replaceText("TestProfile" + Integer.toString(profileNum)));
        onView(allOf(withId(R.id.username), isDescendantOfA(withId(R.id.signUpContainer))))
                .perform(replaceText("TestProfile" + Integer.toString(profileNum)));
        onView(withId(R.id.email))
                .perform(replaceText("TestProfile" + Integer.toString(profileNum) + "@gmail.com"));
        onView(allOf(withId(R.id.password), isDescendantOfA(withId(R.id.signUpContainer))))
                .perform(replaceText("TestProfile" + Integer.toString(profileNum) + "TestProfile" + Integer.toString(profileNum)));

        delay(5);
        onView(allOf(withId(R.id.signUpButton), isDescendantOfA(withId(R.id.signUpContainer)))).perform(click());

        delay(5);

        onView(allOf(withId(R.id.username), isDescendantOfA(withId(R.id.loginLinear))))
                .perform(replaceText("TestProfile" + Integer.toString(profileNum)));
        onView(allOf(withId(R.id.password), isDescendantOfA(withId(R.id.loginLinear))))
                .perform(replaceText("TestProfile" + Integer.toString(profileNum) + "TestProfile" + Integer.toString(profileNum)));

        onView(allOf(withId(R.id.loginButton), isDescendantOfA(withId(R.id.loginLinear)))).perform(click());

        delay(15);

        onView(withId(R.id.nav_add)).perform(click());

        onView(withId(R.id.anger_button)).perform(click());
        onView(withId(R.id.alone_button)).perform(click());
        delay(5);
        onView(withId(R.id.save_mood_button))
                .check(matches(isDisplayed()))
                .perform(click());

        delay(3);

        delay(15);
        onView(withId(R.id.nav_add)).perform(click());

        onView(withId(R.id.happiness_button)).perform(click());
        onView(withId(R.id.trigger_text))
                .perform(replaceText("TestMood"));
        onView(withId(R.id.crowd_button)).perform(click());
        delay(3);
        onView(withId(R.id.save_mood_button))
                .check(matches(isDisplayed()))
                .perform(click());

        delay(5);

        UserProfile userProfile = ((MoodTrackerApp) ApplicationProvider.getApplicationContext()).getCurrentUserProfile();

        CountDownLatch latch2 = new CountDownLatch(1);
        db.collection("following").document("bgoJmto3W3SB8WRzJQSl0B2t7M63")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (!following.contains(userProfile.getId())) {
                        following.add(userProfile.getId());
                    }

                    db.collection("following").document("bgoJmto3W3SB8WRzJQSl0B2t7M63")
                            .update("following", following)
                            .addOnSuccessListener(aVoid -> {
                                latch2.countDown();
                            })
                            .addOnFailureListener(aVoid -> {
                                latch2.countDown();
                            });
                });

        try {
            if (!latch2.await(60, TimeUnit.SECONDS)) {
                throw new RuntimeException("Firestore operation timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test interrupted", e);
        }

    }

    /**
     * Test 2: Test that moods of followed users are shown on the feed.
     * Prompt user to test mood detail fragment by clicking on the mood
     */
    @Test
    public void t2showMoodEventsThroughExistingFollowingUser() {

        activityRule =
                new ActivityScenarioRule<>(MainActivity.class);

        delay(5);
        onView(allOf(withId(R.id.username), isDescendantOfA(withId(R.id.loginLinear))))
                .perform(replaceText("Test2"));
        onView(allOf(withId(R.id.password), isDescendantOfA(withId(R.id.loginLinear))))
                .perform(replaceText("test2test2"));

        delay(5);
        onView(allOf(withId(R.id.loginButton), isDescendantOfA(withId(R.id.loginLinear)))).perform(click());
        delay(30);

        db.collection("following").document("bgoJmto3W3SB8WRzJQSl0B2t7M63")
                .update("following", new ArrayList<String>());
        db.collection("users").document("TestProfile" + profileNum).delete();

    }

    private void delay (int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
