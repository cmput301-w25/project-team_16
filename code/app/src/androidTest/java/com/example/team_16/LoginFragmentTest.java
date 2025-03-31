package com.example.team_16;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.fragments.LoginFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Cases to verify the correct movement between the Login fragment to SignUp and Reset Password.
 */
@RunWith(AndroidJUnit4.class)
public class LoginFragmentTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> activityRule =
            new ActivityScenarioRule<>(HomeActivity.class);

    /**
     * Setup: Ensure we're on the login fragment
     */
    @Before
    public void setup() {
        // We need to make sure we're on the login fragment
        // This will happen automatically if this is a fresh install or the user is logged out
        // But we can also force it via the activity if needed:
        activityRule.getScenario().onActivity(activity -> {
            if (!(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof LoginFragment)) {
                activity.showLoginFragment();
            }
        });
    }

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

    /**
     * Alternative approach using FragmentScenario
     * This can be used if you want to test the fragment in isolation
     */
    /*
    @Test
    public void testFragmentInIsolation() {
        FragmentScenario<LoginFragment> fragmentScenario =
            FragmentScenario.launchInContainer(LoginFragment.class);

        // Now you can test the fragment in isolation
        onView(withId(R.id.signUpButton)).perform(click());
        onView(withId(R.id.signUpToolbar)).check(matches(isDisplayed()));
    }
    */
}