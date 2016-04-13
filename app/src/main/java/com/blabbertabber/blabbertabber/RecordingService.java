package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * RecordingService continues to record even if another Activity is running
 */
public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    // http://www.ibm.com/developerworks/java/library/j-jtp06197/index.html
    // 'volatile' because it will be accessed across threads, "volatile reads are cheap"
    // read-mostly, only written when the recording is paused.
    // I realize that I'm making a global variable to show recording state, but, hey,
    // whether the app is recording is a global condition. Sorry, zealots, pander your dogma elsewhere.
    public static volatile boolean recording = false;
    public static volatile boolean reset = false;
    private final IBinder mBinder = new RecordingBinder();
    private Thread mThreadRecorder;

    public RecordingService() {
        Log.i(TAG, "RecordingService()   this: " + this);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        AudioEventProcessor audioEventProcessor = new AudioEventProcessor(this);
        // make sure we're not spawning another thread if we already have one. We're being
        // overly cautious; in spite of frequent testing, this if-block always succeeds.
        if (mThreadRecorder == null || !mThreadRecorder.isAlive()) {
            mThreadRecorder = new Thread(audioEventProcessor);
            mThreadRecorder.start();
        }
    }

    @Override
    public void onDestroy() {
        if (mThreadRecorder != null) {
            Log.i(TAG, "onDestroy() mThreadRecorder == " + mThreadRecorder.getName());
            mThreadRecorder.interrupt();
            mThreadRecorder = null; // allow GC to reap thread
        } else {
            Log.i(TAG, "onDestroy() mThreadRecorder == null");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() startId: " + startId + " flags: " + flags);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return mBinder;
    }

    public class RecordingBinder extends Binder {
    }
}