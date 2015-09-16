package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import java.util.Random;


public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    final Messenger myMessenger = new Messenger(new Handler());
    private final IBinder mBinder = new RecordingBinder();
    private Random randomGenerator = new Random();
    private SpeakerAndVolume mSpeakerAndVolume = new SpeakerAndVolume();

    public class RecordingBinder extends Binder {
        RecordingService getService() {
            return RecordingService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.wtf(TAG, "onCreate()");
    }

    public RecordingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    };

    public int getSpeakerId() {
        return mSpeakerAndVolume.getSpeakerId();
    }

    public int getSpeakerVolume() {
        return mSpeakerAndVolume.getSpeakerVolume();
    }
}
