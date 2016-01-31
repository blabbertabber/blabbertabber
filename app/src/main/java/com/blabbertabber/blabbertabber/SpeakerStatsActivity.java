package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SpeakerStatsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_stats);
        TextView tv = (TextView) findViewById(R.id.speakerStatView);
        String filePathName = getIntent().getExtras().getString("path");
//        String filePathName = getFilesDir() + "/" + AudioEventProcessor.RECORDER_FILENAME_NO_EXTENSION + ".mfc";
        try {
            File file = new File(filePathName);
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            InputStream input = new BufferedInputStream(new FileInputStream(filePathName));
            input.read(bytes, 0, bytes.length);
            CharSequence fileContents = new String(bytes);
            tv.setText(fileContents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            tv.setText("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            tv.setText("IOException: " + e.getMessage());
        }

    }
}
