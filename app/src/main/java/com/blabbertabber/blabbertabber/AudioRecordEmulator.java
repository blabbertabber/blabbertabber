package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.util.Random;

/**
 * AudioRecord for testing on emulators with no hardware sound (e.g. on a Mac Pro emulator)
 */
public class AudioRecordEmulator extends AudioRecordAbstract {
    static final String TAG = "AudioRecordEmulator";
    private short[] randomAudioData;

    public AudioRecordEmulator(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {
        // We create a random buffer of sound to emulate the real AudioRecord
        Random random = new Random();
        randomAudioData = new short[AudioEventProcessor.NUM_FRAMES];
        for (int i = 0; i < randomAudioData.length; i++) {
            randomAudioData[i] = (short) (random.nextInt(Short.MAX_VALUE * 2) - Short.MAX_VALUE);
        }
    }

    @Override
    protected void stopAndRelease() {
        Log.i(TAG, "stopAndRelease()");
    }

    @Override
    public void startRecording() {
        Log.i(TAG, "startRecording()");
    }

    @Override
    public int read(short[] audioData, int offsetInShorts, int sizeInShorts) {
        try {
            System.arraycopy(randomAudioData, 0, audioData, offsetInShorts, sizeInShorts);
            Thread.sleep(1000 / AudioEventProcessor.UPDATES_PER_SECOND);
        } catch (InterruptedException e) {
            Log.i(TAG, "run() InterruptedException thrown while sleep()ing.");
            e.printStackTrace();
            return 0;
        }
        return sizeInShorts;
    }
}
