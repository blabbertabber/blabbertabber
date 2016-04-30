package com.blabbertabber.blabbertabber.shapes;

import com.blabbertabber.blabbertabber.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;

import static junit.framework.Assert.assertEquals;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CircleTest {

    @Test
    public void testCircleIntersectsCircle() {
        Shape firstCircle = Shape.makeCircle(new Circle2D(new Point2D(0, 0), 2));
        Shape secondCircle = Shape.makeCircle(new Circle2D(new Point2D(1, 1), 2));
        Collection<Point2D> points = firstCircle.intersections(secondCircle);
        assertEquals("There are exactly two points of intersection", 2, points.size());
//        assertEquals("The intersection is at (0,1)", new Point2D(0,1), points.toArray()[0]);
    }

    @Test
    public void testCircleIntersectsCircleTips() {
        Shape firstCircle = Shape.makeCircle(new Circle2D(new Point2D(0, 0), 1));
        Shape secondCircle = Shape.makeCircle(new Circle2D(new Point2D(2, 0), 1));
        Collection<Point2D> points = firstCircle.intersections(secondCircle);
        // Circle2D class has a bug, returning 2 points where there is one point of intersection
        // so we confirm the point(s) returned are all at the correct location.
        for (Point2D point : points) {
            assertEquals("The x-intersection is at 1", 1, point.x(), 0.000000001);
            assertEquals("The y-intersection is at 0", 0, point.y(), 0.000000001);
        }
    }

    @Test
    public void testSomeCirclesDontIntersect() {
        Shape firstCircle = Shape.makeCircle(new Circle2D(new Point2D(0, 0), 1));
        Shape secondCircle = Shape.makeCircle(new Circle2D(new Point2D(2, 0), 0.99));
        Collection<Point2D> points = firstCircle.intersections(secondCircle);
        assertEquals("The lines don't intersect", 0, points.size());
    }

    @Test
    public void testCirclesIntersectLines() {
        Shape circle = Shape.makeCircle(new Circle2D(new Point2D(0, 0), 1));
        Shape line = Shape.makeLine(new Line2D(new Point2D(-3, 0), new Point2D(3, 0)));
        Collection<Point2D> points = circle.intersections(line);
        assertEquals("There are exactly two points of intersection", 2, points.size());
        double sumX = 0;
        for (Point2D point : points) {
            sumX += point.x();
            assertEquals("The x-intersection is at 1", 1, Math.abs(point.x()), 0.000000001);
            assertEquals("The y-intersection is at 0", 0, point.y(), 0.000000001);
        }
        // the above test will pass if the X points are BOTH 1; this test makes sure that their signs oppose
        assertEquals("The sum of X points should be 0", 0, sumX, 0.0000000001);
    }
}
