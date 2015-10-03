package com.blabbertabber.blabbertabber;

import android.content.Context;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by cunnie on 10/3/15.
 */
public class EmulatorRecorder extends Recorder {

    public EmulatorRecorder(Context context) {
        super(context);
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    public int getSpeakerVolume() {
        return ThreadLocalRandom.current().nextInt(0, 100);
    }
}
