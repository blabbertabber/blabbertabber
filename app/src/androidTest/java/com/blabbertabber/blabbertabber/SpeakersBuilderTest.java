package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
        ArrayList<Speaker> speakers = sb.build();
        assertEquals("Building Speakers without adding data returns an empty array.",
                0, speakers.size());
    }

    @Test
    public void testOneSpeakerOneTurn() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        ArrayList<Speaker> speakers = sb.build();
        assertEquals("Building Speakers after adding one 'turn' returns an array with one speaker.",
                1, speakers.size());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's name.",
                "Sam", speakers.get(0).getName());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's gender.",
                'F', speakers.get(0).getGender());
        assertEquals("Building Speakers after adding one 'turn' returns an array with that speaker's duration.",
                279, speakers.get(0).getDuration());
    }

    @Test
    public void testOneSpeakerTwoTurns() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        sb.add(1000, 200, "Sam", 'F');
        ArrayList<Speaker> speakers = sb.build();
        assertEquals("Adding one 'turn' returns an array with one speaker.", 1, speakers.size());
        assertEquals("Adding one 'turn' returns an array with that speaker's name.",
                "Sam", speakers.get(0).getName());
        assertEquals("Adding one 'turn' returns an array with that speaker's gender.",
                'F', speakers.get(0).getGender());
        assertEquals("Adding one 'turn' returns an array with that speaker's duration.",
                479, speakers.get(0).getDuration());
    }

    @Test
    public void testTwoSpeakersOneTurns() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        sb.add(1000, 200, "Pat", 'M');
        sb.add(2000, 100, "Pat", 'M');
        sb.add(3000, 50, "Pat", 'M');
        ArrayList<Speaker> speakers = sb.build();
        assertEquals("Adding one 'turn' returns an array with one speaker.", 2, speakers.size());
        assertEquals("Adding one 'turn' returns an array with that speaker's name.",
                "Pat", speakers.get(0).getName());
        assertEquals("Adding one 'turn' returns an array with that speaker's gender.",
                'M', speakers.get(0).getGender());
        assertEquals("Adding one 'turn' returns an array with that speaker's duration.",
                350, speakers.get(0).getDuration());
    }

    @Test
    public void testBuildReturnsSortedArray() {
        SpeakersBuilder sb = new SpeakersBuilder();
        sb.add(0, 279, "Sam", 'F');
        sb.add(1000, 300, "Teri", 'M');
        sb.add(2000, 1, "Pat", 'M');
        sb.add(3000, 50, "Leslie", 'M');
        ArrayList<Speaker> speakers = sb.build();
        assertEquals("First speaker has most time.", 300, speakers.get(0).getDuration());
        assertEquals("First speaker has correct color.", 0xb0ff6600, speakers.get(0).getColor());
        assertEquals("Second speaker has second-most time.", 279, speakers.get(1).getDuration());
        assertEquals("Second speaker has correct color.", 0xb0ffE600, speakers.get(1).getColor());
        assertEquals("Last speaker has least time.", 1, speakers.get(3).getDuration());
        assertEquals("Last speaker has correct color.", 0xb01aff00, speakers.get(3).getColor());
    }

    @Test
    public void testColorsWrapAround() {
        SpeakersBuilder sb = new SpeakersBuilder();
        for (int i = 0; i < 16; i++) {
            sb.add(0, 279, "Sam " + i, 'F');
        }
        sb.add(0, 179, "Bobo", 'M');
        assertEquals("Last speaker has correct color.", 0xb0ff6600, sb.build().get(16).getColor());
    }

    @Test
    public void parseSegStreamTest() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(
                "BlabTab 1 0 568 U U U S0".getBytes(StandardCharsets.UTF_8));

        ArrayList<Speaker> speakers = new SpeakersBuilder().parseSegStream(inputStream).build();
        assertEquals("The number of speakers created is 1", 1, speakers.size());
        assertEquals("returns an array with that speaker's name.", "S0", speakers.get(0).getName());
        assertEquals("returns an array with that speaker's gender.", 'U', speakers.get(0).getGender());
        assertEquals("returns an array with that speaker's duration.", 568, speakers.get(0).getDuration());
    }
}
