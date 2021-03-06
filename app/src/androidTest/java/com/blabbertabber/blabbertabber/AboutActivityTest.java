package com.blabbertabber.blabbertabber;

//import android.support.test.rule.ActivityTestRule;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


/**
 * Created by cunnie on 8/30/15.
 * Espresso default
 */


public class AboutActivityTest {
    private static final String TAG = "AboutActivityTest";
    @Rule
    public IntentsTestRule<AboutActivity> mActivityRule =
            new IntentsTestRule<AboutActivity>(AboutActivity.class);

    @Test
    public void buttonAckFinishTest() {
        onView(withId(R.id.button_ack_finish)).check(matches(isDisplayed()));
        onView(withId(R.id.button_ack_finish)).check(matches(isClickable()));
    }
}