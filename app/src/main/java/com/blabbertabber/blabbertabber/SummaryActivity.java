package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
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

        ArrayList<Speaker> speakers = new ArrayList<Speaker>();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        setContentView(R.layout.activity_summary);

        String segPathFileName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".l.seg";
        FileInputStream in;
        ArrayList<Speaker> sp;
        try {
            in = new FileInputStream(segPathFileName);
            sp = new SpeakersBuilder().parseSegStream(in).build();
        } catch (IOException e) {
            Log.wtf(TAG, e.getClass().getName() + ": " + e + " thrown while trying to open " + segPathFileName);
            Toast.makeText(this, "I could not open the segmentation file, quitting", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        long meetingDuration = 0L;
        for (Speaker s : sp) {
            meetingDuration += s.getDuration();
        }
        long avgSpeakerDuration = meetingDuration / sp.size();

        TextView durationView = (TextView) findViewById(R.id.textview_duration);
        durationView.setText("" + meetingDuration);

        TextView avgSpeakerDurationView = (TextView) findViewById(R.id.textview_average);
        avgSpeakerDurationView.setText("" + avgSpeakerDuration);

        TextView minSpeakerDurationView = (TextView) findViewById(R.id.textview_min);
        long minSpeakerDuration = Collections.min(sp).getDuration();
        minSpeakerDurationView.setText("" + minSpeakerDuration);

        TextView maxSpeakerDurationView = (TextView) findViewById(R.id.textview_max);
        long maxSpeakerDuration = Collections.min(sp).getDuration();
        maxSpeakerDurationView.setText("" + maxSpeakerDuration);


//        try {
//        for (int i = 0; i < sp.size(); i++) {
        for (int i = 0; i < sp.size(); i++) {
            Speaker speaker = sp.get(i);
            Log.i(TAG, "onResume() speaker: " + speaker.getName() + " sp.size(): " + sp.size());

            TextView name = new TextView(this);
            name.setText(speaker.getName());
            GridLayout speakerGrid = (GridLayout) findViewById(R.id.speaker_duration_grid);
//                GridLayout.Spec spec = GridLayout.spec(Spec.WRAP_CONTENT);
//                GridLayout.LayoutParams params = new GridLayout.LayoutParams(spec,spec);
            speakerGrid.addView(name);

            TextView duration = new TextView(this);
            duration.setText("" + speaker.getDuration());
            speakerGrid.addView(duration);

            RectangleView rv = new RectangleView(this);
            rv.setVisible(true);
            rv.setColor(speaker.getColor());
            rv.setBarRatio((float) speaker.getDuration() / (float) maxSpeakerDuration);
            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
//            glp.height = GridLayout.LayoutParams.MATCH_PARENT;
            glp.height = 8;
            glp.width = GridLayout.LayoutParams.WRAP_CONTENT;
            glp.setGravity(Gravity.CENTER_VERTICAL);
            rv.setLayoutParams(glp);
            speakerGrid.addView(rv);
//            rv.invalidate();

//                int id = R.id.class.getField("speaker_name_label_" + i).getInt(0);
//                TextView tv = (TextView) findViewById(id);
//                tv.setText(speaker.getName());
//
//                id = R.id.class.getField("speaker_duration_label_" + i).getInt(0);
//                tv = (TextView) findViewById(id);
//                long speakerDuration = speaker.getDuration();
//                double speakerPercent = 100 * (double) speakerDuration / (double) meetingDuration;
//                tv.setText(String.format(" %8s (%2.0f%%)", Helper.timeToHMMSS(speakerDuration), speakerPercent));
//
//                id = R.id.class.getField("bar_speaker_" + i).getInt(0);
//                RectangleView rv = (RectangleView) findViewById(id);
//                rv.setVisible(true);
//                rv.setColor(speaker.getColor());
//                rv.setBarRatio((float) speakerDuration / (float) maxSpeakerDuration);
//                rv.invalidate();
        }
//        } catch (NoSuchFieldException e) {
//            Log.wtf(TAG, "NoSuchFieldException exception thrown on index with message " + e.getMessage());
//        } catch (IllegalAccessException e) {
//            Log.wtf(TAG, "IllegalAccessException exception thrown with message " + e.getMessage());
//        }
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
            long speakerDuration = speaker.getDuration();
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

    public void launchAcknowledgementsActivity(MenuItem menuItem) {
        Log.i(TAG, "launchAcknowledgementsActivity()");
        Intent intent = new Intent(this, AcknowledgementsActivity.class);
        startActivity(intent);
    }
}
