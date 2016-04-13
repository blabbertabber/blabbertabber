package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Shows a bar chart of speakers in decreasing order
 */
public class SummaryActivity extends Activity {
    private static final String TAG = "SummaryActivity";
    private static final CharSequence DRAWER_TITLE = "BlabberTabber Options"; // TODO internationalize, make string
    private static final CharSequence NORMAL_TITLE = "BlabberTabber";
    private long mMeetingDurationInMilliseconds;
    private ArrayList<Speaker> mSpeakers;

    public static String speakerDurationAndPercent(long speakerDurationInMilliseconds, long meetingDurationInMilliseconds) {
        double speakerPercent = 100 * (double) speakerDurationInMilliseconds / (double) meetingDurationInMilliseconds;
        return (String.format(Locale.getDefault(), " %8s (%2.0f%%) ", Helper.timeToHMMSS(speakerDurationInMilliseconds), speakerPercent));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        // If you don't setContentView, you'll get either IllegalArgumentException or NullPointerException
        setContentView(R.layout.activity_summary);
        // Nav Drawer, http://stackoverflow.com/questions/26082467/android-on-drawer-closed-listener
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.left_drawer);
        if (mDrawerLayout == null) {
            Log.wtf(TAG, "onCreate() mDrawerLayout is NULL!");
            return;
        } else {
            Log.i(TAG, "onCreate() mDrawerLayout is not null!");
        }

        ArrayList<Speaker> speakers = new ArrayList<Speaker>();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.activity_summary);

        String segPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        String rawPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        FileInputStream in;
        long rawFileSize = new File(rawPathFileName).length();
        mMeetingDurationInMilliseconds = rawFileSize
                * 1000
                / (AudioEventProcessor.RECORDER_SAMPLE_RATE_IN_HZ * 2);
        try {
            Log.i(TAG, "File size: " + rawFileSize);
            in = new FileInputStream(segPathFileName);
            mSpeakers = new SpeakersBuilder().parseSegStream(in).build();
            Log.i(TAG, "sp.size(): " + mSpeakers.size());
        } catch (IOException e) {
            Log.wtf(TAG, e.getClass().getName() + ": " + e + " thrown while trying to open " + segPathFileName);
            Toast.makeText(this, "I could not open the segmentation file, quitting", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
        long avgSpeakerDuration = mMeetingDurationInMilliseconds / mSpeakers.size();

        TextView durationView = (TextView) findViewById(R.id.textview_duration);
        durationView.setText(Helper.timeToHMMSS(mMeetingDurationInMilliseconds));

        TextView avgSpeakerDurationView = (TextView) findViewById(R.id.textview_average);
        avgSpeakerDurationView.setText(Helper.timeToHMMSSm(avgSpeakerDuration));

        TextView minSpeakerDurationView = (TextView) findViewById(R.id.textview_min);
        long minSpeakerDuration = Collections.min(mSpeakers).getDuration();
        minSpeakerDurationView.setText(Helper.timeToHMMSSm(minSpeakerDuration));

        TextView maxSpeakerDurationView = (TextView) findViewById(R.id.textview_max);
        long maxSpeakerDuration = Collections.max(mSpeakers).getDuration();
        maxSpeakerDurationView.setText(Helper.timeToHMMSSm(maxSpeakerDuration));


        for (int i = 0; i < mSpeakers.size(); i++) {
            Speaker speaker = mSpeakers.get(i);
            Log.i(TAG, "onResume() speaker: " + speaker.getName() + " sp.size(): " + mSpeakers.size());

            TextView name = new TextView(this);
            name.setText(speaker.getName());
            GridLayout speakerGrid = (GridLayout) findViewById(R.id.speaker_duration_grid);
            speakerGrid.addView(name);

            TextView duration = new TextView(this);
            duration.setText(speakerDurationAndPercent(speaker.getDuration(), mMeetingDurationInMilliseconds));
            speakerGrid.addView(duration);

            RectangleView rv = new RectangleView(this);
            rv.setVisible(true);
            rv.setColor(speaker.getColor());
            rv.setBarRatio((float) speaker.getDuration() / (float) maxSpeakerDuration);
            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
            glp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
            glp.width = GridLayout.LayoutParams.WRAP_CONTENT;
            glp.setGravity(Gravity.CENTER_VERTICAL);
            rv.setLayoutParams(glp);
            speakerGrid.addView(rv);
        }
    }

    /**
     * Replays the most recent meeting.
     * Called by the navigation drawer.
     *
     * @param menuItem Item selected in navigation drawer.  Unused within method.
     */
    public void replayMeeting(MenuItem menuItem) {
        Log.i(TAG, "replayMeeting()");
        String wavFilePath = WavFile.convertFilenameFromRawToWav(AudioEventProcessor.getRawFilePathName());
        File wavFile = new File(wavFilePath);
        Uri wavFileURI = Uri.fromFile(wavFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(wavFileURI, "audio/x-wav");
        if (wavFile.exists()) {
            Log.i(TAG, "replayMeeting(): wavFile " + wavFilePath + " exists, playing");
            if (intent.resolveActivity(getPackageManager()) != null) {
                Log.v(TAG, "replayMeeting(): resolved activity");
                startActivity(intent);
            } else {
                Log.v(TAG, "replayMeeting(): couldn't resolve activity");
            }
        } else {
            Log.e(TAG, "replayMeeting(): wavFile " + wavFilePath + " doesn't exist");
            Log.wtf(TAG, "The raw file's path name is " + AudioEventProcessor.getRawFilePathName());
            Toast.makeText(getApplicationContext(), "Can't play meeting file " + wavFilePath + "; it doesn't exist.", Toast.LENGTH_LONG).show();
        }
    }

    public void launchMainActivity(MenuItem menuitem) {
        Log.i(TAG, "launchMainActivity()");
        MainActivity.resetFirstTime = true;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void newMeeting(View v) {
        // clear out the old, raw-PCM file
        AudioEventProcessor.newMeetingFile();
        Intent i = new Intent(this, RecordingActivity.class);
        startActivity(i);
    }

    public void share(View v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSpeakers.size(); i++) {
            Speaker speaker = mSpeakers.get(i);
            sb.append(speakerDurationAndPercent(speaker.getDuration(), mMeetingDurationInMilliseconds));
            sb.append(speaker.getName());
            sb.append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "BlabberTabber result");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void showRawFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".raw";
        launchSpeakerStatsActivity(path);
    }

    public void showWavFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".wav";
        launchSpeakerStatsActivity(path);
    }

    public void showMfcFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".mfc";
        launchSpeakerStatsActivity(path);
    }

    public void showUemSegFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".uem.seg";
        launchSpeakerStatsActivity(path);
    }

    public void showSSegFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".s.seg";
        launchSpeakerStatsActivity(path);
    }

    public void showLSegFile(MenuItem menuItem) {
        String path = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        launchSpeakerStatsActivity(path);
    }

    public void launchSpeakerStatsActivity(String path) {
        Log.i(TAG, "launchSpeakerStatsActivity()");
        Intent intent = new Intent(this, SpeakerStatsActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    public void launchAboutActivity(MenuItem menuItem) {
        Log.i(TAG, "launchAboutActivity()");
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
