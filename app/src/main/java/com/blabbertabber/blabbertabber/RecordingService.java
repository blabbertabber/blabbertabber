package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    final Messenger myMessenger = new Messenger(new Handler());
    private final IBinder mBinder = new RecordingBinder();

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
        return 2;
    }

    public int getSpeakerVolume() {
        return 3;
    }
}
