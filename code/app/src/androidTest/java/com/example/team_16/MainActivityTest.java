package com.example.team_16;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.team_16.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Cases to verify the correct movement between the Login page to SignUp and Reset Password.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test 1: Clicking the sign-up button should open the SignUp fragment.
     */
    @Test
    public void testSignUpButtonOpensSignUpFragment() {
        onView(withId(R.id.signUpButton)).perform(click());
        onView(withId(R.id.signUpToolbar)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Clicking the reset password button should open the ResetPassword fragment.
     */
    @Test
    public void testResetPasswordButtonOpensResetPasswordFragment() {
        onView(withId(R.id.resetPasswordButton)).perform(click());
        onView(withId(R.id.resetToolbar)).check(matches(isDisplayed()));
    }

}

