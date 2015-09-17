package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by cunnie on 8/16/15.
 * Activity to record and identify voices.
 */

public class RecordingActivity extends Activity {
    RecordingService mService;
    boolean mBound = false;

    private static final String TAG = "RecordingActivity";
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            RecordingService.RecordingBinder recordingBinder = (RecordingService.RecordingBinder) binder;
            mService = recordingBinder.getService();
            mBound = true;
            Log.wtf(TAG, "onServiceConnected.  mBound: " + mBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.wtf(TAG, "onServiceDisconnected");
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, RecordingService.class);
        if (bindService(serviceIntent, mServerConn, BIND_AUTO_CREATE)) {
            Log.wtf(TAG, "bindService() succeeded, mBound: " + mBound);
        } else {
            Log.wtf(TAG, "bindService() failed, mBound: " + mBound);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_recording);

        int speakerId = 0;
        int speakerVolume = 0;
        if (mBound)

        {
            speakerId = mService.getSpeakerId();
        }

        Toast.makeText(

                getApplicationContext(),

                "speaker: " + speakerId + "  vol: " + speakerVolume, Toast.LENGTH_SHORT).

                show();

    }

    public void displaySpeakerId(View v) {
        int speakerId = 0;
        int speakerVolume = 0;
        if (mBound) {
            speakerId = mService.getSpeakerId();
            speakerVolume = mService.getSpeakerVolume();
        }
        Toast.makeText(getApplicationContext(), "speaker: " + speakerId + "  vol: " + speakerVolume, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mServerConn != null) {
            unbindService(mServerConn);
        }
    }

}