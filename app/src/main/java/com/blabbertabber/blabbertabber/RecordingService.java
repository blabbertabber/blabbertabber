package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    ////    final Messenger myMessenger = new Messenger(new Handler());
    private final IBinder mBinder = new RecordingBinder();
    private SpeakerAndVolumeRunnable mSpeakerAndVolumeRunnable = new SpeakerAndVolumeRunnable(this);

    public RecordingService() {
    }

    @Override
    public void onCreate() {
        Log.wtf(TAG, "onCreate()");
        new Thread(mSpeakerAndVolumeRunnable).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int getSpeakerId() {
        return mSpeakerAndVolumeRunnable.getSpeakerId();
    }

    public int getSpeakerVolume() {
        return mSpeakerAndVolumeRunnable.getSpeakerVolume();
    }

    public class RecordingBinder extends Binder {
        RecordingService getService() {
            return RecordingService.this;
        }
    }
}