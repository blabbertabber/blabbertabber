package com.blabbertabber.blabbertabber;

import android.content.pm.ActivityInfo;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.close;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.not;

/**
 * Test Recording Activity
 */

@RunWith(AndroidJUnit4.class)
public class RecordingActivityTest {

    @Rule
    public ActivityTestRule<RecordingActivity> mActivityRule =
            new ActivityTestRule<RecordingActivity>(RecordingActivity.class);
    private RecordingActivity mActivity;

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
    }

    @SmallTest
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

    private void resetMeeting() {
        // pauses recording, resets meeting. BlabberTabber is NOT recording
        if (RecordingService.recording) {
            onView(withId(R.id.button_pause)).perform(click());
        }
        onView(withId(R.id.button_reset)).perform(click());
    }

    @Test
    public void whenIResetMeetingItShouldBePaused() {
        // if R.id.button_pause is displayed (artifact of earlier state)
        // then click it so that it is paused and we can Reset.
        resetMeeting();
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
        resetMeeting();

        onView(withId(R.id.meeting_timer)).check(matches(withText("0:00")));
        assertMeetingIsPaused();
    }

    @Test
    public void whenIRotateTheTimerContinuesRunning() throws InterruptedException {
        resetMeeting();
        onView(withId(R.id.button_record)).perform(click());
        long start = System.currentTimeMillis();

        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        while (System.currentTimeMillis() < start + 1001 + 100) { // start + 1 second + update-interval
            Thread.sleep(100); // sleep for 100ms before checking again
        }
        onView(withId(R.id.meeting_timer)).check(matches(withText("0:01")));

        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        while (System.currentTimeMillis() < start + 2001 + 100) { // start + 2 seconds + update-interval
            Thread.sleep(100); // sleep for 100ms before checking again
        }
        onView(withId(R.id.meeting_timer)).check(matches(withText("0:02")));
    }

    @Test
    public void openAndCloseDrawerTest() {
        /*
            We cannot match the clickable items in the navigation drawer by their Resource ID
            (e.g. "R.id.show_splash") because NavigationDrawer loses them; instead we
            identify them by their strings
         */

        int[] drawerItems = {
                R.string.play_meeting_wav,
                R.string.launch_main_activity,
                R.string.launch_about_activity,
        };

        // slide open the NavigationDrawer to expose the menuItems
        onView(withId(R.id.drawer_layout)).perform(open());
        for (int i = 0; i < drawerItems.length; i++) {
            onView(withText(drawerItems[i])).check(matches(isDisplayed()));
        }

        // slide the drawer closed, items should NOT be displayed
        onView(withId(R.id.drawer_layout)).perform(close());
        for (int i = 0; i < drawerItems.length; i++) {
            onView(withText(drawerItems[i])).check(matches(not(isDisplayed())));
        }
    }
}