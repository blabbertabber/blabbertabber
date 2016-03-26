package com.blabbertabber.blabbertabber;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
    private static final String PREF_RECORDING = "com.blabbertabber.blabbertabber.pref_recording";
    private static final int REQUEST_RECORD_AUDIO = 51;
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
    private BroadcastReceiver mReceiver;
    private Timer mTimer = new Timer();
    private TextView mTimerView;

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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "onReceive():  received Intent: " + intent);
                if (intent.getAction().equals(AudioEventProcessor.RECORD_RESULT)) {
                    int[] speakerinfo = intent.getIntArrayExtra(AudioEventProcessor.RECORD_MESSAGE);
                    int volume = speakerinfo[0];
                    mTimerView.setText(Helper.timeToHMMSSMinuteMandatory(mTimer.time()));
                    Log.v(TAG, "mReceiver.onReceive() " + volume);
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

        // http://developer.android.com/training/basics/data-storage/shared-preferences.html
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        RecordingService.recording = sharedPref.getBoolean(PREF_RECORDING, RecordingService.recording);

        double meetingInSeconds = Helper.howLongWasMeetingInSeconds(new File(getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME).length());
        Log.w(TAG, "onResume   meetingInSeconds: " + meetingInSeconds + "   Timer: " + mTimer.time());
        mTimer = new Timer((long) (meetingInSeconds * 1000));
        setContentView(R.layout.activity_recording);
        mTimerView = (TextView) findViewById(R.id.meeting_timer);
        // Let's make sure we have android.permission.RECORD_AUDIO permission and WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            registerRecordingServiceReceiver();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
        /// TODO: Determine if we need this for Toolbar manipulation.
        ///       If not, delete these lines.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Log.i(TAG, "onCreate  toolbar: " + toolbar);

        /// TODO: decide if someone pauses the meeting, switches to another activity, switches
        /// back to this activity--do we resume right away or honor the pause? We currently resume.
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
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_RECORDING, RecordingService.recording);
        editor.apply();
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

    private void registerRecordingServiceReceiver() {
        Intent serviceIntent = new Intent(this, RecordingService.class);
        // Start service first, then bind to it. Sounds redundant, but we have our reasons
        startService(serviceIntent);
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

    public void clickRecord(View v) {
        Log.i(TAG, "clickRecord() ");
        // was paused; need to record
        record();
        findViewById(R.id.button_reset).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_finish).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_record).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_record_caption).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_pause).setVisibility(View.VISIBLE);
        findViewById(R.id.button_pause_caption).setVisibility(View.VISIBLE);
    }

    public void clickPause(View v) {
        Log.i(TAG, "clickPause() ");
        // was recording; need to pause
        pause();
        findViewById(R.id.button_reset).setVisibility(View.VISIBLE);
        findViewById(R.id.button_finish).setVisibility(View.VISIBLE);
        findViewById(R.id.button_record).setVisibility(View.VISIBLE);
        findViewById(R.id.button_record_caption).setVisibility(View.VISIBLE);
        findViewById(R.id.button_pause).setVisibility(View.INVISIBLE);
        findViewById(R.id.button_pause_caption).setVisibility(View.INVISIBLE);
    }


    private void record() {
        Log.i(TAG, "record()");
        // Make sure we have all the necessary permissions, then begin recording:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "record() bailing out early, don't have permissions", Toast.LENGTH_LONG).show();
            Log.i(TAG, "record() bailing out early, don't have permissions");
            return;
        }
        mTimer.start();
        RecordingService.recording = true;
    }

    private void pause() {
        Log.i(TAG, "pause()");
        mTimer.stop();
        RecordingService.recording = false;
    }

    public void reset(View v) {
        Log.i(TAG, "reset()");
        mTimer.reset();
        RecordingService.reset = true;
        record(); // start recording again immediately after a reset
    }

    public void summary(View v) {
        Log.i(TAG, "summary()");
        mTimer.stop();
        pause(); // stop the recording
        diarizationProgress();
        final Context context = this;
        new Thread() {
            @Override
            public void run() {
                diarize();
                Intent intent = new Intent(context, SummaryActivity.class);
                startActivity(intent);
            }
        }.start();
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

    private void diarize() {
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
        String uemSegment = String.format("BlabTab 1 0 %d U U U S0", allFeatures.size());
        try {
            FileWriter uemWriter = new FileWriter(getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".uem.seg");
            uemWriter.write(uemSegment);
            uemWriter.flush();
            uemWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String basePathName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION;
        String[] linearSegParams = {
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
            Toast.makeText(this, "DiarizationException: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "Exception " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        try {
            MClust.main(linearClustParams);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void diarizationProgress() {
        Log.i(TAG, "diarizationProgress()");
        final ProgressDialog diarizationProgress = new ProgressDialog(this);
        diarizationProgress.setMessage(getString(R.string.performing_diarization));
        diarizationProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        diarizationProgress.setIndeterminate(false);
        diarizationProgress.setProgress(0);
        diarizationProgress.show();

        File meetingFile = new File(getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME);
        double diarizationDuration = Helper.howLongWillDiarizationTake(
                Helper.howLongWasMeetingInSeconds(meetingFile.length()),
                MainActivity.processorSpeed
        );
        // 1000 seconds/milliseconds, 100 ticks (each tick is 1% on the progressDialog)
        final long sleepInterval = (long) (1000.0 * diarizationDuration / 100.0);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        sleep(sleepInterval);
                        diarizationProgress.setProgress(i);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}