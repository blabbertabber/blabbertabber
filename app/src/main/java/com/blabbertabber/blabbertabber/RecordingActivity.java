package com.blabbertabber.blabbertabber;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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
    RecordingService mService;
    boolean mBound = false;
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
//        Toast.makeText(getApplicationContext(), "speaker: " + speakerId + "  vol: " + speakerVolume, Toast.LENGTH_SHORT).show();
        View view;

//      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.recording_layout).getLayoutParams();
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
                view = findViewById(R.id.id_3);
                Log.wtf(TAG, "we shouldn't get here");
        }
        //// ObjectAnimator anim = ObjectAnimator.ofInt(view, "imageAlpha", 0, 0xff);
        //// anim.setDuration(500);
        //// anim.start();

        // http://stackoverflow.com/questions/4472429/change-the-right-margin-of-a-view-programmatically
        volume_ring.requestLayout();

        PropertyValuesHolder phvx = PropertyValuesHolder.ofFloat(View.SCALE_X, speakerVolume / 10);
        PropertyValuesHolder phvy = PropertyValuesHolder.ofFloat(View.SCALE_Y, speakerVolume / 10);
        ObjectAnimator scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(findViewById(R.id.ring_0), phvx, phvy);
        scaleAnimation.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mServerConn != null) {
            unbindService(mServerConn);
        }
    }

}
