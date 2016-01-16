package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by brendancunnie on 1/16/16.
 */
public class AudioEventProcessor implements Runnable, AudioRecord.OnRecordPositionUpdateListener {
    static final public String RECORD_STATUS = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_STATUS";
    static final public String RECORD_RESULT = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_RESULT";
    static final public String RECORD_STATUS_MESSAGE = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_STATUS_MESSAGE";
    static final public String RECORD_MESSAGE = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_MESSAGE";
    static final public int UNKNOWN_STATUS = -1;
    static final public int MICROPHONE_UNAVAILABLE = -2;
    static final public int CANT_WRITE_MEETING_FILE = -3;
    private static final String TAG = "AudioEventProcessor";
    private static final int NUM_FRAMES = 16000 / 10;  // 10 updates/second
    private Context context;
    private OutputStream rawFileOutputStream;
    private LocalBroadcastManager mBroadcastManager;
    /// TODO remove these variables.
    private int speaker;
    private long nextSpeakerChange;
    public int numSpeakers;

    public AudioEventProcessor(Context context) {
        this.context = context;
        File sharedDir = context.getFilesDir();
        try {
            rawFileOutputStream = context.openFileOutput(sharedDir.getAbsolutePath() + "/meeting.raw", Context.MODE_WORLD_WRITEABLE);
        } catch (FileNotFoundException e) {
            Log.wtf(TAG, "FileNotFoundException thrown on file " + sharedDir.getAbsolutePath() + "/meeting.raw");
            e.printStackTrace();
        }
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        /// TODO remove
        nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        numSpeakers = 1;
    }

    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        short buffer[] = new short[NUM_FRAMES];
        int readSize = recorder.read(buffer, 0, NUM_FRAMES);
        short maxAmplitude = 0;

        if (!RecordingService.recording) {
            return;
        }
        // if readSize is negative, it most likely means that we have an ERROR_INVALID_OPERATION (-3)
        if (readSize > 0) {
            byte[] rawAudio = new byte[readSize * 2];
            // Performance: we must copy the PCM data into an array of bytes so that
            // we can write the RawDataOutputStream in one shot; otherwise it can take
            // 2x longer than the sample time to write (i.e. we drop 1/2 the sound)
            // if we foolishly use writeShort() instead
            for (int i = 0; i < readSize; ++i) {
                // http://developer.android.com/reference/android/media/AudioFormat.html
                // "...when the short is stored in a ByteBuffer, it is native endian (as compared to the default Java big endian)."
                // However the following lines seem to work both on ARM (big endian) and x86_64 emulator (little endian)
                rawAudio[i * 2] = (byte) (buffer[i] >> 8);
                rawAudio[i * 2 + 1] = (byte) buffer[i];
                if (buffer[i] > maxAmplitude) {
                    maxAmplitude = buffer[i];
                }
            }
            Log.v(TAG, "onPeriodicNotification() readsize: " + readSize + " maxAmplitude " + maxAmplitude);
            // write data out to raw file
            try {
                rawFileOutputStream.write(rawAudio, 0, readSize * 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "onPeriodicNotification() NEGATIVE readsize: " + readSize);
        }
        // send max volume to RecordingActivity
        /// Refactor speakerId
        sendVolume(getSpeakerId(), maxAmplitude);
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
    }

    /**
     * Utility method to broadcast the recorded volume.
     *
     * @param id     The id of the speaker speaking.
     * @param volume The volume.  0-32767 inclusive.
     */
    /// TODO: remove references to id.
    /// TODO: should we change volume range to signed short?
    public void sendVolume(int id, int volume) {
        Intent intent = new Intent(RECORD_RESULT);
        intent.putExtra(RECORD_MESSAGE, new int[]{id, volume});
        mBroadcastManager.sendBroadcast(intent);
    }


    // Who is currently speaking?
    /// TODO: Remove this method.
    public int getSpeakerId() {
        if (System.currentTimeMillis() > nextSpeakerChange) {
            speaker = nextSpeaker();
            nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        }
        Log.v(TAG, "getSpeaker(): " + speaker + " nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
        return speaker;
    }

    // usually returns a speaker different than the current speaker, possibly a new speaker
    /// TODO: remove this method.
    private int nextSpeaker() {
        if (newSpeaker()) {
            int nextSpeaker = numSpeakers;
            numSpeakers += 1;
            return (nextSpeaker);
        }
        // I am quite proud of the following line's elegance
        int newSpeaker = (speaker + ThreadLocalRandom.current().nextInt(0, numSpeakers)) % numSpeakers;
        Log.i(TAG, "nextSpeaker(): " + newSpeaker);
        return newSpeaker;
    }

    // Are we adding a completely new speaker who hasn't spoken yet?
    /// TODO: remove this method.
    private boolean newSpeaker() {
        double p = ((TheSpeakers.MAX_SPEAKERS - numSpeakers) / (TheSpeakers.MAX_SPEAKERS - 1.0));
        Log.i(TAG, "newSpeaker(): " + p);
        return p > ThreadLocalRandom.current().nextDouble();
    }

    @Override
    public void run() {
        Log.i(TAG, "run() STARTING Thread ID " + Thread.currentThread().getId());
        AudioRecord ar = new AudioRecord();
        ar.setRecordPositionUpdateListener(audioEventProcessor);
        ar.setPositionNotificationPeriod(MARKER_IN_FRAMES);


    }
}
