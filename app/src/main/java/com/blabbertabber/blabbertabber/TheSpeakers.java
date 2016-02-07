package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cunnie on 11/4/15.
 * <p/>
 * A singleton array of Speakers with attributes
 * such as ViewID, color, visibility, etc...
 */
public class TheSpeakers {
    public static final int MAX_SPEAKERS = 16;
    public static final int[] speakerColors = {
            0xb0ff6600,
            0xb0ffE600,
            0xb099ff00,
            0xb01aff00,
            0xb0ff001a,
            0xb0ff8b3d,
            0xb0ffaf7a,
            0xb000ff66,
            0xb0ff0099,
            0xb07acaff,
            0xb03db1ff,
            0xb000ffe6,
            0xb0e600ff,
            0xb06600ff,
            0xb0001aff,
            0xb00099ff};
    private static final String TAG = "TheSpeakers";
    public static Speaker[] speakers = new Speaker[TheSpeakers.MAX_SPEAKERS];
    public static TheSpeakers singleton = new TheSpeakers();

    protected TheSpeakers() {
        initializeSpeakers();
    }

    public synchronized static TheSpeakers getInstance() {
        return singleton;
    }

    // for testing only; allows injection of speakers
    public static void setInstance(ArrayList<Speaker> speakerList) {
        int i = 0;
        for (Speaker speaker : speakerList) {
            speakers[i] = speaker;
            i++;
        }
    }

    private static void initializeSpeakers() {
        Log.i(TAG, "initializeSpeakers()");
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            speakers[i] = new Speaker("Speaker " + i, 'U'); // TODO: internationalize
            speakers[i].setColor(speakerColors[i]);
        }
    }

    public static double[] getSpeakersTimes() {
        int iSpeaker = 0;
        double[] speakersTimes = new double[MAX_SPEAKERS];
        for (Speaker s : speakers) {
            speakersTimes[iSpeaker] = (double) s.getDuration();
            iSpeaker++;
        }
        return speakersTimes;
    }

    // milliseconds; it's in milliseconds
    public long getMeetingDuration() {
        long duration = 0;
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            duration += speakers[i].getDuration();
        }
        return duration;
    }

    // milliseconds
    public long getAverageSpeakerDuration() {
        long numSpeakers = 0;
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            if (speakers[i].getDuration() > 0) {
                numSpeakers++;
            }
        }
        if (numSpeakers > 0) {
            return getMeetingDuration() / numSpeakers;
        } else {
            return 0;
        }
    }

    // milliseconds
    public long getMaxSpeakerDuration() {
        long max = 0;
        for (Speaker s : speakers) {
            if (s.getDuration() > max) {
                max = s.getDuration();
            }
        }
        return max;
    }

    // milliseconds
    public long getMinSpeakerDuration() {
        long min = Long.MAX_VALUE;
        for (Speaker s : speakers) {
            if (s.getDuration() < min && s.getDuration() > 0) {
                min = s.getDuration();
            }
        }
        return min == Long.MAX_VALUE ? 0 : min;
    }

    // reset the speakers' times to zero
    public void reset() {
        initializeSpeakers();
    }

    // sort the speakers, don't include any with no speaking time
    public ArrayList<Speaker> getSortedSpeakerList() {
        ArrayList<Speaker> sorted = new ArrayList<Speaker>();
        for (Speaker s : speakers) {
            if (s.getDuration() > 0) sorted.add(s);
        }
        // We use reverseOrder() because we want it in descending order
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }
}
