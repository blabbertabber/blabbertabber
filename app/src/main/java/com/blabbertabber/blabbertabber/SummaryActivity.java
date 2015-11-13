package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
    }
}
