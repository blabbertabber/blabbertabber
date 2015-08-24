package com.blabbertabber.blabbertabber.tests;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;

import com.blabbertabber.blabbertabber.MainActivity;
import com.blabbertabber.blabbertabber.R;
import com.blabbertabber.blabbertabber.RecordingActivity;

/**
 * Created by cunnie on 8/22/15.
 * Test class to confirm record_button launches recording Activity.
 */
public class LaunchRecordingActivityTest extends ActivityUnitTestCase<MainActivity> {
    Intent mLaunchIntent;

    public LaunchRecordingActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(getInstrumentation()
                .getTargetContext(), MainActivity.class);
        startActivity(mLaunchIntent, null, null);
        final ImageButton launchNextButton =
                (ImageButton) getActivity()
                        .findViewById(R.id.button_record);
    }

    @SmallTest
    public void testNextActivityWasLaunchedWithIntent() {
//        startActivity(mLaunchIntent, null, null);
//        final ImageButton recordButton =
//                (ImageButton) getActivity()
//                        .findViewById(R.id.button_record);
//        recordButton.performClick();

//        final Intent launchIntent = new Intent(getActivity(), RecordingActivity.class);
//        assertNotNull("Intent was null", launchIntent);
//        assertTrue(isFinishCalled());

//        final String payload =
//                launchIntent.getStringExtra(NextActivity.EXTRAS_PAYLOAD_KEY);
//        assertEquals("Payload is empty", LaunchActivity.STRING_PAYLOAD, payload);
    }
}

