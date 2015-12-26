package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * Wrapper of the AudioRecord singleton (with more consistent method names), exposing
 * only the needed methods.
 * Class that works with a REAL microphone (non-emulator).
 * This class inherits from a Runnable that Recording Service starts when it is created.
 */
public class DeviceRecorder extends Recorder {
    private TheAudioRecord mRecorder = TheAudioRecord.getInstance();

    public DeviceRecorder(Context context) {
        super(context);
    }

    @Override
    protected void pause() {
        Log.i(TAG, "pause()");
        mRecorder.stop();
    }

    @Override
    protected void start() {
        Log.i(TAG, "start()");
        // make sure mRecorder is fresh; if it's stale we'll get a
        // `java.lang.IllegalStateException: startRecording() called on an uninitialized AudioRecord.`
        mRecorder = TheAudioRecord.getInstance();
        mRecorder.startRecording();
    }

    @Override
    protected void stop() {
        Log.i(TAG, "stop()");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = TheAudioRecord.getInstance(); // update the mRecorder to avoid
        // `java.lang.IllegalStateException: startRecording() called on an uninitialized AudioRecord.`
    }

    @Override
    public boolean isRecording() {
        return mRecorder.isRecording();
    }

    @Override
    public int getSpeakerVolume() throws IOException {
        int volume = mRecorder.getMaxAmplitude();
        Log.v(TAG, "getSpeakerVolume() volume: " + volume);
        return volume;
    }

    private TheAudioRecord getRecorder() {
        return TheAudioRecord.getInstance();
    }
}
