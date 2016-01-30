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
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Objects;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MSeg;
//import javax.sound.sampled.AudioFormat;
//import android.media.AudioFormat;

/**
 * Activity to record sound.
 * TODO: reflect volume in animation somehow.
 */
public class RecordingActivity extends Activity {
    public static final String SPHINX_CONFIG = "sphinx4_config.xml";
    private static final String TAG = "RecordingActivity";
    private static final int REQUEST_RECORD_AUDIO = 51;
    private static final AnimatorSet NULL_ANIMATOR_SET = new AnimatorSet();
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
    private AnimatorSet animatorSet = NULL_ANIMATOR_SET;    // Null object pattern

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
                if (intent.getAction().equals(AudioEventProcessor.RECORD_RESULT)) {
                    /// TODO: remove speaker id, and change data passed to just an int.  No array needed.
                    int[] speakerinfo = intent.getIntArrayExtra(AudioEventProcessor.RECORD_MESSAGE);
                    int speaker = speakerinfo[0], volume = speakerinfo[1];
                    // do something here.
                    Log.v(TAG, "mReceiver.onReceive()" + speaker + ", " + volume);
                    updateSpeakerVolumeView(speaker, volume);
                } else if (Objects.equals(intent.getAction(), AudioEventProcessor.RECORD_STATUS)) {
                    // If we start sending statuses other than MICROPHONE_UNAVAILABLE, add logic to check status message returned.
                    int status = intent.getIntExtra(AudioEventProcessor.RECORD_STATUS_MESSAGE, AudioEventProcessor.UNKNOWN_STATUS);
                    String statusMsg = "onReceive():  The microphone has a status of " + status;
                    Log.wtf(TAG, statusMsg);
                    String toastMessage;
                    switch (status) {
                        case AudioEventProcessor.MICROPHONE_UNAVAILABLE:
                            toastMessage = "Problem accessing microphone. Terminate app using microphone (e.g. Phone, Hangouts) and try again.";
                            break;
                        case AudioEventProcessor.CANT_WRITE_MEETING_FILE:
                            toastMessage = "Error recording the meeting to disk; make sure you've closed all BlabberTabber instances and try again.";
                            break;
                        case AudioEventProcessor.UNKNOWN_STATUS:
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
            registerRecordingServiceReceiver();
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
        animatorSet = NULL_ANIMATOR_SET;
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
                new IntentFilter(AudioEventProcessor.RECORD_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                new IntentFilter(AudioEventProcessor.RECORD_STATUS)
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "record() bailing out early, don't have permissions", Toast.LENGTH_LONG).show();
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

        if (animatorSet == NULL_ANIMATOR_SET) {
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
        // Transform the raw file into a .wav file
        WavFile wavFile;
        try {
            Log.i(TAG, "summary()   AudioRecordWrapper.getRawFilePathName(): " + AudioEventProcessor.getRawFilePathName());
            wavFile = WavFile.of(this, new File(AudioEventProcessor.getRawFilePathName()));
        } catch (IOException e) {
            String errorTxt = "Whoops! couldn't convert " + AudioEventProcessor.getRawFilePathName()
                    + ": " + e.getMessage();
            Log.wtf(TAG, errorTxt);
            Toast.makeText(getApplicationContext(), errorTxt, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        // Copy Sphinx's config file into place
        copySphinxConfigFileIntoPlace();

        // Create config manager
        ConfigurationManager cm = new ConfigurationManager(getFilesDir() + "/" + SPHINX_CONFIG);

        FrontEnd frontEnd = (FrontEnd) cm.lookup("mfcFrontEnd");
        StreamDataSource audioSource = (StreamDataSource) cm.lookup("streamDataSource");

        String inputAudioFile = getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME;

        try {
            audioSource.setInputStream(new FileInputStream(inputAudioFile), "audio");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LinkedList<float[]> allFeatures = new LinkedList<float[]>();
        int featureLength = -1;

        //get features from audio
        try {
            assert (allFeatures != null);
            Data feature = frontEnd.getData();
            while (!(feature instanceof DataEndSignal)) {
                if (feature instanceof DoubleData) {
                    double[] featureData = ((DoubleData) feature).getValues();
                    if (featureLength < 0) {
                        featureLength = featureData.length;
                        //logger.info("Feature length: " + featureLength);
                    }
                    float[] convertedData = new float[featureData.length];
                    for (int i = 0; i < featureData.length; i++) {
                        convertedData[i] = (float) featureData[i];
                    }
                    allFeatures.add(convertedData);
                } else if (feature instanceof FloatData) {
                    float[] featureData = ((FloatData) feature).getValues();
                    if (featureLength < 0) {
                        featureLength = featureData.length;
                        //logger.info("Feature length: " + featureLength);
                    }
                    allFeatures.add(featureData);
                }
                feature = frontEnd.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //write the MFCC features to binary file
        DataOutputStream outStream = null;
        try {
            outStream = new DataOutputStream(new FileOutputStream(getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".mfc"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            outStream.writeInt(allFeatures.size() * featureLength);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (float[] feature : allFeatures) {
            for (float val : feature) {
                try {
                    outStream.writeFloat(val);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        try {
            outStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //write initial segmentation file for LIUM_SpkDiarization
        String uemSegment = String.format("test 1 0 %d U U U S0", allFeatures.size());
        try {
            FileWriter uemWriter = new FileWriter(getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".mfc");
            uemWriter.write(uemSegment);
            uemWriter.flush();
            uemWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String basePathName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION;
        int DONE_LINEARSEG = 50;
        int DONE_LINEARCLUST = 100;
        String[] linearSegParams =
                {
                        "--trace",
                        "--help",
                        "--kind=FULL",
                        "--sMethod=GLR",
                        "--fInputMask=" + basePathName + ".mfc",
                        "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                        "--sInputMask=" + basePathName + ".uem.seg",
                        "--sOutputMask=" + basePathName + ".s.seg",
                        AudioEventProcessor.RECORDER_RAW_FILENAME
                };
        String[] linearClustParams = {
                "--trace",
                "--help",
                "--fInputMask=" + basePathName + ".mfc",
                "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0",
                "--sInputMask=" + basePathName + ".s.seg",
                "--sOutputMask=" + basePathName + ".l.seg",
                "--cMethod=l",
                "--cThr=2",
                AudioEventProcessor.RECORDER_RAW_FILENAME
        };

        try {
            MSeg.main(linearSegParams);
        } catch (DiarizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            MClust.main(linearClustParams);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

        // shrink and expand the pie slices, depending on how loudly people are talking
        AnimatorSet ephemeralAnimatorSet = new AnimatorSet();
        ephemeralAnimatorSet.play(bScaleAnimation).with(rScaleAnimation).with(yScaleAnimation);
        ephemeralAnimatorSet.start();
    }

    private void copySphinxConfigFileIntoPlace() {
        String outputFilePathname = getFilesDir() + "/" + SPHINX_CONFIG;
        try {
            InputStream inputStream = getAssets().open(SPHINX_CONFIG, AssetManager.ACCESS_BUFFER);
            Helper.copyInputFileStreamToFilesystem(inputStream, outputFilePathname);
        } catch (IOException e) {
            Log.wtf(TAG, "copySphinxConfigFileIntoPlace() couldn't copy file");
            Toast.makeText(this, "Configuration didn't succeed, expect no results", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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

        // boilerplate from http://developer.android.com/training/permissions/requesting.html
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
        }
        Log.wtf(TAG, "Oops, an unasked-for permission was granted/denied.");
    }

    private class diarize extends AsyncTask<Void, Integer, Void> {
        int DONE_LINEARSEG = 50;
        int DONE_LINEARCLUST = 100;
        String basePathName = getApplicationContext().getFilesDir() + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION;

        String[] linearSegParams =
                {
                        "--trace", "--help", "--kind=FULL", "--sMethod=GLR", "--fInputMask=" + basePathName + ".mfc", "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0", "--sInputMask=" + basePathName + ".uem.seg", "--sOutputMask=" + basePathName + ".s.seg", AudioEventProcessor.RECORDER_RAW_FILENAME
                };
        String[] linearClustParams = {"--trace", "--help", "--fInputMask=" + basePathName + ".mfc", "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0", "--sInputMask=" + basePathName + ".s.seg", "--sOutputMask=" + basePathName + ".l.seg", "--cMethod=l", "--cThr=2", AudioEventProcessor.RECORDER_RAW_FILENAME};

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(DRZ_DIALOG);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                MSeg.main(linearSegParams);
            } catch (DiarizationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            setProgress(DONE_LINEARSEG);

            try {
                MClust.main(linearClustParams);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            setProgress(DONE_LINEARCLUST);

            return null;
        }

//        @Override
//        protected void onPostExecute(Void unused) {
//            dismissDialog(DRZ_DIALOG);
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... value) {
//            dProgressDialog.setProgress(value[0]);
//        }
    }

/*    private class diarize extends AsyncTask<Void, Integer, Void> {
        int DONE_LINEARSEG = 50;
        int DONE_LINEARCLUST = 100;
        String[] linearSegParams = {"--trace", "--help", "--kind=FULL", "--sMethod=GLR", "--fInputMask=/sdcard/test.mfc", "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0", "--sInputMask=/sdcard/test.uem.seg", "--sOutputMask=/sdcard/test.s.seg", "test"};
        String[] linearClustParams = {"--trace", "--help", "--fInputMask=/sdcard/test.mfc", "--fInputDesc=sphinx,1:1:0:0:0:0,13,0:0:0", "--sInputMask=/sdcard/test.s.seg", "--sOutputMask=/sdcard/test.l.seg", "--cMethod=l", "--cThr=2", "test"};

    }*/
}