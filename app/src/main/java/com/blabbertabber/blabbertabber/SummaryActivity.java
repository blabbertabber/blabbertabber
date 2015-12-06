package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cunnie on 11/11/15.
 */
public class SummaryActivity extends Activity {

    private static final String TAG = "SummaryActivity";
    private static final CharSequence DRAWER_TITLE = "BlabberTabber Options"; // TODO internationalize, make string
    private static final CharSequence NORMAL_TITLE = "BlabberTabber";
    // Nav Drawer variables
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_menu_black_24px, R.string.drawer_open, R.string.drawer_close)
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
                tv.setText((CharSequence) speaker.getName());

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

    public void newMeeting(View v) {
        TheSpeakers.getInstance().reset();
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

    public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        getActionBar().setTitle(NORMAL_TITLE);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    public void onDrawerOpened(View view) {
        super.onDrawerOpened(view);
        getActionBar().setTitle(DRAWER_TITLE);
        invalidateOptionsMenu();  // creates call to onPrepareOptionsMenu
    }
}
