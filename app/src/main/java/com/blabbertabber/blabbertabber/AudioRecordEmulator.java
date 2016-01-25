package com.blabbertabber.blabbertabber;

import android.util.Log;

/**
 * Created by brendancunnie on 1/24/16.
 */
public class AudioRecordEmulator extends AudioRecordAbstract {
    static final String TAG = "AudioRecordEmulator";
    private AudioEventProcessor audioEventProcessor;
    Thread notifier;

    public AudioRecordEmulator(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {

    }

    @Override
    protected void stop() {
        notifier.interrupt();
    }

    @Override
    public void release() {
        notifier.interrupt();
    }

    @Override
    public void setRecordPositionUpdateListener(AudioEventProcessor audioEventProcessor) {
        this.audioEventProcessor = audioEventProcessor;
    }

    @Override
    public int setPositionNotificationPeriod(int numFrames) {
        return 0;
    }

    @Override
    public void startRecording() {
        Log.i(TAG, "startRecording()   About to creat and start thread.");

        /// start new thread
        notifier = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.currentThread().sleep(200);
                        audioEventProcessor.onPeriodicNotificationEmulator();
                    } catch (InterruptedException e) {
                        Log.i(TAG, "run()   Huh.  InterruptedException thrown while sleep()ing.");
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };
        notifier.start();
    }
}
