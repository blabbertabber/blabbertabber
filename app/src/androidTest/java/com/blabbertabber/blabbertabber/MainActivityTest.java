package com.blabbertabber.blabbertabber;

//import android.support.test.rule.ActivityTestRule;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

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
@SmallTest
public class MainActivityTest {
    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<MainActivity>(MainActivity.class);

    @Test
    public void recordingButtonTest() {
        // fail: android:visibility="gone" in activity_main.xml
        onView(withId(R.id.button_record)).check(matches(isDisplayed()));
        // fail: android:clickable="false" in activity_main.xml
        onView(withId(R.id.button_record)).check(matches(isClickable()));
    }

    @Test
    public void recordingButtonClickedTest() {
        //
        onView(withId(R.id.button_record)).perform(click());
        intended(hasComponent(RecordingActivity.class.getName()));
    }

}