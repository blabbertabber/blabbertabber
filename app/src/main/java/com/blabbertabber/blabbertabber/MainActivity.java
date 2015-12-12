package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private boolean mFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.launch_recording_activity);
        Log.i(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // http://developer.android.com/training/basics/data-storage/shared-preferences.html
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mFirstTime = sharedPref.getBoolean(getString(R.string.first_time), mFirstTime);

        if (!mFirstTime) {
            launchRecordingActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.first_time), mFirstTime);
        editor.apply();
    }

    public void launchRecordingActivity() {
        mFirstTime = false;

        Intent intent = new Intent(this, RecordingActivity.class);
        startActivity(intent);
    }

    // 2nd signature of launchRecordingActivity to accommodate activity_main.xml's
    // requirement to pass in a View (which is never used)
    public void launchRecordingActivity(View view) {
        launchRecordingActivity();
    }

    // needed for testing
    public boolean getFirstTime() {
        return mFirstTime;
    }
}
