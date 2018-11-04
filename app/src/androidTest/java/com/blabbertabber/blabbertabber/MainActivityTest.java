package com.blabbertabber.blabbertabber;

//import android.support.test.rule.ActivityTestRule;

import android.app.Activity;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * Created by cunnie on 8/30/15.
 * Espresso default
 */


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final String TAG = "MainActivityTest";
    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<MainActivity>(MainActivity.class);
    boolean firstTime = true;

    @Before
    public void isItFirstTime() {
        // If the activity is null, then it's not the first time the application has run, and we've
        // already launched RecordingActivity
        Activity activity = (TestHelpers.getActivityInstance());
        if (activity == null) {
            firstTime = false;
        } else if (activity instanceof com.blabbertabber.blabbertabber.RecordingActivity) {
            firstTime = false;
        } else if (activity instanceof com.blabbertabber.blabbertabber.MainActivity) {
            firstTime = true;
        } else {
            Log.wtf(TAG, "I shouldn't get here");
        }
    }

    @Test
    public void recordingButtonTest() {

        if (firstTime) {
            // fail: android:visibility="gone" in activity_main.xml
            onView(withId(R.id.launch_recording_activity)).check(matches(isDisplayed()));
            // fail: android:clickable="false" in activity_main.xml
            onView(withId(R.id.launch_recording_activity)).check(matches(isClickable()));
            Log.i(TAG, "first time: MainActivity (splash screen) is displayed once, ever.");
        } else {
            Log.i(TAG, "NOT first time: MainActivity has launched RecordingActivity");
        }
    }

    @Test
    public void recordingButtonClickedTest() {
        if (firstTime) {
            onView(withId(R.id.launch_recording_activity)).perform(click());
            intended(hasComponent(RecordingActivity.class.getName()));
        }
    }

}