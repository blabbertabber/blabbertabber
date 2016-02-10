package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by brendancunnie on 2/8/16.
 */

@RunWith(AndroidJUnit4.class)
public class SpeakersBuilderTest {
    /* Things to test
        1) Constructor.  And build() without anything returns array of 0 length
        2) build() after one addition returns that speaker.
        3) build() after 2 additions to same speaker return array of one element
        4) build() after 2 additions to different speakers return array of 2 elements
        */
    @Test
    public void testConstructor() {
        SpeakersBuilder sb = new SpeakersBuilder();
        Speaker[] speakers = sb.build();
        assertEquals("Building Speakers without adding data returns an empty array.", 0, speakers.length);
    }

    @Test
    public void testOneSpeakerOneTurn() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        Speaker[] speakers = sb.build();
        assertEquals("Building Speakers after adding one 'turn' returns an array with one speaker.", 1, speakers.length);
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's name.", "Sam", speakers[0].getName());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's gender.", 'F', speakers[0].getGender());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's duration.", 279, speakers[0].getDuration());
    }

    @Test
    public void testOneSpeakerTwoTurns() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        sb.add(1000, 200, "Sam", 'F');
        Speaker[] speakers = sb.build();
        assertEquals("Building Speakers after adding one 'turn' returns an array with one speaker.", 1, speakers.length);
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's name.", "Sam", speakers[0].getName());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's gender.", 'F', speakers[0].getGender());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's duration.", 479, speakers[0].getDuration());
    }

    @Test
    public void testTwoSpeakersOneTurns() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        sb.add(1000, 200, "Pat", 'M');
        sb.add(2000, 100, "Pat", 'M');
        sb.add(3000, 50, "Pat", 'M');
        Speaker[] speakers = sb.build();
        assertEquals("Building Speakers after adding one 'turn' returns an array with one speaker.", 2, speakers.length);
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's name.", "Pat", speakers[0].getName());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's gender.", 'M', speakers[0].getGender());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's duration.", 350, speakers[0].getDuration());
    }

    @Test
    public void testBuildReturnsSortedArray() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        sb.add(1000, 300, "Teri", 'M');
        sb.add(2000, 1, "Pat", 'M');
        sb.add(3000, 50, "Leslie", 'M');
        Speaker[] speakers = sb.build();
        assertEquals("First speaker has most time.", 300, speakers[0].getDuration());
        assertEquals("Second speaker has second-most time.", 279, speakers[1].getDuration());
        assertEquals("Last speaker has least time.", 1, speakers[3].getDuration());
    }
}
