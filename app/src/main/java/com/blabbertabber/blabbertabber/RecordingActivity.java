package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by cunnie on 8/16/15.
 * Activity to record and identify voices.
 *
 */
public class RecordingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
    }

}
