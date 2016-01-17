package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Shows a bar chart of speakers in decreasing order
 */
public class SummaryActivity extends Activity {

    private static final String TAG = "SummaryActivity";
    private static final CharSequence DRAWER_TITLE = "BlabberTabber Options"; // TODO internationalize, make string
    private static final CharSequence NORMAL_TITLE = "BlabberTabber";
    // Nav Drawer variables
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        // If you don't setContentView, you'll get either IllegalArgumentException or NullPointerException
        setContentView(R.layout.activity_summary);
        // Nav Drawer, http://stackoverflow.com/questions/26082467/android-on-drawer-closed-listener
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.left_drawer);
        if (mDrawerLayout == null) {
            Log.wtf(TAG, "onCreate() mDrawerLayout is NULL!");
            return;
        } else {
            Log.i(TAG, "onCreate() mDrawerLayout is not null!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.activity_summary);

        TextView durationView = (TextView) findViewById(R.id.textview_duration);
        long meetingDuration = TheSpeakers.getInstance().getMeetingDuration();
        durationView.setText(Helper.timeToHMMSS(meetingDuration));

        TextView avgSpeakerDurationView = (TextView) findViewById(R.id.textview_average);
        long avgSpeakerDuration = TheSpeakers.getInstance().getAverageSpeakerDuration();
        avgSpeakerDurationView.setText(Helper.timeToHMMSSm(avgSpeakerDuration));

        TextView minSpeakerDurationView = (TextView) findViewById(R.id.textview_min);
        long minSpeakerDuration = TheSpeakers.getInstance().getMinSpeakerDuration();
        minSpeakerDurationView.setText(Helper.timeToHMMSSm(minSpeakerDuration));

        TextView maxSpeakerDurationView = (TextView) findViewById(R.id.textview_max);
        long maxSpeakerDuration = TheSpeakers.getInstance().getMaxSpeakerDuration();
        maxSpeakerDurationView.setText(Helper.timeToHMMSSm(maxSpeakerDuration));

        ArrayList<Speaker> sp = TheSpeakers.getInstance().getSortedSpeakerList();

        try {
            for (int i = 0; i < sp.size(); i++) {
                Speaker speaker = sp.get(i);

                int id = R.id.class.getField("speaker_name_label_" + i).getInt(0);
                TextView tv = (TextView) findViewById(id);
                tv.setText(speaker.getName());

                id = R.id.class.getField("speaker_duration_label_" + i).getInt(0);
                tv = (TextView) findViewById(id);
                long speakerDuration = speaker.duration();
                double speakerPercent = 100 * (double) speakerDuration / (double) meetingDuration;
                String speakerStats = String.format(" %8s (%2.0f%%)", Helper.timeToHMMSS(speakerDuration), speakerPercent);
                tv.setText((CharSequence) speakerStats);

                id = R.id.class.getField("bar_speaker_" + i).getInt(0);
                RectangleView rv = (RectangleView) findViewById(id);
                rv.setVisible(true);
                rv.setColor(speaker.getColor());
                rv.setBarRatio((float) speakerDuration / (float) maxSpeakerDuration);
                rv.invalidate();
            }
        } catch (NoSuchFieldException e) {
            Log.wtf(TAG, "NoSuchFieldException exception thrown on index with message " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.wtf(TAG, "IllegalAccessException exception thrown with message " + e.getMessage());
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
        TheSpeakers.getInstance().reset();
        // clear out the old, raw-PCM file
        AudioEventProcessor.newMeetingFile();
        Intent i = new Intent(this, RecordingActivity.class);
        startActivity(i);
    }

    public void share(View v) {
        long meetingDuration = TheSpeakers.getInstance().getMeetingDuration();
        ArrayList<Speaker> sp = TheSpeakers.getInstance().getSortedSpeakerList();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sp.size(); i++) {
            Speaker speaker = sp.get(i);
            long speakerDuration = speaker.duration();
            double speakerPercent = 100 * (double) speakerDuration / (double) meetingDuration;
            String speakerStats = String.format(" %8s (%2.0f%%) ", Helper.timeToHMMSS(speakerDuration), speakerPercent);
            sb.append(speakerStats + "  ");
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
}
