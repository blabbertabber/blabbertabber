package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import math.geom2d.conic.Circle2D;

import static org.junit.Assert.assertEquals;


/**
 * tests CirclePacker
 */
@RunWith(AndroidJUnit4.class)
public class CirclePackerTest {

    @Test
    public void packsOneCircleAsLargeAsPossible() {
        CirclePacker cp = new CirclePacker(72, 72);
        Circle2D[] circles = cp.addCircle(new double[]{10});
        assertEquals("The X coordinate should be 36", 36.0, circles[0].center().getX(), 0.1);
        assertEquals("The Y coordinate should be 36", 36.0, circles[0].center().getY(), 0.1);
        assertEquals("The radius should be 36", 36.0, circles[0].radius(), 0.1);
    }
}
