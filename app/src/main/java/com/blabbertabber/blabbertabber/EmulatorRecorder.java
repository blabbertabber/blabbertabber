package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that works with an emulator (no microphone)
 * It appears that the emulator on Macs that have builtin microphones, e.g. Brendan's
 * MacBook Pro, works fine with the emulator; however, the emulator on Macs that don't
 * have builtin microphones, e.g. Brian's Mac Pro, crash when they try to access the microphone.
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
    public boolean isRecording() {
        return true;
    }

    @Override
    public int getSpeakerVolume() {
        return ThreadLocalRandom.current().nextInt(0, 100);
    }
}
