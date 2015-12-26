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
 * release() calls AudioRecord.release() instead of MediaRecorder.release();
 * <p/>
 * We adhere to AudioRecord's asymmetric naming conventions (startRecording() vs. stop()),
 * but we plan to symmetricize them in any calling class (e.g. DeviceRecorder)
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
    private static final int PERIOD_IN_FRAMES = RECORDER_SAMPLE_RATE_IN_HZ / 5; // five periods/sec
    // 1 channel (mono), 2 bytes per sample (PCM 16-bit)
    private static final int RECORDER_BUFFER_SIZE_IN_BYTES = PERIOD_IN_FRAMES * 1 * 2;
    private static final String BLABBERTABBER_DIRECTORY = Environment.getExternalStorageDirectory() + "/BlabberTabber/";
    public static final String RECORDER_RAW_FILENAME = BLABBERTABBER_DIRECTORY + "meeting.raw";
    public static TheAudioRecord singleton;
    // Stuff needed for getMaxAmplitude()
    // http://stackoverflow.com/questions/15804903/android-dev-audiorecord-without-blocking-or-threads
    private static short[] AUDIO_DATA = new short[RECORDER_BUFFER_SIZE_IN_BYTES / 2];  // 2 => 1 x PCM 16 / 2 bytes
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
            TheAudioRecord temp;
            temp = new TheAudioRecord(RECORDER_AUDIO_SOURCE, RECORDER_SAMPLE_RATE_IN_HZ,
                    RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT, RECORDER_BUFFER_SIZE_IN_BYTES);

            File file = new File(BLABBERTABBER_DIRECTORY);

            if (file.exists()) {
                Log.i(TAG, "getInstance() " + BLABBERTABBER_DIRECTORY + " already exists");
            } else if (file.mkdirs()) {
                Log.i(TAG, "getInstance() " + BLABBERTABBER_DIRECTORY + " created");
            } else {
                Log.i(TAG, "getInstance() could not create " + BLABBERTABBER_DIRECTORY + ".");
            }
            // open file for writing "/sdcard/BlabberTabber"; create dir if necessary
            File rawFile = new File(RECORDER_RAW_FILENAME);
            if (mRawDataOutputStream == null) {
                try {
                    mRawDataOutputStream = new DataOutputStream(new FileOutputStream(rawFile));
                } catch (java.io.FileNotFoundException e) {
                    Log.wtf(TAG, "Could not open FileOutputStream " + RECORDER_RAW_FILENAME +
                            " with message " + e.getMessage());
                }
            }
            singleton = temp;
            Log.i(TAG, "getInstance() singleton created: " + singleton);
        }
        return singleton;
    }

    @Override
    public void startRecording() {
        super.startRecording();
        Log.i(TAG, "startRecording()");
    }

    /**
     * We stop the recording, but we do NOT close the file in the event that the user
     * decides to resume the recording later. This is more of a "pause" than a "stop"
     */
    @Override
    public void stop() {
        super.stop();
        Log.i(TAG, "stop()");
    }

    /**
     * If the user calls the release, then the recording is over, so we close the file
     * and nullify the AudioRecord.
     * <p/>
     * developer.android.com/reference/android/media/AudioRecord.html
     * "The object can no longer be used and the reference should be set to null after a call to release()"
     */
    @Override
    public void release() {
        super.release();
        Log.i(TAG, "release()");
        try {
            mRawDataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRawDataOutputStream = null; // to avoid pesky java.io.IOException: write failed: EBADF (Bad file descriptor)
        singleton = null;
        getInstance();  // recreate the singleton to avoid `java.lang.NullPointerException: Attempt
        // to invoke virtual method 'boolean com.blabbertabber.blabbertabber.TheAudioRecord.isRecording()'`
    }

    public boolean isRecording() {
        return (getRecordingState() == RECORDSTATE_RECORDING);
    }

    // We are temporarily writing a .raw file out for postprocessing.
    // A subsequent version will not perform this intermediate step.

    /**
     * @return int  The maximum volume over the most recent section of time.
     * The range is that of a signed short.
     * @throws IOException if it can't write to the file; it's unintuitive that the exception is
     * thrown in getMaxAmplitude() (i.e. "what does getMaxAmplitude() have to do with writing a file?")
     */
    public int getMaxAmplitude() throws IOException {
        Log.v(TAG, "getMaxAmplitude()");
        int maxAmplitude = Short.MIN_VALUE;
        // if our singleton is null, then we skip writing to mRawDataOutputStream to avoid
        // `java.lang.NullPointerException: Attempt to invoke virtual method 'void java.io.DataOutputStream.write(byte[], int, int)' on a null object reference`
        if ( singleton != null ) {
            int readSize = read(AUDIO_DATA, 0, AUDIO_DATA.length);
            // if readSize is negative, it most likely means that we have an ERROR_INVALID_OPERATION (-3)
            if (readSize > 0) {
                byte[] rawAudio = new byte[AUDIO_DATA.length * 2];

                // Performance: we must copy the PCM data into an array of bytes so that
                // we can write the RawDataOutputStream in one shot; otherwise it can take
                // 2x longer than the sample time to write (i.e. we drop 1/2 the sound)
                // if we foolishly use writeShort() instead
                for (int i = 0; i < readSize; i++) {
                    // http://developer.android.com/reference/android/media/AudioFormat.html
                    // "...when the short is stored in a ByteBuffer, it is native endian (as compared to the default Java big endian)."
                    // However the following lines seem to work both on ARM (big endian) and x86_64 emulator (little endian)
                    rawAudio[i * 2] = (byte) (AUDIO_DATA[i] >> 8);
                    rawAudio[i * 2 + 1] = (byte) AUDIO_DATA[i];
                    if (AUDIO_DATA[i] > maxAmplitude) {
                        maxAmplitude = AUDIO_DATA[i];
                    }
                }
                mRawDataOutputStream.write(rawAudio, 0, readSize * 2);
                Log.v(TAG, "getMaxAmplitude() readsize: " + readSize + " maxAmplitude " + maxAmplitude);
            } else {
                Log.i(TAG, "getMaxAmplitude() NEGATIVE readsize: " + readSize + " maxAmplitude " + maxAmplitude);
            }
        } else {
            Log.v(TAG, "getMaxAmplitude() maxAmplitude " + maxAmplitude);
        }
        return maxAmplitude;
    }
}
