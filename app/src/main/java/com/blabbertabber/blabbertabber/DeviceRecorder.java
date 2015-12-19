package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

/**
 * Wrapper of the AudioRecord singleton (with more consistent method names), exposing
 * only the needed methods.
 * Class that works with a REAL microphone (non-emulator).
 * This class inherits from a Runnable that Recording Service starts when it is created.
 */
public class DeviceRecorder extends Recorder {
    private TheAudioRecord mRecorder;

    public DeviceRecorder(Context context) {
        super(context);
    }

    @Override
    protected void startRecording() {
        Log.i(TAG, "startRecording()");
        mRecorder = TheAudioRecord.getInstance();
        mRecorder.startRecording();
    }

    @Override
    protected void stopRecording() {
        Log.i(TAG, "stopRecording()");

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public boolean isRecording() {
        return mRecorder.isRecording();
    }

    @Override
    public int getSpeakerVolume() {
        int volume = mRecorder.getMaxAmplitude();
        volume = volume * 100 / 32768;
        Log.i(TAG, "getSpeakerVolume() volume is " + volume);
        return volume;
    }
}
