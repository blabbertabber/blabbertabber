package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by cunnie on 11/17/15.
 */

@RunWith(AndroidJUnit4.class)
public class SpeakerTest {
    private static final String TAG = "SpeakerTest";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testConstructor() {
        Speaker speaker = new Speaker("Sam", 'M');
        assertEquals("Newly constructed speaker's name is Sam", "Sam", speaker.getName());
        assertEquals("Newly constructed speaker's gender is M", 'M', speaker.getGender());
    }

    @Test
    public void testAddTurn() {
        Speaker speaker = new Speaker("Sam", 'M');
        speaker.addTurn(529, 293);
        assertEquals("Duration is 293", 293, speaker.getDuration());
    }

    @Test
    public void testCompareto() {
        Speaker big = new Speaker("big", 1000);
        Speaker medium = new Speaker("medium", 100);
        Speaker small = new Speaker("small", 10);

        assertEquals("big is bigger than medium", 1, big.compareTo(medium));
        assertEquals("medium is smaller than big", -1, medium.compareTo(big));
        assertEquals("medium is bigger than small", 1, medium.compareTo(small));
        assertEquals("small is smaller than big", -1, small.compareTo(big));
        assertEquals("small is smaller than big", -1, small.compareTo(big));
        assertEquals("small is small", small.compareTo(small), 0);
    }

    @Test
    public void testComparetoWithNames() {
        // force the sort by names, not duration
        Speaker medium = new Speaker("medium", 100);
        Speaker small = new Speaker("small", 10);
        Speaker alpha = new Speaker("alpha", 10);
        Speaker bravo = new Speaker("bravo", 10);
        Speaker charlie = new Speaker("charlie", 10);

        assertTrue("medium is bigger than alpha", medium.compareTo(alpha) > 0);
        assertTrue("alpha is smaller than bravo", alpha.compareTo(bravo) < 0);
        assertTrue("bravo is smaller than charlie", bravo.compareTo(charlie) < 0);
        assertTrue("charlie is smaller than small", charlie.compareTo(small) < 0);
        assertTrue("small is bigger than alpha", small.compareTo(alpha) > 0);
    }

    @Test
    public void testCompareto_2() {
        exception.expect(NullPointerException.class);
        new Speaker("zero", 0).compareTo(null);
    }
}
