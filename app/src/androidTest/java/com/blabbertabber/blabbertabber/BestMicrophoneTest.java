package com.blabbertabber.blabbertabber;

import android.media.MediaRecorder;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 10/6/15.
 * <p/>
 * Test that devices are using the correct microphones.
 */

@RunWith(AndroidJUnit4.class)
public class BestMicrophoneTest {
    private static final String TAG = "BestMicrophoneTest";
    private String mBuildModel;

    @Test
    public void testDefaultDeviceHasMic() {
        mBuildModel = "fake device";
        Log.i(TAG, "mBuildModel == " + mBuildModel);
        assertEquals("Default should use MediaRecorder.AudioSource.DEFAULT", MediaRecorder.AudioSource.DEFAULT, BestMicrophone.getBestMicrophone(mBuildModel));
    }

    @Test
    public void testNexus5HasMic() {
        mBuildModel = "Nexus 5";
        Log.i(TAG, "mBuildModel == " + mBuildModel);
        assertEquals("Nexus 5 should use MediaRecorder.AudioSource.MIC", MediaRecorder.AudioSource.MIC, BestMicrophone.getBestMicrophone(mBuildModel));
    }

    @Test
    public void testNexus6HasVoiceRecognition() {
        mBuildModel = "Nexus 6";
        Log.i(TAG, "mBuildModel == " + mBuildModel);
        assertEquals("Nexus 6 should use MediaRecorder.AudioSource.VOICE_RECOGNITION", MediaRecorder.AudioSource.VOICE_RECOGNITION, BestMicrophone.getBestMicrophone(mBuildModel));
    }
}

