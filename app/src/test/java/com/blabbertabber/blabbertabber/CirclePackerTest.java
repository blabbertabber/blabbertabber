package com.blabbertabber.blabbertabber;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.security.InvalidParameterException;

import math.geom2d.conic.Circle2D;

import static org.junit.Assert.assertEquals;


/**
 * tests CirclePacker
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CirclePackerTest {

    @Test
    public void packsOneCircleAsLargeAsPossible() {
        CirclePacker cp = new CirclePacker(72, 72);
        Circle2D[] circles = cp.addCircles(new double[]{10});
        assertEquals("The X coordinate should be 36", 36.0, circles[0].center().getX(), 0.1);
        assertEquals("The Y coordinate should be 36", 36.0, circles[0].center().getY(), 0.1);
        assertEquals("The radius should be 36", 36.0, circles[0].radius(), 0.1);
    }

    @Test
    public void packsTwoEqualCircles() {
        CirclePacker cp = new CirclePacker(4, 2);
        Circle2D[] circles = cp.addCircles(new double[]{1, 1});

        assertEquals("The first circle's X coordinate should be 1", 1.0, circles[0].center().getX(), 0.1);
        assertEquals("The first circle's Y coordinate should be 1", 1.0, circles[0].center().getY(), 0.1);

        assertEquals("The second circle's X coordinate should be 3", 3.0, circles[1].center().getX(), 0.1);
        assertEquals("The second circle's Y coordinate should be 1", 1.0, circles[1].center().getY(), 0.1);
    }

    @Test(expected = InvalidParameterException.class)
    public void packsTwoTooBigCircles() {
        CirclePacker cp = new CirclePacker(4, 2);
        cp.addCircles(new double[]{3, 2});
    }
}
