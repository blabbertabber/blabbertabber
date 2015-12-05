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
            0xffff6600,
            0xffffE600,
            0xff99ff00,
            0xff1aff00,
            0xffff001a,
            0xffff8b3d,
            0xffffaf7a,
            0xff00ff66,
            0xffff0099,
            0xff7acaff,
            0xff3db1ff,
            0xff00ffe6,
            0xffe600ff,
            0xff6600ff,
            0xff001aff,
            0xff0099ff};
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
            speakers[i] = new Speaker("Speaker " + i); // TODO: internationalize
            speakers[i].setColor(speakerColors[i]);
        }
    }

    public static double[] getSpeakersTimes() {
        int iSpeaker = 0;
        double[] speakersTimes = new double[MAX_SPEAKERS];
        for (Speaker s : speakers) {
            speakersTimes[iSpeaker] = (double) s.duration();
            iSpeaker++;
        }
        return speakersTimes;
    }

    // milliseconds; it's in milliseconds
    public long getMeetingDuration() {
        long duration = 0;
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            duration += speakers[i].duration();
        }
        return duration;
    }

    // milliseconds
    public long getAverageSpeakerDuration() {
        long numSpeakers = 0;
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            if (speakers[i].duration() > 0) {
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
            if (s.duration() > max) {
                max = s.duration();
            }
        }
        return max;
    }

    // milliseconds
    public long getMinSpeakerDuration() {
        long min = Long.MAX_VALUE;
        for (Speaker s : speakers) {
            if (s.duration() < min && s.duration() > 0) {
                min = s.duration();
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
            if (s.duration() > 0) sorted.add(s);
        }
        // We use reverseOrder() because we want it in descending order
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }
}
