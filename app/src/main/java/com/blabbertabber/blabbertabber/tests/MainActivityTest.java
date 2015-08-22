package com.blabbertabber.blabbertabber.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.blabbertabber.blabbertabber.MainActivity;
import com.blabbertabber.blabbertabber.R;

/**
 * Created by cunnie on 8/22/15.
 * Tests the Main activity by confirming presence and functionality of record_button.
 */
//public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
//}

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private View mRecordingButton;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);

        mMainActivity = getActivity();
        mRecordingButton = mMainActivity.findViewById(R.id.button_record);

    }

    @SmallTest
    public void testClickMeButton_layout() {
        final View decorView = mMainActivity.getWindow().getDecorView();

        ViewAsserts.assertOnScreen(decorView, mRecordingButton);

        final ViewGroup.LayoutParams layoutParams =
                mRecordingButton.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, 200);
        assertEquals(layoutParams.height, 200);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

//    public void testOnCreate() throws Exception {
//        super.testOnCreate();
//    }

//    public void testOnCreateOptionsMenu() throws Exception {
//        super.testOnCreateOptionsMenu();
//    }
//
//    public void testOnOptionsItemSelected() throws Exception {
//
//    }
}
