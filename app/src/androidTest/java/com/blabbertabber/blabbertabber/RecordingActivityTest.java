package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 9/12/15.
 * Test Recording Activity
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

    @Test
    public void recordingActivityIsPortrait() {
        int orientation = getActivityInstance().getResources().getConfiguration().orientation;
        assertEquals(orientation, Configuration.ORIENTATION_PORTRAIT);
    }

    // http://qathread.blogspot.com/2014/09/discovering-espresso-for-android-how-to.html
    public Activity getActivityInstance() {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                for (Activity act : resumedActivities) {
                    Log.d("Your current activity: ", act.getClass().getName());
                    currentActivity = act;
                    break;
                }
            }
        });
        return currentActivity;
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