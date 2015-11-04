package com.blabbertabber.blabbertabber;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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

    public void record(View v) {
        Toast.makeText(getApplicationContext(), "You are recording", Toast.LENGTH_SHORT).show();
    }

    public void pause(View v) {
        Toast.makeText(getApplicationContext(), "You have paused the Recording", Toast.LENGTH_SHORT).show();
    }

    public void reset(View v) {
        Toast.makeText(getApplicationContext(), "You have reset the Recording", Toast.LENGTH_SHORT).show();
    }

    public void finish(View v) {
        Toast.makeText(getApplicationContext(), "You have finished the Recording", Toast.LENGTH_SHORT).show();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace(); // FIXME: why bother doing this?
        }
        finish();
    }

    private void updateSpeakerVolumeView(int speakerId, int speakerVolume) {
        ////ImageView volume_ring = (ImageView) findViewById(R.id.ring_0);
        ///GridLayout.LayoutParams params = (GridLayout.LayoutParams) volume_ring.getLayoutParams();
        // convert from pixels to dp http://stackoverflow.com/questions/4914039/margins-of-a-linearlayout-programmatically-with-dp
        float dp = getApplicationContext().getResources().getDisplayMetrics().density;

///        GridLayout gridLayout = (GridLayout) findViewById(R.id.speaker_grid);
//        ((ViewGroup)volume_ring.getParent()).removeView(volume_ring);
//        gridLayout.addView(volume_ring, new GridLayout.LayoutParams(
//                GridLayout.spec(1, GridLayout.CENTER),
///                GridLayout.spec(1, GridLayout.CENTER)));


        // http://stackoverflow.com/questions/4472429/change-the-right-margin-of-a-view-programmatically
        //volume_ring.requestLayout();

        View currentSpeaker = findViewById(rDotId(speakerId));
        currentSpeaker.requestLayout();

//        float originalX = PropertyValuesHolder.ofFloat(View.X);
//        PropertyValuesHolder phvx = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, speakerVolume / 10);
//        PropertyValuesHolder phvy = PropertyValuesHolder.ofFloat(View.SCALE_Y, speakerVolume / 10);
        ObjectAnimator translateAnimationUp = ObjectAnimator.ofFloat(currentSpeaker, View.TRANSLATION_Y, -speakerVolume);
        translateAnimationUp.setRepeatCount(1);
        translateAnimationUp.setRepeatMode(ValueAnimator.REVERSE);
        translateAnimationUp.setDuration(45);

//        ObjectAnimator translateAnimationDown = ObjectAnimator.ofFloat(currentSpeaker, View.TRANSLATION_Y, speakerVolume);
//        translateAnimationDown.setRepeatCount(1);
//        translateAnimationDown.setRepeatMode(ValueAnimator.REVERSE);
//        translateAnimationDown.setDuration(12);

        translateAnimationUp.start();
//        AnimatorSet setAnimation = new AnimatorSet();
//        setAnimation.play(translateAnimationUp);

//        PropertyValuesHolder phvyUp = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -speakerVolume);
//        PropertyValuesHolder phvyDown = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, speakerVolume);
//        PropertyValuesHolder phvReset = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0);
//        ObjectAnimator translateAnimation = ObjectAnimator.ofPropertyValuesHolder(currentSpeaker, phvyUp, phvReset, phvyDown, phvReset);
////        translateAnimation.ofFloat(current_speaker, View.TRANSLATION_Y, -speakerVolume);

//        phvx = PropertyValuesHolder.ofFloat(View.X, 0);

    }

    int rDotId(int speakerId) {
        switch (speakerId) {
            case 0:
                return (R.id.speaker_0);
            case 1:
                return (R.id.speaker_1);
            case 2:
                return (R.id.speaker_2);
            case 3:
                return (R.id.speaker_3);
            case 4:
                return (R.id.speaker_4);
            case 5:
                return (R.id.speaker_5);
            case 6:
                return (R.id.speaker_6);
            case 7:
                return (R.id.speaker_7);
            case 8:
                return (R.id.speaker_8);
            case 9:
                return (R.id.speaker_9);
            case 10:
                return (R.id.speaker_10);
            case 11:
                return (R.id.speaker_11);
            case 12:
                return (R.id.speaker_12);
            case 13:
                return (R.id.speaker_13);
            case 14:
                return (R.id.speaker_14);
            case 15:
                return (R.id.speaker_15);
            default:
                Log.wtf(TAG, "We should never get here.  Speaker ID " + speakerId + " should be from 0 to 15 inclusive.  ");
                return (R.id.speaker_0);
        }
    }
}