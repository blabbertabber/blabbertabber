package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by cunnie on 10/3/15.
 * Class that works with an emulator (no microphone)
 */
public class EmulatorRecorder extends Recorder {

    public EmulatorRecorder(Context context) {
        super(context);
    }

    @Override
    protected void startRecording() {
        Log.i(TAG, "startRecording()");
    }

    @Override
    protected void stopRecording() {
        Log.i(TAG, "stopRecording()");
    }

    @Override
    public int getSpeakerVolume() {
        return ThreadLocalRandom.current().nextInt(0, 100);
    }
}
