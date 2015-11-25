package com.blabbertabber.blabbertabber;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

/**
 * Created by Cunnie on 10/8/15.
 * http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 */

// BUGS: uses 'static' which makes testing much more difficult, for example we don't test that
//    release() calls MediaRecorder.reset() instead of MediaRecorder.release();

public class TheAudioRecord extends AudioRecord {
    private static final String TAG = "TheAudioRecord";
    private static final int RECORDER_AUDIO_SOURCE = BestMicrophone.getBestMicrophone();
    // http://developer.android.com/reference/android/media/AudioRecord.html
    // "44100Hz is currently the only rate that is guaranteed to work on all devices"
    // 16k samples/sec * 2 bytes/sample = 32kB/sec == 115.2 MB/hour
    private static final int RECORDER_SAMPLE_RATE_IN_HZ = 16_000;
    private static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int PERIOD_IN_FRAMES = RECORDER_SAMPLE_RATE_IN_HZ / 10; // ten periods/sec
    // 1 channel (mono), 2 bytes per sample (PCM 16-bit)
    private static final int RECORDER_BUFFER_SIZE_IN_BYTES = PERIOD_IN_FRAMES * 1 * 2;
    public static TheAudioRecord singleton;
    // Stuff needed for getMaxAmplitude()
    // http://stackoverflow.com/questions/15804903/android-dev-audiorecord-without-blocking-or-threads
    private static short[] AUDIO_DATA = new short[RECORDER_BUFFER_SIZE_IN_BYTES / 2];  // 2 => 1 x PCM 16 / 2 bytes
    private int maxAmplitude = Short.MIN_VALUE;

    protected TheAudioRecord(
            int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        // Exists only to defeat instantiation.
        super(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        Log.i(TAG, "TheAudioRecord() bufferSizeInBytes == " + bufferSizeInBytes);
    }

    public synchronized static TheAudioRecord getInstance() {
        if (singleton == null) {
            singleton = new TheAudioRecord(RECORDER_AUDIO_SOURCE, RECORDER_SAMPLE_RATE_IN_HZ,
                    RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT, RECORDER_BUFFER_SIZE_IN_BYTES);
        }
        return singleton;
    }

    @Override
    public void startRecording() {
        // To avoid `java.lang.IllegalStateException: startRecording() called on an uninitialized AudioRecord`
        // we try several times to start before giving up
        for (int i = 0; i < 5; i++) {
            try {
                super.startRecording();
                return;
            } catch (IllegalStateException e) {
                Log.i(TAG, "startRecording() caught IllegalStateException");
                // placate Android Studio who always wants to wrap sleep() in try/catch
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        throw new IllegalStateException("startRecording() has failed to start");
    }

    @Override
    public void release() {
        // developer.android.com/reference/android/media/AudioRecord.html
        // " The object can no longer be used and the reference should be set to null after a call to release()"
        super.release();
        singleton = null;
    }

    public int getMaxAmplitude() {
        // we had to write our own when we switched from MediaRecorder to AudioRecord
        maxAmplitude = Short.MIN_VALUE;
        int readSize = read(AUDIO_DATA, 0, AUDIO_DATA.length);
        for (int i = 0; i < readSize; i++) {
            if (AUDIO_DATA[i] > maxAmplitude) {
                maxAmplitude = AUDIO_DATA[i];
            }
        }
        Log.i(TAG, "getMaxAmplitude() readsize: " + readSize + " maxAmplitude " + maxAmplitude);
        return maxAmplitude;
    }
}
