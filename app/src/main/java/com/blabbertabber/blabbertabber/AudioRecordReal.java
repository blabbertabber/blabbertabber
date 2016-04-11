package com.blabbertabber.blabbertabber;

import android.media.AudioRecord;
import android.util.Log;

/**
 * Created by brendancunnie on 1/24/16.
 */
public class AudioRecordReal extends AudioRecordAbstract {
    private static final String TAG = "AudioRecordReal";
    private static AudioRecord audioRecord;
    private static AudioRecordReal theAudioRecordReal;
    private static int recorderAudioSource;
    private static int recorderSampleRateInHz;
    private static int recorderChannelConfig;
    private static int recorderAudioFormat;
    private static int recorderBufferSizeInBytes;
    private static AudioEventProcessor audioEventProcessor;
    private static int numFrames;

    private AudioRecordReal() {
    }

    public static synchronized AudioRecordReal getInstance(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {
        AudioRecordReal.recorderAudioSource = recorderAudioSource;
        AudioRecordReal.recorderSampleRateInHz = recorderSampleRateInHz;
        AudioRecordReal.recorderChannelConfig = recorderChannelConfig;
        AudioRecordReal.recorderAudioFormat = recorderAudioFormat;
        AudioRecordReal.recorderBufferSizeInBytes = recorderBufferSizeInBytes;
        if (audioRecord == null) {
            audioRecord = new AudioRecord(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes);
        }
        if (theAudioRecordReal == null) {
            theAudioRecordReal = new AudioRecordReal();
        }
        return theAudioRecordReal;
    }

    @Override
    public synchronized void stopAndRelease() {
        audioRecord.stop();
        audioRecord.release();
        audioRecord = new AudioRecord(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes);
        audioRecord.setRecordPositionUpdateListener(audioEventProcessor);
        audioRecord.setPositionNotificationPeriod(numFrames);
        Log.i(TAG, "stopAndRelease() audioRecord: " + audioRecord);
    }

    @Override
    public void setRecordPositionUpdateListener(AudioEventProcessor audioEventProcessor) {
        AudioRecordReal.audioEventProcessor = audioEventProcessor;
        audioRecord.setRecordPositionUpdateListener(audioEventProcessor);
    }

    @Override
    public int setPositionNotificationPeriod(int numFrames) {
        AudioRecordReal.numFrames = numFrames;
        return audioRecord.setPositionNotificationPeriod(numFrames);
    }

    @Override
    public void startRecording() {
        Log.i(TAG, "startRecording() audioRecord: " + audioRecord + " state: "
                + audioRecord.getState() + " recordingState: " + audioRecord.getRecordingState());
        // TODO get rid of this! this masks a bug
        try {
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "startRecording() " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public int read(short[] audioData, int offsetInShorts, int sizeInShorts) {
        return audioRecord.read(audioData, offsetInShorts, sizeInShorts);
    }

}
