package com.blabbertabber.blabbertabber.shapes;

import com.blabbertabber.blabbertabber.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;

import static junit.framework.Assert.assertEquals;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LineTest {

    @Test
    public void testLineIntersectsLine() {
        Shape firstLine = ShapeFactory.makeLine(new Line2D(new Point2D(0, 0), new Point2D(0, 2)));
        Shape secondLine = ShapeFactory.makeLine(new Line2D(new Point2D(-1, 1), new Point2D(1, 1)));
        Collection<Point2D> points = firstLine.intersections(secondLine);
        assertEquals("There is exactly one point of intersection", 1, points.size());
        assertEquals("The intersection is at (0,1)", new Point2D(0, 1), points.toArray()[0]);
    }

    @Test
    public void testLineIntersectsLineTips() {
        Shape firstLine = ShapeFactory.makeLine(new Line2D(new Point2D(0, 0), new Point2D(0, 2)));
        Shape secondLine = ShapeFactory.makeLine(new Line2D(new Point2D(0, 0), new Point2D(2, 0)));
        Collection<Point2D> points = firstLine.intersections(secondLine);
        assertEquals("There is exactly one point of intersection", 1, points.size());
        assertEquals("The intersection is at (0,1)", new Point2D(0, 0), points.toArray()[0]);
    }

    @Test
    public void testParallelLinesDontIntersect() {
        Shape firstLine = ShapeFactory.makeLine(new Line2D(new Point2D(0, 0), new Point2D(0, 2)));
        Shape secondLine = ShapeFactory.makeLine(new Line2D(new Point2D(2, 0), new Point2D(2, 2)));
        Collection<Point2D> points = firstLine.intersections(secondLine);
        assertEquals("The lines don't intersect", 0, points.size());
    }

    @Test
    public void testLinesIntersectCircles() {
        Shape line = ShapeFactory.makeLine(new Line2D(new Point2D(-3, 0), new Point2D(3, 0)));
        Shape circle = ShapeFactory.makeCircle(new Circle2D(new Point2D(0, 0), 1));
        Collection<Point2D> points = line.intersections(circle);
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

    @Test
    public void testLocationsForCenterOfCircleWithRadiusReturnsParallelVerticalLine() {
        Collection<Line> lines = ShapeFactory.makeLines(new Box2D(0, 10, 0, 5));
        ArrayList<Shape> parallelLines = new ArrayList<>(4);
        double radius = 2;
        for (Line edge : lines) {
            parallelLines.add(edge.potentialLocationsForCenterOfCircleWithRadius(radius));
        }
        for (Shape line : parallelLines) {
            System.out.println("The parallel line is " + line);
        }

        ArrayList<Line> expectedParallelLines = new ArrayList<>();
        expectedParallelLines.add(ShapeFactory.makeLine(new Line2D(10, 3, 0, 3)));  // top
        expectedParallelLines.add(ShapeFactory.makeLine(new Line2D(0, 2, 10, 2)));  // left
        expectedParallelLines.add(ShapeFactory.makeLine(new Line2D(2, 0, 2, 5)));   // bottom
        expectedParallelLines.add(ShapeFactory.makeLine(new Line2D(8, 0, 8, 5)));   // right
        assertEquals("The number of parallel lines within the box should be 4", 4, parallelLines.size());
        long notNull = 0;
        long parallelCount = 0;
        for (Shape line : parallelLines) {
            if (line == null) {
                notNull++;
            }
            if (expectedParallelLines.contains(line)) {
                parallelCount++;
            }
        }
        assertEquals("The parallel lines within the box should not be null", 0, notNull);
        assertEquals("Four parallel lines should match the expected four", 4, parallelCount);
    }

}
