package com.blabbertabber.blabbertabber.shapes;

import com.blabbertabber.blabbertabber.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.line.Line2D;

import static junit.framework.Assert.assertEquals;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LineTest {

    @Test
    public void testLineIntersectsLine() throws Exception {
        Shape firstLine = Shape.makeLine(new Line2D(new Point2D(0, 0), new Point2D(0, 2)));
        Shape secondLine = Shape.makeLine(new Line2D(new Point2D(-1, 1), new Point2D(1, 1)));
        Collection<Point2D> points = firstLine.intersections(secondLine);
        assertEquals("There is exactly one point of intersection", 1, points.size());
    }
}
