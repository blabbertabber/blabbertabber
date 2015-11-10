package com.blabbertabber.blabbertabber;

import java.util.Date;

/**
 * Created by cunnie on 11/4/15.
 */
public class Speaker {
    private int color;
    private int visible;
    private int viewID;
    private long mTotalSpeakingTime = 0; // milliseconds
    private long mStartTime;

    public Speaker() {
    }

    public void startSpeaking() {
        mStartTime = new Date().getTime();
    }

    public void stopSpeaking() {
        mTotalSpeakingTime += new Date().getTime() - mStartTime;
    }

    public int isVisible() {
        return visible;
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
}
