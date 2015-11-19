package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 11/18/15.
 */

@RunWith(AndroidJUnit4.class)
public class TheSpeakersTest {
    private static final String TAG = "TheSpeakersTest";

    @Test
    public void testIsSingleton() {
        TheSpeakers test1 = TheSpeakers.getInstance();
        TheSpeakers test2 = TheSpeakers.getInstance();
        assertEquals("Two instances are equal, not merely equivalent", test1, test2);
    }

    @Test
    public void testCanSort() {
        TheSpeakers ts = TheSpeakers.getInstance();
        ArrayList<Speaker> speakerList = new ArrayList<Speaker>();
        speakerList.add(new Speaker("chatterbox", 40_000));
        speakerList.add(new Speaker("blabbermouth", 50_000));
        speakerList.add(new Speaker("chatty cathy", 60_000));
        speakerList.add(new Speaker("wallflower", 5_000));
        speakerList.add(new Speaker("mute", 0));
        ts.setInstance(speakerList);
        ArrayList<Speaker> sortedSpeakerList = ts.getSortedSpeakerList();

        assertEquals("The sorted speaker list has only 4 entries", 4, sortedSpeakerList.size());
        assertEquals("The first speaker's name is 'chatty cathy'", "chatty cathy", sortedSpeakerList.get(0).getName());
        assertEquals("The second speaker's name is 'blabbermouth'", "blabbermouth", sortedSpeakerList.get(1).getName());
        assertEquals("The third speaker's name is 'chatterbox'", "chatterbox", sortedSpeakerList.get(2).getName());
        assertEquals("The fourth speaker's name is 'wallflower'", "wallflower", sortedSpeakerList.get(3).getName());
    }
}
