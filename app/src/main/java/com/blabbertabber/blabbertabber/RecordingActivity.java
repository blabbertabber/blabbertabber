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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by cunnie on 8/16/15.
 * Activity to record and identify voices.
 */


public class RecordingActivity extends Activity {
    private static final String TAG = "RecordingActivity";
    private RecordingService mRecordingService;
    private boolean mBound = false;
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            RecordingService.RecordingBinder recordingBinder = (RecordingService.RecordingBinder) binder;
            mRecordingService = recordingBinder.getService();
            mBound = true;
            Log.v(TAG, "mServerConn.onServiceConnected()");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.v(TAG, "mServerConn.onServiceDisconnected()");
        }
    };
    private int mPreviousSpeakerId = -1;
    private TheSpeakers mSpeakers;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        mSpeakers = TheSpeakers.getInstance();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.activity_recording);
        // kick off the service
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
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        // unregister
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (mServerConn != null) {
            unbindService(mServerConn);
        }
        // close-out the current speaker
        stopPreviousSpeaker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
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
        mPreviousSpeakerId = -1;
        mSpeakers.reset();
    }

    public void summary(View v) {
        Intent intent = new Intent(this, SummaryActivity.class);
        startActivity(intent);
    }

    private void stopPreviousSpeaker() {
        if (mPreviousSpeakerId >= 0) {
            // The previous speaker is valid; we are not initializing.
            // reset the size of the previous speakerBall, and dim it, too
            Speaker previousSpeaker = mSpeakers.speakers[mPreviousSpeakerId];
            previousSpeaker.stopSpeaking();
            View previousSpeakerBall = findViewById(previousSpeaker.getViewID());
            previousSpeakerBall.setScaleX(1);
            previousSpeakerBall.setScaleY(1);
            previousSpeakerBall.setAlpha((float) 0.7);
        }
    }

    private void updateSpeakerVolumeView(int speakerId, int speakerVolume) {
        if (speakerId != mPreviousSpeakerId) {
            // Aha! The speaker has changed.
            stopPreviousSpeaker();
            mPreviousSpeakerId = speakerId;
        }
        Speaker speaker = mSpeakers.speakers[speakerId];
        speaker.startSpeaking();
        ImageView speakerBall = (ImageView) findViewById(speaker.getViewID());
        speaker.setVisible(View.VISIBLE);
        speakerBall.setVisibility(View.VISIBLE);
        speakerBall.setAlpha((float) 1.0);
        GradientDrawable shape = (GradientDrawable) speakerBall.getDrawable();
        if (shape != null) {
            shape.setColor(speaker.getColor());
        }
        speakerBall.requestLayout();

        PropertyValuesHolder phvx = PropertyValuesHolder.ofFloat(View.SCALE_X, (float) (0.5 + speakerVolume / 80.0));
        PropertyValuesHolder phvy = PropertyValuesHolder.ofFloat(View.SCALE_Y, (float) (0.5 + speakerVolume / 80.0));
        ObjectAnimator scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(speakerBall, phvx, phvy);
        scaleAnimation.setDuration(20).start();
    }
}