package com.blabbertabber.blabbertabber;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Tracks the duration information for each speaker.
 * Implements Comparable to facilitate sorting.
 */
public class Speaker implements Comparable<Speaker> {
    private static final String TAG = "Speaker";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private int color;
    private ArrayList<Long> startTimes = new ArrayList<Long>();  // features since start of meeting
    private ArrayList<Long> durations = new ArrayList<Long>();  // duration in features
    private Date mStartDate = null;
    private boolean mSpoke = false;

    private String mName = null;
    private char mGender = '\0';

    // constructor for setting name, preferred constructor
    public Speaker(String name, char gender) {
        mName = name;
        mGender = gender;
    }

    // constructor that allows injecting totalSpeakingTime and name; meant for tests exclusively
    public Speaker(String name, long totalSpeakingTime) {
        startTimes.add(0L);
        durations.add(totalSpeakingTime);
        mName = name;
    }

    public void addTurn(long startTime, long duration) {
        startTimes.add(startTime);
        durations.add(duration);
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
        long totalDuration = 0;
        for (long duration : durations) {
            totalDuration += duration;
        }
        return totalDuration;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
