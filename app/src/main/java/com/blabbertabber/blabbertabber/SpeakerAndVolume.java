package com.blabbertabber.blabbertabber;

/**
 * Created by Cunnie on 9/16/15.
 * <p/>
 * Class that returns speaker and volume
 * This is a throw-away class that will be replaced by actual speaker diarization software
 * which will be some type of service
 */

import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

public class SpeakerAndVolume {
    private static final int MAX_SPEAKERS = 4;
    private static final String TAG = "SpeakerAndVolume";
    public int numSpeakers;
    private int speaker;
    private long nextSpeakerChange;

    // Constructor
    public SpeakerAndVolume() {
        // speakers change on average every 15 seconds
        nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        numSpeakers = 1; // initially only one speaker
        Log.wtf(TAG, "SpeakerAndVolume(): nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
    }

    // Who is currently speaking?
    public int getSpeakerId() {
        if (System.currentTimeMillis() > nextSpeakerChange) {
            speaker = nextSpeaker();
            nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        }
        Log.wtf(TAG, "getSpeaker(): " + speaker + " nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
        return speaker;
    }

    public int getSpeakerVolume() {
        return ThreadLocalRandom.current().nextInt(0, 100);
    }

    // usually returns a speaker different than the current speaker, possibly a new speaker
    private int nextSpeaker() {
        if (newSpeaker()) {
            int nextSpeaker = numSpeakers;
            numSpeakers += 1;
            return (nextSpeaker);
        }
        // I am quite proud of the following line's elegance
        int newSpeaker = (speaker + ThreadLocalRandom.current().nextInt(0, numSpeakers)) % numSpeakers;
        Log.wtf(TAG, "nextSpeaker(): " + newSpeaker);
        return newSpeaker;
    }

    // Are we adding a completely new speaker who hasn't spoken yet?
    private boolean newSpeaker() {
        double p = ((MAX_SPEAKERS - numSpeakers) / (MAX_SPEAKERS - 1.0));
        Log.wtf(TAG, "newSpeaker(): " + p);
        return p > ThreadLocalRandom.current().nextDouble();
    }
}
