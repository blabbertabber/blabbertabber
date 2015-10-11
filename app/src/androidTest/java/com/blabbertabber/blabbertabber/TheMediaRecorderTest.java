package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 10/8/15.
 */

@RunWith(AndroidJUnit4.class)
public class TheMediaRecorderTest {
    private static final String TAG = "TheMediaRecorderTest";
    public TheMediaRecorder test1 = TheMediaRecorder.getInstance();
    public TheMediaRecorder test2 = TheMediaRecorder.getInstance();

    @Test
    public void testIsSingleton() {
        assertEquals("Two instances are equal, not merely equivalent", test1, test2);
    }
}
