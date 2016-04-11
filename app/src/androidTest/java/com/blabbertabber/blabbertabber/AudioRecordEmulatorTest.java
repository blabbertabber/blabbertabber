package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by cunnie on 4/10/16.
 */
@RunWith(AndroidJUnit4.class)
public class AudioRecordEmulatorTest {
    static final int ARRAYSIZE = 16;

    @Test
    public void readReturnsRandomData() {
        int recorderBufferSizeInBytes = ARRAYSIZE * 2; // 2 bytes per short
        short[] audioData = new short[ARRAYSIZE];
        Arrays.fill(audioData, (short) 0);

        AudioRecordEmulator audioRecordEmulator = new AudioRecordEmulator(0, 0, 0, 0, recorderBufferSizeInBytes);
        long startTime = System.currentTimeMillis();
        int framesRead = audioRecordEmulator.read(audioData, 0, ARRAYSIZE);
        long endTime = System.currentTimeMillis();
        assertEquals("We expect to see " + ARRAYSIZE + " frames", ARRAYSIZE, framesRead);
        assertFalse("read() populates the array with randomData", isArrayAllZeroes(audioData));
        // when the sleep() is commented-out we get a delay of 0ms
        assertTrue("We expect a delay of at least 2ms", endTime - startTime > 2);
    }

    boolean isArrayAllZeroes(short[] PCM) {
        for (short s : PCM) {
            if (s != 0) {
                return false;
            }
        }
        return true;
    }
}