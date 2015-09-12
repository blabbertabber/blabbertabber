package com.blabbertabber.blabbertabber;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class RecordingService extends Service {
    private static final String TAG = "RecordingService";
    final Messenger myMessenger = new Messenger(new Handler());

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
        return myMessenger.getBinder();
    };
}
