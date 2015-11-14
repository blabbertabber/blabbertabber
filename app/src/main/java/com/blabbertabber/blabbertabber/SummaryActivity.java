package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    }

    public void newMeeting(View v) {
        TheSpeakers.getInstance().reset();
        Intent i = new Intent(this, RecordingActivity.class);
        startActivity(i);
    }
}
