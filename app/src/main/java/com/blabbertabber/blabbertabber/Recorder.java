package com.blabbertabber.blabbertabber;

/**
 * Created by Cunnie on 9/16/15.
 * <p/>
 * Class that returns speaker and volume
 * This is a throw-away class that will be replaced by actual speaker diarization software
 * which will be some type of service
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;

public abstract class Recorder implements Runnable {
    static final public String RECORD_RESULT = "com.blabbertabber.blabbertabber.RecordingService.REQUEST_PROCESSED";
    static final public String RECORD_MESSAGE = "com.blabbertabber.blabbertabber.RecordingService.RECORD_MSG";
    protected static final String TAG = "Recorder";
    private static final int MAX_SPEAKERS = 4;
    public int numSpeakers;
    private LocalBroadcastManager mBroadcastManager;
    private Context mContext;
    private int speaker;
    private long nextSpeakerChange;

    // Constructor
    public Recorder(Context context) {
        mContext = context;
        // speakers change on average every 5 seconds
        nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        numSpeakers = 1; // initially only one speaker
        Log.i(TAG, "Recorder(): nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
    }

    public void run() {
        Log.i(TAG, "run()");
        // https://developer.android.com/training/multiple-threads/define-runnable.html
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);

        // TODO:  Writer code to set up AudioRecord stuff
        // Loop, getting pack sliced of audio record data
        // while ...
        //   get volume from AudioRecord data
        //   Do mathematical logic to determine speaker id
        //   Notify listener(s) of speakerId and speakerVolume
        /*
            for (ResponseReceivedListener listener:listeners){
               listener.onResponseReceived(arg1, arg2);
            }
         */

        startRecording();
        for (int x = 0; x < 1500; x++) {
            try {
                sleep(25);
            } catch (InterruptedException e) {
                Log.i(TAG, "InterruptedException, return");
                e.printStackTrace();
                stopRecording();
                return; // <- avoids spawning many threads when changing orientation
            }
            Log.v(TAG, "run() tick");
            sendResult(getSpeakerId(), getSpeakerVolume());
        }
        stopRecording();
    }

    public void sendResult(int id, int volume) {
        Intent intent = new Intent(RECORD_RESULT);
        intent.putExtra(RECORD_MESSAGE, new int[]{id, volume});
        mBroadcastManager.sendBroadcast(intent);
    }

    protected abstract void startRecording();

    protected abstract void stopRecording();

    // Who is currently speaking?
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
    private boolean newSpeaker() {
        double p = ((MAX_SPEAKERS - numSpeakers) / (MAX_SPEAKERS - 1.0));
        Log.i(TAG, "newSpeaker(): " + p);
        return p > ThreadLocalRandom.current().nextDouble();
    }
}
