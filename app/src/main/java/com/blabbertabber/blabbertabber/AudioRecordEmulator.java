package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.util.Random;

/**
 * Created by brendancunnie on 1/24/16.
 */
public class AudioRecordEmulator extends AudioRecordAbstract {
    static final String TAG = "AudioRecordEmulator";
    private Thread notifier;
    private AudioEventProcessor audioEventProcessor;
    private short[] mRandomAudioData = new short[AudioEventProcessor.NUM_FRAMES];

    public AudioRecordEmulator(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {
        // We create a random buffer of sound to emulate the real AudioRecord
        Random random = new Random();
        for (int i = 0; i < mRandomAudioData.length; i++) {
            mRandomAudioData[i] = (short) (random.nextInt(Short.MAX_VALUE * 2) - Short.MAX_VALUE);
        }
    }

    @Override
    protected void stopAndRelease() {
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
                        Thread.currentThread();
                        sleep(1000 / AudioEventProcessor.UPDATES_PER_SECOND);
                        audioEventProcessor.onPeriodicNotification(mRandomAudioData);
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
