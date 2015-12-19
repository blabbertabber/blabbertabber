package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Tracks the duration information for each speaker.
 * Implements Comparable to facilitate sorting.
 */
public class Speaker implements Comparable<Speaker> {
    private static final String TAG = "Speaker";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private int color;
    private long mTotalSpeakingTime = 0; // milliseconds
    private Date mStartDate = null;
    private boolean mSpoke = false;

    private String mName = null;

    // constructor for setting name, preferred constructor
    public Speaker(String name) {
        mName = name;
    }

    // constructor that allows injecting totalSpeakingTime and name; meant for tests exclusively
    public Speaker(String name, long totalSpeakingTime) {
        mName = name;
        mTotalSpeakingTime = totalSpeakingTime;
    }

    public void startSpeaking() {
        mSpoke = true;
        if (mStartDate == null) {
            mStartDate = new Date();
            Log.v(TAG, "start time: " + format.format(mStartDate) + " mTotalSpeakingTime: " + mTotalSpeakingTime);
        }
    }

    public void stopSpeaking() {
        if (mStartDate == null) {
            Log.wtf(TAG, "WTF?  It should be impossible to call stopSpeaking() before calling startSpeaking().");
            return;
        }
        long stopTime = new Date().getTime();
        mTotalSpeakingTime += (stopTime - mStartDate.getTime());
        mStartDate = null;
        Log.v(TAG, "stop time: " + mStartDate + " mTotalSpeakingTime: " + mTotalSpeakingTime);
    }

    public int compareTo(Speaker s) {
        // e.compareTo(null) should throw a NullPointerException
        // http://docs.oracle.com/javase/7/docs/api/java/lang/Comparable.html
        if (s == null) throw new NullPointerException();
        if (duration() > s.duration()) {
            return 1;
        } else if (duration() == s.duration()) {
            return mName.compareTo(s.mName);
        } else {
            return -1;
        }
    }

    public String getName() {
        return mName;
    }

    public long duration() {
        return mTotalSpeakingTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
