package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 11/17/15.
 */

@RunWith(AndroidJUnit4.class)
public class SpeakerTest {
    private static final String TAG = "HelperTest";
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCompareto() {
        Speaker big = new Speaker(1000);
        Speaker medium = new Speaker(100);
        Speaker small = new Speaker(10);
        assertEquals("big is bigger than medium", big.compareTo(medium), 1);
        assertEquals("medium is smaller than big", medium.compareTo(big), -1);
        assertEquals("medium is bigger than small", medium.compareTo(small), 1);
        assertEquals("small is smaller than big", small.compareTo(big), -1);
        assertEquals("small is smaller than big", small.compareTo(big), -1);
        assertEquals("small is small", small.compareTo(small), 0);
    }

    @Test
    public void testCompareto_2() {
        exception.expect(NullPointerException.class);
        new Speaker(0).compareTo(null);
    }
}
