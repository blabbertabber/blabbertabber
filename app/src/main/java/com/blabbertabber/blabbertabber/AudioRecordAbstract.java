package com.blabbertabber.blabbertabber;

/**
 * Abstraction of AudioRecord
 */
public abstract class AudioRecordAbstract {


    public AudioRecordAbstract() {
    }

    protected abstract void start();    // starts the recording, resumes recording

    protected abstract void stop();     // stops the recording, closes file, cannot resume after stop().

    public abstract void release();
}
