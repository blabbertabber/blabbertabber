package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Shares output, including speaker times, .seg files, etc...
 */

public class SpeakerStatsActivity extends Activity {
    // We don't want to attempt to display a 1.5MB binary .wav file
    // 32kB is enough to get a sense of what we're looking at
    private static final int MAX_DISPLAY_SIZE = 32_768;
    private String filePathName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_stats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tv = (TextView) findViewById(R.id.speakerStatView);

        filePathName = getIntent().getExtras().getString("path");
        try {
            byte[] bytes = new byte[MAX_DISPLAY_SIZE];
            InputStream input = new BufferedInputStream(new FileInputStream(filePathName));
            input.read(bytes, 0, MAX_DISPLAY_SIZE);
            CharSequence fileContents = new String(bytes);
            tv.setText(fileContents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            tv.setText(String.format("FileNotFoundException: %s", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            tv.setText(String.format("IOException: %s", e.getMessage()));
        }
    }

    public void share(View view) {
        File fileToShare = new File(filePathName);
        Uri fileToShareUri = getUriForFile(getApplicationContext(),
                "com.blabbertabber.blabbertabber.fileprovider", fileToShare);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, fileToShareUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "BlabberTabber_" + fileToShare.getName());
        intent.setType("application/octet-stream");
        startActivity(intent);
    }
}
