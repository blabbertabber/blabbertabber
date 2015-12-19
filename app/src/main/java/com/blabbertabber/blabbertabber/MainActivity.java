package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * The splash screen.
 * It is shown only the first time the application is openned.
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static boolean resetFirstTime = false;
    private boolean mFirstTime = true;
    private int rushLimbaughIsWrongCount = 0;

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
        Log.i(TAG, "onResume()");
        // http://developer.android.com/training/basics/data-storage/shared-preferences.html
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mFirstTime = sharedPref.getBoolean(getString(R.string.first_time), mFirstTime);

        if (!mFirstTime && !resetFirstTime) {
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
        resetFirstTime = false;

        Intent intent = new Intent(this, RecordingActivity.class);
        startActivity(intent);
    }

    // 2nd signature of launchRecordingActivity to accommodate activity_main.xml's
    // requirement to pass in a View (which is never used)
    public void launchRecordingActivity(View view) {
        launchRecordingActivity();
    }

    // Easter Egg for new users
    public void rushLimbaughIsWrong(View v) {
        Log.i(TAG, "rushLimbaughIsWrong()");
        rushLimbaughIsWrongCount += 1;
        if (rushLimbaughIsWrongCount > 3) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/search?q=rush+limbaugh+wrong"));
            if (intent.resolveActivity(getPackageManager()) != null) {
                Log.v(TAG, "rushLimbaughIsWrong(): resolved activity");
                startActivity(intent);
            } else {
                Log.v(TAG, "rushLimbaughIsWrong(): couldn't resolve activity");
            }
        }
    }

    // needed for testing
    public boolean getFirstTime() {
        return mFirstTime;
    }
}
