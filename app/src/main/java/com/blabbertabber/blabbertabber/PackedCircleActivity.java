package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class PackedCircleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packed_circle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CircleView cv = new CircleView(this);
        FrameLayout fl = (FrameLayout) findViewById(R.id.speaker_circle_frame_layout);
        fl.addView(cv);
    }
}
