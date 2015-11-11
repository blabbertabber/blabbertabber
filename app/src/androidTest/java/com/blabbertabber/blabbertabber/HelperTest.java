package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 11/11/15.
 */

@RunWith(AndroidJUnit4.class)
public class HelperTest {
    private static final String TAG = "HelperTest";

    @Test
    public void testZero_0Duration() {
        assertEquals("Zero-duration should return '0.0'", Helper.timeToHMS(0), "0.0");
    }
    @Test
    public void testZero_1Duration() {
        assertEquals("50ms should return '0.0'", Helper.timeToHMS(50), "0.0");
    }

    @Test
    public void testPoint1_0Duration() {
        assertEquals("51ms should return '0.1'", "0.1", Helper.timeToHMS(51));
    }

    @Test
    public void testPoint1_1Duration() {
        assertEquals("149ms should return '0.1'", "0.1", Helper.timeToHMS(149));
    }

    @Test
    public void testPoint59Duration() {
        assertEquals("59_900ms should return '59.9'", "59.9", Helper.timeToHMS(59_900));
    }

    @Test
    public void testPoint1M_0Duration() {
        assertEquals("60_000ms should return '1:00.0'", "1:00.0", Helper.timeToHMS(60_000));
    }

    @Test
    public void testPoint1M_1Duration() {
        assertEquals("60_001ms should return '1:00.0'", "1:00.0", Helper.timeToHMS(60_001));
    }

    @Test
    public void testPoint1M_2Duration() {
        assertEquals("60_051ms should return '1:00.1'", "1:00.1", Helper.timeToHMS(60_051));
    }

    @Test
    public void testPoint10M_0Duration() {
        assertEquals("599_949ms should return '9:59.9'", "9:59.9", Helper.timeToHMS(599_949));
    }

    @Test
    public void testPoint10M_1Duration() {
        assertEquals("600_050ms should return '10:00.0'", "10:00.0", Helper.timeToHMS(600_050));
    }

    @Test
    public void testPoint1H_0Duration() {
        assertEquals("3_599_949ms should return '59:59.9'", "59:59.9", Helper.timeToHMS(3_599_949));
    }

    @Test
    public void testPoint1H_1Duration() {
        assertEquals("3_600_050ms should return '1:00:00.0'", "1:00:00.0", Helper.timeToHMS(3_600_050));
    }
}
