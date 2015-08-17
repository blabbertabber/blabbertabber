package com.blabbertabber.blabbertabber.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blabbertabber.blabbertabber.MainActivity;

import junit.framework.TestCase;

/**
 * Created by cunnie on 8/9/15.
 * Tests MainActivity by simulating a click on the record button.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private View mRecordingButton;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        setActivityInitialTouchMode(true);
//
//        mMainActivity = getActivity();
//        mRecordingButton = (Button)
//                mMainActivity.findViewById(R.id.button_record);

    }

    public void tearDown() throws Exception {
    }

    public void testOnCreate() throws Exception {

    }

    public void testOnCreateOptionsMenu() throws Exception {

    }

    public void testOnOptionsItemSelected() throws Exception {

    }
}