package com.blabbertabber.blabbertabber;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import math.geom2d.conic.Circle2D;

import static org.junit.Assert.assertEquals;


/**
 * tests Packing
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PackingTest {

    @Test
    public void packsZeroCircles() {
        ArrayList<Double> radii = new ArrayList<Double>();
        Packing p = new Packing(2, 2, radii);
        ArrayList<Circle2D> circles = p.pack();
        assertEquals("Zero circles are packed", new ArrayList<Circle2D>(), circles);
    }
}
