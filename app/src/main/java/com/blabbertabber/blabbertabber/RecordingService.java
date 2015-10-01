package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    private final IBinder mBinder = new RecordingBinder();
    private Thread mThreadSAVR;
    private SpeakerAndVolumeRunnable mSpeakerAndVolumeRunnable = new SpeakerAndVolumeRunnable(this);

    public RecordingService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        // make sure we're not spawning another thread if we already have one. We're being
        // overly cautious; in spite of frequent testing, this if-block always succeeds.
        if (mThreadSAVR == null || !mThreadSAVR.isAlive()) {
            mThreadSAVR = new Thread(mSpeakerAndVolumeRunnable);
            mThreadSAVR.start();
        }
    }

    @Override
    public void onDestroy() {
        if (mThreadSAVR != null) {
            Log.i(TAG, "onDestroy() mThreadSAVR == " + mThreadSAVR.getName());
            mThreadSAVR.interrupt();
        } else {
            Log.i(TAG, "onDestroy() mThreadSAVR == null");
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