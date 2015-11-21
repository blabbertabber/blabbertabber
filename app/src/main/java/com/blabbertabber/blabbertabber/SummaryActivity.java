package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cunnie on 11/11/15.
 */
public class SummaryActivity extends Activity {

    private static final String TAG = "SummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
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
                Log.wtf(TAG, "speaker_duration_label_ " + id);
                tv = (TextView) findViewById(id);
                long speakerDuration = speaker.duration();
                double speakerPercent = 100 * (double) speakerDuration / (double) meetingDuration;
                String speakerStats = String.format(" %8s (%2.0f%%)", Helper.timeToHMMSS(speakerDuration), speakerPercent);
                tv.setText((CharSequence) speakerStats);

                id = R.id.class.getField("bar_speaker_" + i).getInt(0);
                Log.wtf(TAG, "bar_speaker_0 " + id);
                RectangleView rv = (RectangleView) findViewById(id);
                if (rv == null) {
                    Log.wtf(TAG, "rv is null!");
                } else {
                    rv.setVisible(true);
                    rv.setColor(speaker.getColor());
                    rv.setBarRatio((float) speakerDuration / (float) maxSpeakerDuration);
                    rv.invalidate();
                }
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
}
