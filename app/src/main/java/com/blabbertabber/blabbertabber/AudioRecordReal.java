package com.blabbertabber.blabbertabber;

import android.media.AudioRecord;

/**
 * Created by brendancunnie on 1/24/16.
 */
public class AudioRecordReal extends AudioRecordAbstract {
    private static AudioRecord audioRecord;
    private static int recorderAudioSource;
    private static int recorderSampleRateInHz;
    private static int recorderChannelConfig;
    private static int recorderAudioFormat;
    private static int recorderBufferSizeInBytes;

    private AudioRecordReal() {
    }

    public AudioRecordReal(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {
        this.recorderAudioSource = recorderAudioSource;
        this.recorderSampleRateInHz = recorderSampleRateInHz;
        this.recorderChannelConfig = recorderChannelConfig;
        this.recorderAudioFormat = recorderAudioFormat;
        this.recorderBufferSizeInBytes = recorderBufferSizeInBytes;
        audioRecord = buildAudioRecord(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes);
    }

    private synchronized AudioRecord buildAudioRecord(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {
        return new AudioRecord(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes);
    }

    @Override
    protected void stop() {
        audioRecord.stop();
    }

    @Override
    public synchronized void release() {
        audioRecord.release();
        audioRecord = buildAudioRecord(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes);
    }

    @Override
    public void setRecordPositionUpdateListener(AudioEventProcessor audioEventProcessor) {
        audioRecord.setRecordPositionUpdateListener(audioEventProcessor);
    }

    @Override
    public int setPositionNotificationPeriod(int numFrames) {
        return audioRecord.setPositionNotificationPeriod(numFrames);
    }

    @Override
    public void startRecording() {
        audioRecord.startRecording();
    }
}
