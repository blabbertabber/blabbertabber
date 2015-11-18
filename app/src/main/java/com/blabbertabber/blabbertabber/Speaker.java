package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cunnie on 11/4/15.
 */

public class Speaker implements Comparable<Speaker> {
    private static final String TAG = "Speaker";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private int color;
    private int visible;
    private int viewID;
    private long mTotalSpeakingTime = 0; // milliseconds
    private Date mStartDate = null;
    private boolean mSpoke = false;

    public Speaker() {
    }

    // constructor that allows injecting totalSpeakingTime; good for tests
    public Speaker(long totalSpeakingTime) {
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
            return 0;
        } else {
            return -1;
        }
    }

    public int isVisible() {
        return visible;
    }

    public long duration() {
        return mTotalSpeakingTime;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }

    public boolean getSpoke() {
        return mSpoke;
    }
}
