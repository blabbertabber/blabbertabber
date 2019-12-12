package com.blabbertabber.blabbertabber;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * Activity to record sound.
 */
public class RecordingActivity extends Activity {
    private static final String TAG = "RecordingActivity";
    private static final String PREF_DIARIZER = "com.blabbertabber.blabbertabber.pref_diarizer";
    private static final String PREF_RECORDING = "com.blabbertabber.blabbertabber.pref_recording";
    private static final String PREF_USE_TEST_SERVER = "com.blabbertabber.blabbertabber.pref_test_server";
    private static final String PREF_TRANSCRIBER = "com.blabbertabber.blabbertabber.pref_transcriber";
    private static final String DIARIZER_URL = "https://diarizer.com:9443/api/v1/upload";
    private static final String TEST_DIARIZER_URL = "http://test.diarizer.com:8080/api/v1/upload";
    private static final int BLOCK_SIZE = 32 * 1024;
    private static final int REQUEST_RECORD_AUDIO = 51;
    private static final String MULTIPART_BOUNDARY = "--ILoveMyDogCherieSheIsSoWarmAndCuddly";
    private boolean mBound = false;
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            RecordingService.RecordingBinder recordingBinder = (RecordingService.RecordingBinder) binder;
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
    private Thread mTimerDisplayThread;
    private ProgressBar uploadProgressBar;
    private boolean mUseTestServer = false;

    private String mDiarizer;
    private String mTranscriber;

    /**
     * Construct a new BroadcastReceiver that listens for Intent RECORD_RESULT and
     * Intent RECORD_STATUS.
     * Extracts the volumes and speaker id from the RECORD_RESULT messages.
     * Gracefully handles any RECORD_STATUS message as a failure.
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        // If you don't setContentView, you'll get either IllegalArgumentException or NullPointerException
        setContentView(R.layout.activity_recording);
        // Toolbar, https://developer.android.com/training/appbar/setting-up.html
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        uploadProgressBar = findViewById(R.id.determinateBar);
        // http://developer.android.com/training/basics/data-storage/shared-preferences.html
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mDiarizer = sharedPref.getString(PREF_DIARIZER, "IBM");
        mTranscriber = sharedPref.getString(PREF_TRANSCRIBER, "null");
        mUseTestServer = sharedPref.getBoolean(PREF_USE_TEST_SERVER, false);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(AudioEventProcessor.RECORD_RESULT)) {
                    displayTimer(mTimer);
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

        // Let's make sure we have android.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            registerRecordingServiceReceiver();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }

        double meetingInSeconds = Helper.howLongWasMeetingInSeconds(new File(getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME).length());
        Log.w(TAG, "onResume   meetingInSeconds: " + meetingInSeconds + "   Timer: " + mTimer.time());
        mTimer = new Timer((long) (meetingInSeconds * 1000));
        displayTimer(mTimer);

        // http://developer.android.com/training/basics/data-storage/shared-preferences.html
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(PREF_RECORDING, RecordingService.recording)) {
            // we're recording, not paused; maybe the screen was rotated
            clickRecord(null);
        } else {
            clickPause(null);
        }
        // mTimerDisplayThread sends Intents to update the Timer TextView
        mTimerDisplayThread = new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "run() starting " + Thread.currentThread().getId());
                try {
                    while (true) {
                        sleep(100);
                        Intent refreshTimerIntent = new Intent(AudioEventProcessor.RECORD_RESULT);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(refreshTimerIntent);
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    Log.i(TAG, "run() exiting " + Thread.currentThread().getId());
                }
            }
        };
        mTimerDisplayThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        uploadProgressBar.setVisibility(View.INVISIBLE);
        mTimerDisplayThread.interrupt();
        // unregister
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (mServerConn != null && mBound) {
            unbindService(mServerConn);
        }
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_DIARIZER, mDiarizer);
        editor.putBoolean(PREF_RECORDING, RecordingService.recording);
        editor.putBoolean(PREF_USE_TEST_SERVER, mUseTestServer);
        editor.putString(PREF_TRANSCRIBER, mTranscriber);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer, menu);
        menu.findItem(R.id.use_test_server).setChecked(mUseTestServer);
        switch (mDiarizer) {
            case "Aalto":
                menu.findItem(R.id.diarizer_aalto).setChecked(true);
                break;
            case "IBM":
                menu.findItem(R.id.diarizer_ibm).setChecked(true);
                break;
            default:
                menu.findItem(R.id.diarizer_aalto).setChecked(true);
        }
        switch (mTranscriber) {
            case "null":
                menu.findItem(R.id.transcriber_null).setChecked(true);
                break;
            case "CMU Sphinx 4":
                menu.findItem(R.id.transcriber_cmu).setChecked(true);
                break;
            case "IBM":
                menu.findItem(R.id.transcriber_ibm).setChecked(true);
                break;
            default:
                menu.findItem(R.id.transcriber_null).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.use_test_server:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                mUseTestServer = item.isChecked();
                Log.i(TAG, "Use test server: " + mUseTestServer);
                return true;
            case R.id.diarizer_aalto:
                mDiarizer = "Aalto";
                Log.i(TAG, "Diarizer " + mDiarizer);
                item.setChecked(true);
                return true;
            case R.id.diarizer_ibm:
                mDiarizer = "IBM";
                Log.i(TAG, "Diarizer " + mDiarizer);
                item.setChecked(true);
                return true;
            case R.id.transcriber_null:
                mTranscriber = "null";
                Log.i(TAG, "Transcriber " + mTranscriber);
                item.setChecked(true);
                return true;
            case R.id.transcriber_ibm:
                mTranscriber = "IBM";
                Log.i(TAG, "Transcriber " + mTranscriber);
                item.setChecked(true);
                return true;
            case R.id.transcriber_cmu:
                mTranscriber = "CMU Sphinx 4";
                Log.i(TAG, "Transcriber " + mTranscriber);
                item.setChecked(true);
                return true;
            default:
                mTranscriber = "null";
                mDiarizer = "Aalto";
                return super.onOptionsItemSelected(item);
        }
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
        displayTimer(mTimer);
        RecordingService.reset = true;
        pause();
    }

    public void summary(View v) {
        Log.i(TAG, "summary()");
        mTimer.stop();
        pause(); // stop the recording

        // Transform the raw file into a .wav file
        try {
            Log.i(TAG, "summary()   AudioRecordWrapper.getRawFilePathName(): " + AudioEventProcessor.getRawFilePathName());
            WavFile.of(this, new File(AudioEventProcessor.getRawFilePathName()));
        } catch (IOException e) {
            String errorTxt = "Whoops! couldn't convert " + AudioEventProcessor.getRawFilePathName()
                    + ": " + e.getMessage();
            Log.wtf(TAG, errorTxt);
            Toast.makeText(getApplicationContext(), errorTxt, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        uploadProgressBar.setVisibility(View.VISIBLE);

        new Thread() {
            @Override
            public void run() {
                // TODO(brian): make async (currently sync)
                HttpURLConnection diarizer = null;
                String diarizerUrl = DIARIZER_URL;
                if (mUseTestServer) {
                    diarizerUrl = TEST_DIARIZER_URL;
                }
                try {
                    diarizer = (HttpURLConnection) (new URL(diarizerUrl)).openConnection();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                String soundFilePath = WavFile.convertFilenameFromRawToWav(AudioEventProcessor.getRawFilePathName());
                File soundFile = new File(soundFilePath);
                long length = soundFile.length();
                FileInputStream soundFileStream = null;
                try {
                    soundFileStream = new FileInputStream(soundFile);
                } catch (java.io.IOException e) {
                    Log.e(TAG, "Unable to open sound file " + soundFilePath + ".  This is catastrophic.");
                    e.printStackTrace();
                }

                String resultsURL = "";
                try {
                    resultsURL = upload(diarizer, soundFileStream, length);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(resultsURL));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    Log.v(TAG, "view_results(): resolved activity");
                    startActivity(intent);
                } else {
                    Log.v(TAG, "view_results(): couldn't resolve activity");
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    private void displayTimer(Timer t) {
        ((TextView) findViewById(R.id.meeting_timer))
                .setText(Helper.timeToHMMSSMinuteMandatory(t.time()));
    }

    private String upload(HttpURLConnection diarizerConnection, FileInputStream soundFileStream, long soundFileLength) throws IOException {
        // http://stackoverflow.com/questions/34222980/urlconnection-always-returns-400-bad-request-when-i-try-to-upload-a-wav-file
        // upload .wav to endpoint and return GUID
        // TODO: don't load the entire meeting into RAM
        String resultsURL = "";
        try {
            diarizerConnection.setRequestMethod("POST");
            diarizerConnection.setRequestProperty("Diarizer", mDiarizer);
            diarizerConnection.setRequestProperty("Transcriber", mTranscriber);
            diarizerConnection.setRequestProperty("AndroidClientVersion", BuildConfig.VERSION_NAME);
            diarizerConnection.setDoOutput(true);
            diarizerConnection.setDoInput(true);
            diarizerConnection.setChunkedStreamingMode(BLOCK_SIZE); //disable while debugging

            // http://stackoverflow.com/questions/941628/urlconnection-filenotfoundexception-for-non-standard-http-port-sources/2274535#2274535
            diarizerConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            diarizerConnection.setRequestProperty("Accept", "*/*");

            diarizerConnection.setRequestProperty("Connection", "Keep-Alive");
            diarizerConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MULTIPART_BOUNDARY);
            diarizerConnection.setRequestProperty("Accept-Encoding", "identity"); // disable gzip compression for debuggin
            diarizerConnection.connect();

            OutputStream out = diarizerConnection.getOutputStream();
            out.write("Q\r\n".getBytes());

            addFilePart(soundFileStream, out, "soundFile", "meeting.wav", soundFileLength);

            byte[] buffer = new byte[BLOCK_SIZE];
            int status = diarizerConnection.getResponseCode();
            Log.i(TAG, "return code: " + status);
            InputStream in;
            if (status >= 300) {
                in = diarizerConnection.getErrorStream();
            } else {
                in = diarizerConnection.getInputStream();
            }
            int length = in.read(buffer);
            resultsURL = new String(buffer).substring(0, length);
            Log.i(TAG, "return data: " + resultsURL);
        } catch (IOException e) {
            Log.w(TAG, "Caught IOException: " + e.getMessage());
            Bundle connErrBundle = new Bundle();
            connErrBundle.putString("message", getString(R.string.cant_reach_server) + "\n\nDetails: " + diarizerConnection.getURL() + ": " + e.getMessage());
            DialogFragment uhOh = new UnreachableServerDialog();
            uhOh.setArguments(connErrBundle);
            uhOh.show(getFragmentManager(), "unreachableTag");
        } finally {
            diarizerConnection.disconnect();
        }
        return resultsURL;
    }

    private void addFilePart(FileInputStream in, OutputStream out, String paramName, String fileName, long soundFileLength) throws IOException {
        Log.i(TAG, "addFilePart()");
        out.write(("--" + MULTIPART_BOUNDARY + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        out.write(("Content-Type: application/octet-stream\r\n").getBytes());
        out.write("\r\n".getBytes());

        uploadProgressBar.setMax((int) soundFileLength / BLOCK_SIZE);
        Log.e(TAG, "addFilePart(): soundFileLength " + soundFileLength);
        //
        int megabytesUploaded = 0;
        byte[] buffer = new byte[BLOCK_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            uploadProgressBar.setProgress(megabytesUploaded);
            megabytesUploaded++;
            Log.e(TAG, "addFilePart(): megabytesUploaded  " + megabytesUploaded);
        }

        out.write("\r\n".getBytes());
        out.write(("--" + MULTIPART_BOUNDARY + "--" + "\r\n").getBytes());
    }

    /**
     * Replays the most recent meeting.
     * Called by the navigation drawer.
     *
     * @param menuItem Item selected in navigation drawer.  Unused within method.
     */
    public void replayMeeting(MenuItem menuItem) {
        Log.i(TAG, "replayMeeting()");
        String rawFilePath = AudioEventProcessor.getRawFilePathName();
        File rawFile = new File(rawFilePath);
        WavFile wavFile = null;
        try {
            wavFile = WavFile.of(this, rawFile);
            Log.i(TAG, "replayMeeting(): wavFile " + WavFile.convertFilenameFromRawToWav(rawFilePath) + " exists, playing");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Log.v(TAG, "delete me");
            Uri wavFileURI = FileProvider.getUriForFile(RecordingActivity.this, "com.blabbertabber.blabbertabber.fileprovider", wavFile);
            intent.setDataAndType(wavFileURI, "audio/wav");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.v(TAG, "delete me 2");
            if (intent.resolveActivity(getPackageManager()) != null) {
                Log.v(TAG, "replayMeeting(): resolved activity");
                try {
                    startActivity(intent);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "wavFile (" + AudioEventProcessor.getRawFilePathName() + ") cant't be shared");
                }

                //  Caused by: android.os.FileUriExposedException: file:///data/user/0/com.blabbertabber.blabbertabber/files/meeting.wav exposed beyond app through Intent.getData()
            } else {
                Log.e(TAG, "replayMeeting(): couldn't resolve activity");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launchMainActivity(MenuItem menuitem) {
        Log.i(TAG, "launchMainActivity()");
        MainActivity.resetFirstTime = true;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void launchAboutActivity(MenuItem menuItem) {
        Log.i(TAG, "launchAboutActivity()");
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
