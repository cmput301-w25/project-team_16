//package com.example.team_16;
//
//import static androidx.test.InstrumentationRegistry.getContext;
//
//import androidx.fragment.app.Fragment;
//import androidx.test.core.app.ActivityScenario;
//
//import com.example.team_16.database.FirebaseDB;
//import com.example.team_16.models.EmotionalStateRegistry;
//import com.example.team_16.models.MoodEvent;
//import com.example.team_16.ui.activity.MoodDetailsTestsActivity;
//import com.example.team_16.ui.fragments.MoodDetails;
//import com.google.firebase.Timestamp;
//
//import java.util.Date;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.assertNotNull;
//
//import android.os.Bundle;
//import android.util.Log;
//
//public class MoodDetailsTests {
//
//    MoodEvent moodEventOne = new MoodEvent();
//    MoodEvent moodEventTwo = new MoodEvent();
//
//    @Before
//    public void setup() {
//        Log.e("MoodDetailsTests", "in setup");
//
//        moodEventOne = new MoodEvent(
//                "mood123", // Unique ID
//                new Timestamp(new Date()), // Current timestamp
//                EmotionalStateRegistry.getByName("Happiness"),
//                "Had a great day!",
//                "user_001",
//                "With colleagues"
//        );
//
//        moodEventTwo = new MoodEvent(
//                "mood456",
//                new Timestamp(new Date()),
//                EmotionalStateRegistry.getByName("Sadness"),
//                "Lost my book.",
//                "user_002",
//                "Alone"
//        );
//
////        FirebaseDB firebaseDB = FirebaseDB.getInstance(getContext());
////        moodEventOne.saveToFirestore(firebaseDB, new FirebaseDB.FirebaseCallback<Boolean>() {
////            @Override
////            public void onCallback(Boolean success) {
////                if (success) {
////                    Log.e("MoodDetailsTests", "mood event saved");
////                } else {
////                    Log.e("MoodDetailsTests", "mood event not saved");
////                }
////            }
////        });
////        moodEventTwo.saveToFirestore(firebaseDB, new FirebaseDB.FirebaseCallback<Boolean>() {
////            @Override
////            public void onCallback(Boolean success) {
////                if (success) {
////
////                } else {
////
////                }
////            }
////        });
////        Log.e("MoodDetailsTests", "moodEventTwo");
//
//    }
//
//    @Test
//    public void moodOne() {
//        Log.e("MoodDetailsTests", "activity created");
//
//        try (ActivityScenario<MoodDetailsTestsActivity> activityScenario = ActivityScenario.launch(MoodDetailsTestsActivity.class)) {
//            activityScenario.onActivity(activity -> {
//                Log.e("MoodDetailsTests", "activity created");
//
//                Log.e("MoodDetailsTests", "begin fragment creation");
//                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
//                Log.e("MoodDetailsTests", "fragment created, creating bundle");
//                //Bundle args = new Bundle();
//                Log.e("MoodDetailsTests", "setting fragment params");
//                //args.putString("param1", moodEventTwo.getId());
//                Log.e("MoodDetailsTests", "set fragment to mooddetailsFragment if ");
//                if (fragment instanceof MoodDetails) {
//                    Log.e("MoodDetailsTests", "set fragment to mooddetails fragment");
//                    MoodDetails fragmentInstance = (MoodDetails) fragment;
//                    assertNotNull(fragmentInstance.getView());
//                }
//            });
//
//            // Wait for 7 seconds for the user to analyze
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Test Two: Display moodEventTwo
//    @Test
//    public void moodTwo() {
//
//        try (ActivityScenario<MoodDetailsTestsActivity> activityScenario = ActivityScenario.launch(MoodDetailsTestsActivity.class)) {
//            activityScenario.onActivity(activity -> {
//
//                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
//                if (fragment instanceof MoodDetails) {
//                    MoodDetails fragmentInstance = (MoodDetails) fragment;
//                    //fragmentInstance.receiveData(moodEventTwo);
//                    assertNotNull(fragmentInstance.getView());
//                }
//            });
//
//            // Wait for 7 seconds for the user to analyze
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}




package com.example.team_16;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;

import com.example.team_16.models.EmotionalStateRegistry;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.ui.fragments.MoodDetails;
import com.google.firebase.Timestamp;
import com.example.team_16.R;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class MoodDetailsTests {

    MoodEvent moodEventOne;
    MoodEvent moodEventTwo;

    @Before
    public void setup() {
        Log.e("MoodDetailsTests", "in setup");

        moodEventOne = new MoodEvent(
                "mood123", // Unique ID
                new Timestamp(new Date()), // Current timestamp
                EmotionalStateRegistry.getByName("Happiness"),
                "Had a great day!",
                "user_001",
                "With colleagues"
        );

        moodEventTwo = new MoodEvent(
                "mood456",
                new Timestamp(new Date()),
                EmotionalStateRegistry.getByName("Sadness"),
                "Lost my book.",
                "user_002",
                "Alone"
        );
    }

    @Test
    public void testReceiveDataUpdatesUIForMoodOne() {
        Bundle bundle = new Bundle();
        bundle.putString("param1", ""); // Ensure no Firebase callback branch is triggered
        //FragmentScenario<MoodDetails> scenario = FragmentScenario.launchInContainer(MoodDetails.class, bundle, R.style.AppTheme, null);
        FragmentScenario<MoodDetails> scenario = FragmentScenario.launchInContainer(
                MoodDetails.class,
                bundle,
                R.style.AppTheme,
                (androidx.fragment.app.FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            fragment.receiveData(moodEventOne);
            View view = fragment.getView();
            assertNotNull("Fragment view should not be null", view);
            TextView moodOneView = view.findViewById(R.id.mood_one);
            assertNotNull("Mood one TextView should not be null", moodOneView);
            assertEquals("Happiness", moodOneView.getText().toString());
        });
    }

    @Test
    public void testReceiveDataUpdatesUIForMoodTwo() {
        Bundle bundle = new Bundle();
        bundle.putString("param1", "");
        //FragmentScenario<MoodDetails> scenario = FragmentScenario.launchInContainer(MoodDetails.class, bundle, R.style.AppTheme, null);
        FragmentScenario<MoodDetails> scenario = FragmentScenario.launchInContainer(
                MoodDetails.class,
                bundle,
                R.style.AppTheme,
                (androidx.fragment.app.FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            fragment.receiveData(moodEventTwo);
            View view = fragment.getView();
            assertNotNull("Fragment view should not be null", view);
            TextView moodOneView = view.findViewById(R.id.mood_one);
            assertNotNull("Mood one TextView should not be null", moodOneView);
            assertEquals("Sadness", moodOneView.getText().toString());
        });
    }
}
