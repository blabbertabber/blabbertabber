package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by cunnie on 11/11/15.
 */

@RunWith(AndroidJUnit4.class)
public class TimerTest {
    private static final String TAG = "TimerTest";
    private Timer timer;
    private long time;

    @Before
    public void before() {
        timer = new Timer();
    }

    @Test
    public void notStarted() {
        assertEquals("Not started, time elapsed should return '0' milliseconds", timer.time(), 0);
    }

    @Test
    public void started() {
        timer.start();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        time = timer.time();
        assertTrue("Started, time elapsed should return should return '50' milliseconds, +/- 40% " + time, time > 30 && time < 70);
    }

    @Test
    public void reset() {
        timer.start();
        try {
            Thread.currentThread().sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.reset();
        time = timer.time();
        assertEquals("Not started, time elapsed should return '0' milliseconds", timer.time(), 0);
    }

    @Test
    public void startedAfterReset() {
        timer.start();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.reset();
        timer.start();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        time = timer.time();
        assertTrue("Started, time elapsed should return should return '50' milliseconds, +/- 40% " + time, time > 30 && time < 70);
    }

    @Test
    public void startedStopped() {
        timer.start();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        time = timer.time();
        assertTrue("Started, time elapsed should return should return '50' milliseconds, +/- 40% " + time, time > 30 && time < 70);
    }

    @Test
    public void startedStoppedStartedStartedStopped() {
        timer.start();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.start();
        timer.start();
        timer.stop();
        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        time = timer.time();
        assertTrue("Started, time elapsed should return should return '50' milliseconds, +/- 40% " + time, time > 30 && time < 70);
    }
}
