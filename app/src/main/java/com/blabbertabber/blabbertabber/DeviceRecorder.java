package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

/**
 * Created by cunnie on 10/3/15.
 * <p/>
 * Class that works with a REAL microphone (non-emulator)
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
