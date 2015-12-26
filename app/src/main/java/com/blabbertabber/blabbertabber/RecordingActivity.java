package com.blabbertabber.blabbertabber;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Activity to record sound.
 * TODO: reflect volume in animation somehow.
 */
public class RecordingActivity extends Activity {
    private static final String TAG = "RecordingActivity";
    private static final int REQUEST_RECORD_AUDIO = 51;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 52;
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
    private PieSlice bluePieSlice;
    private PieSlice redPieSlice;
    private PieSlice yellowPieSlice;
    private ObjectAnimator rotateBlue;
    private ObjectAnimator rotateRed;
    private ObjectAnimator rotateYellow;
    private AnimatorSet animatorSet;

    /**
     * Construct a new BroadcastReceiver that listens for Intent RECORD_RESULT and
     * Intent RECORD_STATUS.
     * Extracts the volumes and speaker id from the RECORD_RESULT messages.
     * Gracefully handles any RECORD_STATUS message as a failure.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        mSpeakers = TheSpeakers.getInstance();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "onReceive():  received Intent: " + intent);
                if (intent.getAction().equals(Recorder.RECORD_RESULT)) {
                    /// TODO: remove speaker id, and change data passed to just an int.  No array needed.
                    int[] speakerinfo = intent.getIntArrayExtra(Recorder.RECORD_MESSAGE);
                    int speaker = speakerinfo[0], volume = speakerinfo[1];
                    // do something here.
                    Log.v(TAG, "mReceiver.onReceive()" + speaker + ", " + volume);
                    updateSpeakerVolumeView(speaker, volume);
                } else if (Objects.equals(intent.getAction(), Recorder.RECORD_STATUS)) {
                    // If we start sending statuses other than MICROPHONE_UNAVAILABLE, add logic to check status message returned.
                    int status = intent.getIntExtra(Recorder.RECORD_STATUS_MESSAGE, Recorder.UNKNOWN_STATUS);
                    String statusMsg = "onReceive():  The microphone has a status of " + status;
                    Log.wtf(TAG, statusMsg);
                    String toastMessage;
                    switch (status) {
                        case Recorder.MICROPHONE_UNAVAILABLE:
                            toastMessage = "Problem accessing microphone. Terminate app using microphone (e.g. Phone, Hangouts) and try again.";
                            break;
                        case Recorder.CANT_WRITE_MEETING_FILE:
                            toastMessage = "Error recording the meeting to disk; make sure you've closed all BlabberTabber instances and try again.";
                            break;
                        case Recorder.UNKNOWN_STATUS:
                        default:
                            toastMessage = "I have no idea what went wrong; restart BlabberTabber and see if that fixes it";
                    }
                    Toast.makeText(context, toastMessage,
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "onReceive() received an Intent with unknown action " + intent.getAction();
                    Log.wtf(TAG, errorMsg);
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                }
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
        // Let's make sure we have android.permission.RECORD_AUDIO permission and WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                registerRecordingServiceReceiver();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
        bluePieSlice = (PieSlice) findViewById(R.id.blue_pie_slice);
        redPieSlice = (PieSlice) findViewById(R.id.red_pie_slice);
        yellowPieSlice = (PieSlice) findViewById(R.id.yellow_pie_slice);

        // TODO: decide if someone pauses the meeting, switches to another activity, switches
        // back to this activity--do we resume right away or honor the pause? We currently resume.
        record();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        // unregister
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (mServerConn != null && mBound) {
            unbindService(mServerConn);
        }
        pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        // clear out the animatorSet; it doesn't recover properly (paused/started/running
        // are all true, but the animations are frozen)
        animatorSet.cancel(); // cancel() is quicker than end(); it doesn't wait for animations to finish
        animatorSet = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); // yes, call super first, even with onDestroy()
        Log.i(TAG, "onDestroy()");
    }

    private void registerRecordingServiceReceiver() {
        Intent serviceIntent = new Intent(this, RecordingService.class);
        if (bindService(serviceIntent, mServerConn, BIND_AUTO_CREATE)) {
            Log.i(TAG, "bindService() succeeded, mBound: " + mBound);
        } else {
            Log.wtf(TAG, "bindService() failed, mBound: " + mBound);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(Recorder.RECORD_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(Recorder.RECORD_STATUS)
        );
    }

    public void togglePauseRecord(View v) {
        Log.i(TAG, "togglePauseRecord() getRecording initial state: " + RecordingService.recording);
        if (RecordingService.recording) {
            pause();
        } else {
            record();
        }
    }

    private void record() {
        Log.i(TAG, "record()");
        // Make sure we have all the necessary permissions, then begin recording:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "record() bailing out early, don't have permissions");
            return;
        }
        RecordingService.recording = true;

        // start the animations
        findViewById(R.id.button_record).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_pause).setVisibility(View.VISIBLE);

        rotateBlue = ObjectAnimator.ofFloat(bluePieSlice, View.ROTATION, 360).setDuration(7_000);
        rotateRed = ObjectAnimator.ofFloat(redPieSlice, View.ROTATION, -360).setDuration(11_000);
        rotateYellow = ObjectAnimator.ofFloat(yellowPieSlice, View.ROTATION, 360).setDuration(13_000);

        rotateBlue.setRepeatCount(ValueAnimator.INFINITE);
        rotateRed.setRepeatCount(ValueAnimator.INFINITE);
        rotateYellow.setRepeatCount(ValueAnimator.INFINITE);

        if (animatorSet == null) {
            animatorSet = new AnimatorSet();
            animatorSet.play(rotateBlue).with(rotateRed).with(rotateYellow);
            animatorSet.start();
        } else {
            Log.i(TAG, "animatorSet.isPaused(): " + animatorSet.isPaused() +
                    " animatorSet.isStarted(): " + animatorSet.isStarted() +
                    " animatorSet.isRunning(): " + animatorSet.isRunning());
            if (animatorSet.isPaused()) {
                animatorSet.resume();
            }
        }
    }

    private void pause() {
        Log.i(TAG, "pause()");
        RecordingService.recording = false;
        // stop the animations
        findViewById(R.id.button_record).setVisibility(View.VISIBLE);
        findViewById(R.id.button_pause).setVisibility(View.INVISIBLE);
        // close-out the current speaker
        stopPreviousSpeaker();
        // pause the animation
        animatorSet.pause();
    }

    public void reset(View v) {
        Log.i(TAG, "reset()");
        mPreviousSpeakerId = -1;
        mSpeakers.reset();
        // reset the animation and begin recording
        animatorSet.end();
        animatorSet.start();
        RecordingService.reset = true;
        record(); // start recording again immediately after a reset
    }

    public void summary(View v) {
        Log.i(TAG, "summary()");
        /// Transform the raw file into a .wav file
        WavFile wavFile = null;
        try {
            wavFile = WavFile.of(new File(AudioRecordWrapper.RECORDER_RAW_FILENAME));
        } catch (IOException e) {
            String errorTxt = "Whoops! couldn't convert " + AudioRecordWrapper.RECORDER_RAW_FILENAME
                    + ": " + e.getMessage();
            Log.wtf(TAG, errorTxt);
            Toast.makeText(getApplicationContext(), errorTxt, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        /// process the .wav file
        Intent intent = new Intent(this, SummaryActivity.class);
        startActivity(intent);
    }

    private void stopPreviousSpeaker() {
        Log.i(TAG, "stopPreviousSpeaker()");
        if (mPreviousSpeakerId >= 0) {
            // The previous speaker is valid; we are not initializing.
            Speaker previousSpeaker = mSpeakers.speakers[mPreviousSpeakerId];
            previousSpeaker.stopSpeaking();
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

        // PieSlices are "maxed-out" by default
        // Max-out corresponds to a 32767 speaker volume
        // .80 * PieSlice is the min, corresponding to a 0 speaker volume
        // "goldenRatio" has nothing to do with the Golden Ratio
        float goldenRatio = (float) (0.2 * (double) speakerVolume / (double) Short.MAX_VALUE + 0.8);
        Log.d(TAG, "updateSpeakerVolumeView() goldenRatio: " + goldenRatio);
        PropertyValuesHolder phvx = PropertyValuesHolder.ofFloat(View.SCALE_X, (float) (goldenRatio));
        PropertyValuesHolder phvy = PropertyValuesHolder.ofFloat(View.SCALE_Y, (float) (goldenRatio));

        ObjectAnimator bScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(bluePieSlice, phvx, phvy).setDuration(20);
        ObjectAnimator rScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(redPieSlice, phvx, phvy).setDuration(20);
        ObjectAnimator yScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(yellowPieSlice, phvx, phvy).setDuration(20);

        AnimatorSet ephemeralAnimatorSet = new AnimatorSet();
        ephemeralAnimatorSet.play(bScaleAnimation).with(rScaleAnimation).with(yScaleAnimation);
        ephemeralAnimatorSet.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // log everything
        String argumentString = " " + requestCode + " [ ";
        for (String perm : permissions) {
            argumentString += perm + ", ";
        }
        argumentString += " ], [ ";
        for (int grantResult : grantResults) {
            argumentString += grantResult + ", ";
        }
        argumentString += " ]";
        Log.i(TAG, "onRequestPermissionsResult() " + argumentString);

        // http://developer.android.com/training/permissions/requesting.html
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission denied, message & exit gracefully
                    Toast.makeText(getApplicationContext(), "BlabberTabber exited because it's unable to access the microphone", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission denied, message & exit gracefully
                    Toast.makeText(getApplicationContext(), "BlabberTabber exited because it's unable to write storage", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
        Log.wtf(TAG, "Oops, an unasked-for permission was granted/denied.");
    }
}