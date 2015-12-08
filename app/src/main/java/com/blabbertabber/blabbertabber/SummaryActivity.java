package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private NavigationView mNavigationView;
//    private ActionBarDrawerToggle mDrawerToggle;
//    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        // If you don't setContentView, you'll get either IllegalArgumentException or NullPointerException
        setContentView(R.layout.activity_summary);
        // Nav Drawer, http://stackoverflow.com/questions/26082467/android-on-drawer-closed-listener
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mNavigationView = (NavigationView) findViewById(R.id.left_drawer);
//        if (mDrawerLayout == null) {
//            Log.wtf(TAG, "onCreate() mDrawerLayout is NULL!");
//            return;
//        } else {
//            Log.i(TAG, "onCreate() mDrawerLayout is not null!");
//        }
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
//                R.string.drawer_open, R.string.drawer_close) {
//            public void onDrawerClosed(View view) {
//                super.onDrawerClosed(view);
//                getActionBar().setTitle(NORMAL_TITLE);
//                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//
//            public void onDrawerOpened(View view) {
//                super.onDrawerOpened(view);
//                getActionBar().setTitle(DRAWER_TITLE);
//                invalidateOptionsMenu();  // creates call to onPrepareOptionsMenu
//            }
//        };
        // Set the drawer toggle as the DrawerListener
//        mNavigationView.setNavigationItemSelectedListener(new DrawerItemClickListener());
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

    public void replayMeeting(View v) {
        Toast.makeText(getApplicationContext(), "Playing back the meeting", Toast.LENGTH_LONG).show();
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

    /* Called whenever we call invalidateOptionsMenu() */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }
//
//    private void setupDrawerContent(NavigationView navigationView) {
//        navigationView.setNavigationItemSelectedListener(
//                new NavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(MenuItem menuItem) {
//                        menuItem.setChecked(true);
//                        mDrawerLayout.closeDrawers();
//                        return true;
//                    }
//                }
//        );
//    }
}
