package com.blabbertabber.blabbertabber;

import android.app.Activity;
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
public class SummaryActivityTest {
    @Rule
    public IntentsTestRule<SummaryActivity> mActivityRule =
            new IntentsTestRule<SummaryActivity>(SummaryActivity.class);
    Activity currentActivity;

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
}