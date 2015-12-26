package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * AudioRecordWrapper of the AudioRecord (with more consistent method names), exposing
 * only the needed methods.
 * Class that works with a REAL microphone (non-emulator).
 * This class inherits from a Runnable that Recording Service starts when it is created.
 */
public class DeviceRecorder extends Recorder {
    public DeviceRecorder(Context context) {
        super(context);
    }

    @Override
    protected void pause() {
        Log.i(TAG, "pause()");
        AudioRecordWrapper.stop();
    }

    @Override
    protected void start() {
        Log.i(TAG, "start()");
        // make sure mRecorder is fresh; if it's stale we'll get a
        // `java.lang.IllegalStateException: startRecording() called on an uninitialized AudioRecord.`
        AudioRecordWrapper.startRecording();
    }

    @Override
    protected void stop() {
        Log.i(TAG, "stop()");
        AudioRecordWrapper.stop();
        AudioRecordWrapper.close();
        // `java.lang.IllegalStateException: startRecording() called on an uninitialized AudioRecord.`
    }

    @Override
    public boolean isRecording() {
        return AudioRecordWrapper.isRecording();
    }

    @Override
    public int getSpeakerVolume() throws IOException {
        int volume = AudioRecordWrapper.getMaxAmplitude();
        Log.v(TAG, "getSpeakerVolume() volume: " + volume);
        return volume;
    }
}
