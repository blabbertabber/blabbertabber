package com.blabbertabber.blabbertabber;

import java.util.ArrayList;

/**
 * Tracks the duration information for each speaker.
 * Implements Comparable to facilitate sorting.
 */
public class Speaker implements Comparable<Speaker> {
    private static final String TAG = "Speaker";
    private int color;
    private ArrayList<Long> startTimes = new ArrayList<Long>();  // features since start of meeting
    private ArrayList<Long> durations = new ArrayList<Long>();  // duration in features

    private String mName = null;
    private char mGender = '\0';

    // constructor for setting name, preferred constructor
    public Speaker(String name, char gender) {
        mName = name;
        mGender = gender;
    }

    // constructor that allows injecting totalSpeakingTimeInMilliseconds and name; meant for tests exclusively
    public Speaker(String name, long totalSpeakingTimeInMilliseconds) {
        startTimes.add(0L);
        durations.add(totalSpeakingTimeInMilliseconds);
        mName = name;
    }

    public void addTurn(long startTimeInMilliseconds, long durationInMilliseconds) {
        startTimes.add(startTimeInMilliseconds);
        durations.add(durationInMilliseconds);
    }

    public char getGender() {
        return mGender;
    }

    public int compareTo(Speaker s) {
        // e.compareTo(null) should throw a NullPointerException
        // http://docs.oracle.com/javase/7/docs/api/java/lang/Comparable.html
        if (s == null) throw new NullPointerException();
        if (getDuration() > s.getDuration()) {
            return 1;
        } else if (getDuration() == s.getDuration()) {
            return mName.compareTo(s.mName);
        } else {
            return -1;
        }
    }

    public String getName() {
        return mName;
    }

    public long getDuration() {
        long totalDurationInMilliseconds = 0;
        for (long duration : durations) {
            totalDurationInMilliseconds += duration;
        }
        return totalDurationInMilliseconds;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
