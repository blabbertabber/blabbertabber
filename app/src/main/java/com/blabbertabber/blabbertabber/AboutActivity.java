package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Log.i(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        TextView version = (TextView) findViewById(R.id.aboutVersion);
        version.setText(getString(R.string.app_name)
                + " "
                + getString(R.string.about_version)
                + ": "
                + BuildConfig.VERSION_NAME
                + "\n");
    }

    public void finish(View v) {
        finish();
    }
}
