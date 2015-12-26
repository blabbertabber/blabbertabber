package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Do we really need to do this?
 * This class spawns the DeviceRecorder or EmulatorRecorder thread.  But can that thread be
 * spawned just as easily in RecordingActivity?
 */
public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    private final IBinder mBinder = new RecordingBinder();
    private Thread mThreadRecorder;
    // emulator crashes if attempts to use the actual microphone, so we simulate microphone in EmulatorRecorder
    private Recorder mRecorder = "goldfish".equals(Build.HARDWARE) ? new EmulatorRecorder(this) : new DeviceRecorder(this);
    // http://www.ibm.com/developerworks/java/library/j-jtp06197/index.html
    // 'volatile' because it will be accessed across threads, "volatile reads are cheap"
    // read-mostly, only written when the recording is paused.
    // I realize that I'm making a global variable to show recording state, but, hey,
    // whether the app is recording is a global condition. Sorry, zealots, pander your dogma elsewhere.
    public static volatile boolean recording = true;
    public static volatile boolean reset = false;

    public RecordingService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        // make sure we're not spawning another thread if we already have one. We're being
        // overly cautious; in spite of frequent testing, this if-block always succeeds.
        if (mThreadRecorder == null || !mThreadRecorder.isAlive()) {
            mThreadRecorder = new Thread(mRecorder);
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
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class RecordingBinder extends Binder {
        RecordingService getService() {
            return RecordingService.this;
        }
    }
}