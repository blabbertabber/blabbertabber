package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

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
        assertThat("Started, time elapsed should return should be greater than 49 ms", time, greaterThan(49L));
        assertThat("Started, time elapsed should return should be less than 100 ms", time, lessThan(100L));
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
        assertThat("Started, time elapsed should return should be greater than 49 ms", time, greaterThan(49L));
        assertThat("Started, time elapsed should return should be less than 100 ms", time, lessThan(100L));
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
        assertThat("Started, time elapsed should return should be greater than 49 ms", time, greaterThan(49L));
        assertThat("Started, time elapsed should return should be less than 100 ms", time, lessThan(100L));
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
        assertThat("Started, time elapsed should return should be greater than 49 ms", time, greaterThan(49L));
        assertThat("Started, time elapsed should return should be less than 100 ms", time, lessThan(100L));
    }

    @Test
    public void startedStoppedStartedStopped() {
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
        assertThat("Started, time elapsed should return should be greater than 99 ms", time, greaterThan(99L));
        assertThat("Started, time elapsed should return should be less than 150 ms", time, lessThan(150L));
    }

    @Test
    public void startedStoppedStartedStoppedStartedStopped() {
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
        assertThat("Started, time elapsed should return should be greater than 149 ms", time, greaterThan(149L));
        assertThat("Started, time elapsed should return should be less than 250 ms", time, lessThan(250L));
    }

    @Test
    public void constructorWithElapsedTime() {
        timer = new Timer(120);
        assertEquals("Time elapsed should be 120 ms", 120L, timer.time());
    }
}
