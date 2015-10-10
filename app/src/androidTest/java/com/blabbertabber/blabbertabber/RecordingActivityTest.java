package com.blabbertabber.blabbertabber;

import android.support.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by cunnie on 9/12/15.
 * Test Recording Activity
 */
public class RecordingActivityTest {

    @Rule
    public IntentsTestRule<RecordingActivity> mActivityRule =
            new IntentsTestRule<RecordingActivity>(RecordingActivity.class);

    @Test
    public void rootViewTest() {
        // fail: android:visibility="gone" in activity_recording.xml
        onView(withId(R.id.recording_root_view)).check(matches(isDisplayed()));
    }


    // Test the dummies; this can be fleshed out later
    // fail: android:visibility="gone" in activity_main.xml
    // fail: android:clickable="false" in activity_main.xml
    @Test
    public void dummyRecordingTest() {
        onView(withId(R.id.dummy_recording_button)).check(matches(isDisplayed()));
        onView(withId(R.id.dummy_recording_button)).check(matches(isClickable()));
    }

    @Test
    public void dummyStopTest() {
        onView(withId(R.id.dummy_stop_button)).check(matches(isDisplayed()));
        onView(withId(R.id.dummy_stop_button)).check(matches(isClickable()));
    }

    @Test
    public void dummyFinishTest() {
        onView(withId(R.id.dummy_finish_button)).check(matches(isDisplayed()));
        onView(withId(R.id.dummy_finish_button)).check(matches(isClickable()));
    }

}