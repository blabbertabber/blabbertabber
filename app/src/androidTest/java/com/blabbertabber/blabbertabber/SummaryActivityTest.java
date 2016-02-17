package com.blabbertabber.blabbertabber;

import android.support.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.close;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.not;

/**
 * Created by cunnie on 9/12/15.
 * Test Recording Activity
 */
public class SummaryActivityTest {
    @Rule
    public IntentsTestRule<SummaryActivity> mActivityRule =
            new IntentsTestRule<SummaryActivity>(SummaryActivity.class);

    // Test the dummies; this can be fleshed out later
    // fail: android:visibility="gone" in activity_main.xml
    // fail: android:clickable="false" in activity_main.xml
    @Test
    public void newMeetingTest() {
        onView(withId(R.id.button_new_meeting)).check(matches(isDisplayed()));
        onView(withId(R.id.button_new_meeting)).check(matches(isClickable()));
    }

    @Test
    public void shareTest() {
        onView(withId(R.id.button_share)).check(matches(isDisplayed()));
        onView(withId(R.id.button_share)).check(matches(isClickable()));
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
                R.string.launch_acknowledgements_activity,
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

    @Test
    public void speakerDurationAndPercentTest() {
        assertEquals("60,000ms meeting and 600cs speaker should return ' 6 (10%) '(",
                "        6 (10%) ",
                SummaryActivity.speakerDurationAndPercent(600L, 60_000L));
    }
}