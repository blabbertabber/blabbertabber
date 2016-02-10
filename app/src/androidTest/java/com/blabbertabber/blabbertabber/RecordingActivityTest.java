package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by cunnie on 9/12/15.
 * Test Recording Activity
 * <p/>
 * FIXME: these tests will fail the first time BlabberTabber is run because there will be a
 * dialog asking permission to allow BlabberTabber to access microphone
 */
public class RecordingActivityTest {
    @Rule
    public IntentsTestRule<RecordingActivity> mActivityRule =
            new IntentsTestRule<RecordingActivity>(RecordingActivity.class);
    Activity currentActivity;

    // It's easier to layout the dots if we always assume portrait; we may revisit this decision.
    @Test
    public void rootViewTest() {
        // fail: android:visibility="gone" in activity_recording.xml
        onView(withId(R.id.recording_root_view)).check(matches(isDisplayed()));
    }

    // Test the dummies; this can be fleshed out later
    // fail: android:visibility="gone" in activity_main.xml
    // fail: android:clickable="false" in activity_main.xml
    @Test
    public void recordingTest() {
        onView(withId(R.id.button_record)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.button_record)).check(matches(isClickable()));
    }

    @Test
    public void pauseTest() {
        onView(withId(R.id.button_pause)).check(matches(isDisplayed()));
        onView(withId(R.id.button_pause)).check(matches(isClickable()));
    }

    @Test
    public void FinishTest() {
        onView(withId(R.id.button_finish)).check(matches(isDisplayed()));
        onView(withId(R.id.button_finish)).check(matches(isClickable()));
    }

    @Test
    public void pushRecordingTest() {
        onView(withId(R.id.button_pause)).perform(click());

        onView(withId(R.id.button_record)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.button_pause)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        // test the toggle feature; can't be in a separate test because it resets the state
        // note that we click button_record because that's the visible one, not button_pause.
        onView(withId(R.id.button_record)).perform(click());

        onView(withId(R.id.button_record)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.button_pause)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}