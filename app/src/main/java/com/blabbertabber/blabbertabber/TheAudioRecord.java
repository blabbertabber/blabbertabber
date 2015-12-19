package com.blabbertabber.blabbertabber;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Singletonized version of AudioRecord.
 * Contained by DeviceRecorder.
 * <p/>
 * http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 * <p/>
 * BUGS: uses 'static' which makes testing much more difficult, for example we don't test that
 * release() calls MediaRecorder.reset() instead of MediaRecorder.release();
 */
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
    private static final String BLABBERTABBER_DIRECTORY = Environment.getExternalStorageDirectory() + "/BlabberTabber/";
    public static final String RECORDER_RAW_FILENAME = BLABBERTABBER_DIRECTORY + "meeting.raw";
    public static TheAudioRecord singleton;
    // Stuff needed for getMaxAmplitude()
    // http://stackoverflow.com/questions/15804903/android-dev-audiorecord-without-blocking-or-threads
    private static short[] AUDIO_DATA = new short[RECORDER_BUFFER_SIZE_IN_BYTES / 2];  // 2 => 1 x PCM 16 / 2 bytes
    private static File mRawFile;
    private static DataOutputStream mRawDataOutputStream;

    protected TheAudioRecord(
            int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        // Exists only to defeat instantiation.
        super(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        Log.i(TAG, "TheAudioRecord() bufferSizeInBytes == " + bufferSizeInBytes);
    }

    public synchronized static TheAudioRecord getInstance() {
        Log.i(TAG, "getInstance()");
        if (singleton == null) {
            singleton = new TheAudioRecord(RECORDER_AUDIO_SOURCE, RECORDER_SAMPLE_RATE_IN_HZ,
                    RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT, RECORDER_BUFFER_SIZE_IN_BYTES);

            File file = new File(BLABBERTABBER_DIRECTORY);

            if (file.exists()) {
                Log.i(TAG, "getInstance() " + BLABBERTABBER_DIRECTORY + " already exists");
            } else if (file.mkdirs()) {
                Log.i(TAG, "getInstance() " + BLABBERTABBER_DIRECTORY + " created");
            } else {
                Log.i(TAG, "getInstance() could not create " + BLABBERTABBER_DIRECTORY + ".");
            }
        }
        return singleton;
    }

    @Override
    public void startRecording() {
        Log.i(TAG, "startRecording()");
        super.startRecording();
        // open file for writing "/sdcard/BlabberTabber"; create dir if non-existing
        mRawFile = new File(RECORDER_RAW_FILENAME);
        try {
            mRawDataOutputStream = new DataOutputStream(new FileOutputStream(mRawFile));
        } catch (java.io.FileNotFoundException e) {
            Log.wtf(TAG, "Could not open FileOutputStream " + RECORDER_RAW_FILENAME +
                    " with message " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        Log.i(TAG, "stop()");
        super.stop();
        try {
            mRawDataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        // developer.android.com/reference/android/media/AudioRecord.html
        // " The object can no longer be used and the reference should be set to null after a call to release()"
        super.release();
        singleton = null;
    }

    public boolean isRecording() {
        return (getRecordingState() == RECORDSTATE_RECORDING);
    }

    // We are temporarily writing a .raw file out for postprocessing.
    // A subsequent version will not perform this intermediate step.

    /**
     * @return int  The maximum volume over the most recent section of time.
     * The range is that of a signed short.
     */
    public int getMaxAmplitude() {
        Log.i(TAG, "getMaxAmplitude()");
        int maxAmplitude = Short.MIN_VALUE;
        int readSize = read(AUDIO_DATA, 0, AUDIO_DATA.length);
        byte[] rawAudio = new byte[AUDIO_DATA.length * 2];

        // Performance: we must copy the PCM data into an array of bytes so that
        // we can write the RawDataOutputStream in one shot; otherwise it can take
        // 2x longer than the sample time to write (i.e. we drop 1/2 the sound)
        // if we foolishly use writeShort() instead
        for (int i = 0; i < readSize; i++) {
            // if we ever run on a little-endian processor (Intel) this might be a problem:
            rawAudio[i * 2] = (byte) (AUDIO_DATA[i] >> 8);
            rawAudio[i * 2 + 1] = (byte) AUDIO_DATA[i];
            if (AUDIO_DATA[i] > maxAmplitude) {
                maxAmplitude = AUDIO_DATA[i];
            }
        }
        try {
            mRawDataOutputStream.write(rawAudio, 0, readSize * 2);
        } catch (IOException e) {
            Log.wtf(TAG, "IOException thrown trying to write to file " + RECORDER_RAW_FILENAME
                    + " with message " + e.getMessage());
            e.printStackTrace();
        }
        Log.i(TAG,"getMaxAmplitude() readsize: "+readSize+" maxAmplitude "+maxAmplitude);
        return maxAmplitude;
    }
}
