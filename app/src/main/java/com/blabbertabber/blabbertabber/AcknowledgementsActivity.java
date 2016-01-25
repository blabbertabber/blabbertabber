package com.blabbertabber.blabbertabber;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class AcknowledgementsActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledgements);
    }

    public void finish(View v) {
        finish();
    }
}
