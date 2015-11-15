package com.blabbertabber.blabbertabber;

import android.util.Log;
import android.view.View;

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

    private static void initializeSpeakers() {
        Log.i(TAG, "initializeSpeakers()");
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            speakers[i] = new Speaker();
            speakers[i].setVisible(View.INVISIBLE);
            speakers[i].setColor(speakerColors[i]);
        }

        speakers[0].setViewID(R.id.speaker_0);
        speakers[1].setViewID(R.id.speaker_1);
        speakers[2].setViewID(R.id.speaker_2);
        speakers[3].setViewID(R.id.speaker_3);
        speakers[4].setViewID(R.id.speaker_4);
        speakers[5].setViewID(R.id.speaker_5);
        speakers[6].setViewID(R.id.speaker_6);
        speakers[7].setViewID(R.id.speaker_7);
        speakers[8].setViewID(R.id.speaker_8);
        speakers[9].setViewID(R.id.speaker_9);
        speakers[10].setViewID(R.id.speaker_10);
        speakers[11].setViewID(R.id.speaker_11);
        speakers[12].setViewID(R.id.speaker_12);
        speakers[13].setViewID(R.id.speaker_13);
        speakers[14].setViewID(R.id.speaker_14);
        speakers[15].setViewID(R.id.speaker_15);
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
}
