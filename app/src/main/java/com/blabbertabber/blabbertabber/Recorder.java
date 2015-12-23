package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;

/**
 * Class that returns speaker and volume.
 * This is executed as a thread from RecordingService.
 */
public abstract class Recorder implements Runnable {
    static final public String RECORD_STATUS = "com.blabbertabber.blabbertabber.RecordingService.RECORD_STATUS";
    static final public String RECORD_RESULT = "com.blabbertabber.blabbertabber.RecordingService.RECORD_RESULT";
    static final public String RECORD_STATUS_MESSAGE = "com.blabbertabber.blabbertabber.RecordingService.RECORD_STATUS_MESSAGE";
    static final public String RECORD_MESSAGE = "com.blabbertabber.blabbertabber.RecordingService.RECORD_MESSAGE";
    static final public int UNKNOWN_STATUS = -1;
    static final public int MICROPHONE_UNAVAILABLE = -2;
    protected static final String TAG = "Recorder";
    public int numSpeakers;
    private LocalBroadcastManager mBroadcastManager;
    private Context mContext;
    private int speaker;
    private long nextSpeakerChange;

    public Recorder(Context context) {
        mContext = context;
        // speakers change on average every 5 seconds
        nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        numSpeakers = 1; // initially only one speaker
        Log.i(TAG, "Recorder()");
    }

    /**
     * Start the recording, and broadcast the speaker volume via Intent RECORD_RESULT.
     * This is called by RecordingService on RecordingService's creation.
     */
    public void run() {
        Log.i(TAG, "run() STARTING Thread ID " + Thread.currentThread().getId());
        // https://developer.android.com/training/multiple-threads/define-runnable.html
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);

        start();
        try {
            while (true) {
                if (RecordingService.reset) {
                    Log.i(TAG, "run() resetting");
                    stop();
                    RecordingService.reset = false; // re-cock the trigger
                } else if (RecordingService.recording) {
                    if (isRecording()) {
                        // sample at twice the AudioRecord buffer size or else choppiness
                        sleep(50);
                        Log.v(TAG, "run() Thread ID " + Thread.currentThread().getId());
                        /// TODO: remove getSpeakerId()
                        sendResult(getSpeakerId(), getSpeakerVolume());
                    } else {
                        // RecordingService.recording has signalled us to begin recording
                        start();
                        Log.i(TAG, "run() resuming");
                        if (!isRecording()) {
                            // Something's wrong: we should be recording but we aren't; most likely
                            // another app has grabbed the microphone.
                            Log.v(TAG, "run() Thread ID " + Thread.currentThread().getId() + " NOT recording()");
                            sendStatus(MICROPHONE_UNAVAILABLE);
                            sleep(2500);
                        }
                    }
                } else if (isRecording()) {
                    // RecordingService.recording has signalled us to pause() recording
                    pause();
                    Log.i(TAG, "run() pausing");
                } else {
                    // we should be paused, we are paused, let's rest
                    sleep(100);
                }
            }
        } catch (InterruptedException e) {
            Log.i(TAG, "InterruptedException, return");
            e.printStackTrace();
            stop();
            Log.i(TAG, "run() STOPPING Thread ID " + Thread.currentThread().getId());
            return; // <- avoids spawning many threads when changing orientation
        }
    }

    /**
     * Utility method to broadcast the recorded volume.
     *
     * @param id     The id of the speaker speaking.
     * @param volume The volume.  0-32767 inclusive.
     */
    /// TODO: remove references to id.
    /// TODO: should we change volume range to signed short?
    public void sendResult(int id, int volume) {
        Intent intent = new Intent(RECORD_RESULT);
        intent.putExtra(RECORD_MESSAGE, new int[]{id, volume});
        mBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Utility method to broadcast the recorder status.
     *
     * @param status The status, e.g. MICROPHONE_UNAVAILABLE
     */
    public void sendStatus(int status) {
        Intent intent = new Intent(RECORD_STATUS);
        intent.putExtra(RECORD_STATUS_MESSAGE, status);
        mBroadcastManager.sendBroadcast(intent);
    }

    protected abstract void start();    // starts the recording, resumes recording

    protected abstract void pause();    // pauses the recording

    protected abstract void stop();     // stops the recording, closes file, cannot resume after stop().

    public abstract boolean isRecording();

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

    public abstract int getSpeakerVolume();

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
}
