package com.blabbertabber.blabbertabber;

/**
 * Abstraction of AudioRecord
 */
public abstract class AudioRecordAbstract {

    public AudioRecordAbstract() {
    }

    protected abstract void stop();     // stops the recording, closes file, cannot resume after stop().

    public abstract void release();

    public abstract void setRecordPositionUpdateListener(AudioEventProcessor audioEventProcessor);

    public abstract int setPositionNotificationPeriod(int numFrames);

    public abstract void startRecording();
}
