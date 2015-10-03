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
import android.media.MediaRecorder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;

public class SpeakerAndVolumeRunnable implements Runnable {
    static final public String RECORD_RESULT = "com.blabbertabber.blabbertabber.RecordingService.REQUEST_PROCESSED";
    static final public String RECORD_MESSAGE = "com.blabbertabber.blabbertabber.RecordingService.RECORD_MSG";
    private static final String TAG = "SpeakerAndVolumeRunnabl";
    private static final int MAX_SPEAKERS = 4;
    public int numSpeakers;
    private LocalBroadcastManager mBroadcastManager;
    private Context mContext;
    private int speaker;
    private long nextSpeakerChange;
    private MediaRecorder mRecorder;
    private String mFileName = "/dev/null"; // search for audioRecordName() when ready to write to a file

    // Constructor
    public SpeakerAndVolumeRunnable(Context context) {
        mContext = context;
        // speakers change on average every 5 seconds
        nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        numSpeakers = 1; // initially only one speaker
        Log.i(TAG, "SpeakerAndVolumeRunnable(): nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
    }

    public void run() {
        Log.i(TAG, "run()");
        // https://developer.android.com/training/multiple-threads/define-runnable.html
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);

        // TODO:  Writer code to set up AudioRedord stuff
        // Loop, getting pack sliced of audio record data
        // while ...
        //   get volume from AudioRecord data
        //   Do mathematcal logic to determine speaker id
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
    }

    public void sendResult(int id, int volume) {
        Intent intent = new Intent(RECORD_RESULT);
        intent.putExtra(RECORD_MESSAGE, new int[]{id, volume});
        mBroadcastManager.sendBroadcast(intent);
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        //                    NEXUS 6 MediaRecorder.AudioSource.
        // kinda works:       CAMCORDER
        //                    VOICE_RECOGNITION
        // terrible:          DEFAULT
        //                    MIC
        //                    VOICE_COMMUNICATION
        // RuntimeException:  VOICE_UPLINK
        //                    REMOTE_SUBMIX
        //                    VOICE_CALL
        //                    VOICE_DOWNLINK
        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    // Who is currently speaking?
    public int getSpeakerId() {
        if (System.currentTimeMillis() > nextSpeakerChange) {
            speaker = nextSpeaker();
            nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        }
        Log.v(TAG, "getSpeaker(): " + speaker + " nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
        return speaker;
    }


//    public int getSpeakerVolume() {
//        return ThreadLocalRandom.current().nextInt(0, 100);
//    }

    public int getSpeakerVolume() {
        int volume = mRecorder.getMaxAmplitude();
        volume = volume * 100 / 32768;
        Log.i(TAG, "volume is " + volume);
        return volume;
    }

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
