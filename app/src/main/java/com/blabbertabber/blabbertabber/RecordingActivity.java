package com.blabbertabber.blabbertabber;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by cunnie on 8/16/15.
 * Activity to record and identify voices.
 */


public class RecordingActivity extends Activity {
    private static final String TAG = "RecordingActivity";
    private RecordingService mService;
    private boolean mBound = false;
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            RecordingService.RecordingBinder recordingBinder = (RecordingService.RecordingBinder) binder;
            mService = recordingBinder.getService();
            mBound = true;
            Log.v(TAG, "mServerConn.onServiceConnected()");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.v(TAG, "mServerConn.onServiceDisconnected()");
        }
    };
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int[] speakerinfo = intent.getIntArrayExtra(Recorder.RECORD_MESSAGE);
                int speaker = speakerinfo[0], volume = speakerinfo[1];
                // do something here.
                Log.v(TAG, "mReceiver.onReceive()" + speaker + ", " + volume);
                updateSpeakerVolumeView(speaker, volume);
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        Intent serviceIntent = new Intent(this, RecordingService.class);
        if (bindService(serviceIntent, mServerConn, BIND_AUTO_CREATE)) {
            Log.i(TAG, "bindService() succeeded, mBound: " + mBound);
        } else {
            Log.wtf(TAG, "bindService() failed, mBound: " + mBound);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(Recorder.RECORD_RESULT)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.activity_recording);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (mServerConn != null) {
            unbindService(mServerConn);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); // yes, call super first, even with onDestroy()
        Log.i(TAG, "onDestroy()");
    }

    public void dummyToggleRecording(View v) {
        Toast.makeText(getApplicationContext(), "You have toggled the Recording", Toast.LENGTH_SHORT).show();
    }

    public void dummyStopRecording(View v) {
        Toast.makeText(getApplicationContext(), "You have stopped the Recording", Toast.LENGTH_SHORT).show();
    }

    public void dummyFinishRecording(View v) {
        Toast.makeText(getApplicationContext(), "You have finished the Recording", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeakerVolumeView(int speakerId, int speakerVolume) {
        ImageView volume_ring = (ImageView) findViewById(R.id.ring_0);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) volume_ring.getLayoutParams();
        // convert from pixels to dp http://stackoverflow.com/questions/4914039/margins-of-a-linearlayout-programmatically-with-dp
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;

        switch (speakerId) {
            case 0:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case 1:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case 2:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case 3:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            default:
                Log.wtf(TAG, "we shouldn't get here");
        }
        // http://stackoverflow.com/questions/4472429/change-the-right-margin-of-a-view-programmatically
        volume_ring.requestLayout();

        PropertyValuesHolder phvx = PropertyValuesHolder.ofFloat(View.SCALE_X, speakerVolume / 10);
        PropertyValuesHolder phvy = PropertyValuesHolder.ofFloat(View.SCALE_Y, speakerVolume / 10);
        ObjectAnimator scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(findViewById(R.id.ring_0), phvx, phvy);
        scaleAnimation.setDuration(25).start();
    }
}