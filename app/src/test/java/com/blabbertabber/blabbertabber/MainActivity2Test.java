package com.blabbertabber.blabbertabber;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivity2Test {

    @Test
    public void testSomething() throws Exception {
        assertTrue(Robolectric.setupActivity(MainActivity.class) != null);
    }

}
