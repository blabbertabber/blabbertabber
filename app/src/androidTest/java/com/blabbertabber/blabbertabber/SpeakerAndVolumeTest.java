package com.blabbertabber.blabbertabber;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created by cunnie on 9/16/15.
 *
 * Test SpeakerAndVolume class
 */
public class SpeakerAndVolumeTest {

    @Test
    public void speakerReturnsNonNegativeIntTest() {
        SpeakerAndVolume tester = new SpeakerAndVolume();

        int i;

        for (i=0; i<100; i++) {
            assertTrue("speakerId is non-negative", tester.getSpeakerId() >=  0);
        }
    }

    @Test
    public void volumeReturnsNonNegativeIntTest() {
        SpeakerAndVolume tester = new SpeakerAndVolume();

        int i;

        for (i=0; i<100; i++) {
            assertTrue("speakerVolume is non-negative", tester.getSpeakerVolume() >= 0);
        }
    }

}
