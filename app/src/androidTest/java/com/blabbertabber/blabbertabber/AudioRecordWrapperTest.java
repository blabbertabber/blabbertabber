package com.blabbertabber.blabbertabber;

import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by cunnie on 10/8/15.
 */

@RunWith(AndroidJUnit4.class)
public class AudioRecordWrapperTest {
    private static final String TAG = "AudioRecordWrapperTest";

    @Test
    public void startingAndStoppingTest() {
        if (!"goldfish".equals(Build.HARDWARE)) {
            // the following should NOT throw an exception
//            assertTrue("isRecording() should be true after calling AudioRecordWrapper.startRecording()", AudioRecordWrapper.isRecording());
//            AudioRecordWrapper.stop();
//            assertTrue("isRecording() should be true after calling AudioRecordWrapper.startRecording() again", AudioRecordWrapper.isRecording());
//            AudioRecordWrapper.close();
//            assertFalse("isRecording() should be false after calling AudioRecordWrapper.close()", AudioRecordWrapper.isRecording());
        }
    }
}
