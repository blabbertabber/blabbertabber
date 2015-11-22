package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by cunnie on 11/18/15.
 */

// Helper class for TESTs
// not to be confused with HelperTest, which tests the Helper class
public class TestHelpers {
    static Activity mCurrentActivity;

    // http://qathread.blogspot.com/2014/09/discovering-espresso-for-android-how-to.html
    public static synchronized Activity getActivityInstance() {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                for (Activity act : resumedActivities) {
                    Log.d("Your current activity: ", act.getClass().getName());
                    mCurrentActivity = act;
                    break;
                }
            }
        });
        return mCurrentActivity;
    }
}
