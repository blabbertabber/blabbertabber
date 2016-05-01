package com.blabbertabber.blabbertabber.shapes;

import com.blabbertabber.blabbertabber.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;

import static junit.framework.Assert.assertNotNull;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ShapeTest {

    @Test
    public void testMakeCircleCreatesACircle() {
        Circle testCircle = ShapeFactory.makeCircle(new Circle2D(new Point2D(1, 1), 1));
        assertNotNull(testCircle);
    }

    @Test
    public void testMakeLineCreatesALine() {
        Line testLine = ShapeFactory.makeLine(new Line2D(new Point2D(1, 1), new Point2D(2, 2)));
        assertNotNull(testLine);
    }
}
