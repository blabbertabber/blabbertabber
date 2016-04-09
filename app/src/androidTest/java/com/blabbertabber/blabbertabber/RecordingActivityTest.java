package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void InitialScreenTest() {
        // if R.id.button_pause is displayed (artifact of earlier state)
        // then click it so that button_record is displayed and test can continue
        if (RecordingService.recording) {
            onView(withId(R.id.button_pause)).perform(click());
        }
        assertMeetingIsPaused();
    }

    @Test
    public void pushRecordingTest() {
        // if R.id.button_record is displayed (artifact of earlier state)
        // then click it so that button_pause is displayed and test can continue
        if (!RecordingService.recording) {
            onView(withId(R.id.button_record)).perform(click());
        }

        onView(withId(R.id.button_pause)).perform(click());

        assertMeetingIsPaused();

        // test the toggle feature; can't be in a separate test because it resets the state
        // note that we click button_record because that's the visible one, not button_pause.
        // note that reset/finish are only visible when recording is paused
        onView(withId(R.id.button_record)).perform(click());

        assertMeetingIsRecording();
    }

    private void assertMeetingIsPaused() {
        assertFalse("Meeting should not be recording", RecordingService.recording);

        onView(withId(R.id.button_pause)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.button_pause_caption)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        onView(withId(R.id.button_record)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.button_record_caption)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.button_reset)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.button_finish)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    private void assertMeetingIsRecording() {
        assertTrue("Meeting should be recording", RecordingService.recording);

        onView(withId(R.id.button_pause)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.button_pause_caption)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.button_record)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.button_record_caption)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        onView(withId(R.id.button_reset)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.button_finish)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    }

    @Test
    public void whenIResetMeetingItShouldBePaused() {
        // if R.id.button_pause is displayed (artifact of earlier state)
        // then click it so that it is paused and we can Reset.
        if (RecordingService.recording) {
            onView(withId(R.id.button_pause)).perform(click());
        }
        onView(withId(R.id.button_reset)).perform(click());
        assertMeetingIsPaused();
    }

    @Test
    public void whenIResetMeetingTheTimeShouldBeResetToZero() {
        // if R.id.button_pause is displayed (artifact of earlier state)
        // then click it so that it is paused and we can Reset.
        if (!RecordingService.recording) {
            onView(withId(R.id.button_record)).perform(click());
        }
        try {
            Thread.sleep(1001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.button_pause)).perform(click());
        onView(withId(R.id.button_reset)).perform(click());

        onView(withId(R.id.meeting_timer)).check(matches(withText("0:00")));
        assertMeetingIsPaused();
    }
}