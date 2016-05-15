package com.blabbertabber.blabbertabber;

import com.blabbertabber.blabbertabber.shapes.Circle;
import com.blabbertabber.blabbertabber.shapes.Line;
import com.blabbertabber.blabbertabber.shapes.Shape;
import com.blabbertabber.blabbertabber.shapes.ShapeFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.LinearShape2D;

import static junit.framework.Assert.assertTrue;
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
        ArrayList<Circle2D> circles = p.packNonRecursive();
        assertEquals("Zero circles are packed", new ArrayList<Circle2D>(), circles);
    }

    @Test
    public void withNoCirclesWeHave4ShapePairs() {
        ArrayList<Double> radii = new ArrayList<Double>();
        Packing p = new Packing(2, 2, radii);
        ArrayList<Circle2D> circles = p.packNonRecursive();
        List<ShapePair> shapePairs = p.getShapePairs();
        assertEquals("With Zero circles packed, I have 4 shape pairs", 4, shapePairs.size());
    }

    @Test
    public void withNoCirclesTheThirdShapePairShouldBeInTheLowerLeft() {
        ArrayList<Double> radii = new ArrayList<Double>();
        Packing p = new Packing(20, 20, radii);
        ArrayList<Circle2D> circles = p.packNonRecursive();
        List<ShapePair> shapePairs = p.getShapePairs();
        Collection<Point2D> points = shapePairs.get(2).positionsForNewCircle(10);
        assertEquals("An edge/edge shape pair should have only 1 position for a new circle", 1, points.size());
    }

    @Test
    public void packsOneCircle() {
        ArrayList<Double> radii = new ArrayList<Double>();
        radii.add(10.0);
        Packing p = new Packing(200.0, 300.0, radii);
        ArrayList<Circle2D> circles = p.packNonRecursive();
        assertEquals("One circle is packed", 1, circles.size());
    }

    // One shape pair should be circle/bottom.  Another should be circle/left.
    public void withOneCircleTheNewShapePairsShouldBeAgainstTheBottomAndLeftEdges() {
        ArrayList<Double> radii = new ArrayList<Double>();
        radii.add(10.0);
        Packing p = new Packing(20, 20, radii);
        ArrayList<Circle2D> circles = p.packNonRecursive();
        List<ShapePair> shapePairs = p.getShapePairs();
        Circle circle = ShapeFactory.makeCircle(circles.get(0));
        Line bottomEdge = ShapeFactory.makeLine(new Line2D(new Point2D(0,0), new Point2D(20,0)));
        Line leftEdge = ShapeFactory.makeLine(new Line2D(new Point2D(0,0), new Point2D(0,20)));
        ShapePair circleBottom = new ShapePair(circle, bottomEdge);
        ShapePair circleLeft = new ShapePair(circle, leftEdge);
        boolean foundCircleBottom = false;
        boolean foundCircleLeft = false;
        for (ShapePair shapePair : shapePairs) {
            if (shapePair.equals(circleBottom)) {
                foundCircleBottom = true;
            }
            if (shapePair.equals(circleLeft)) {
                foundCircleLeft = true;
            }
        }
        /// one shape pair should be circle/bottom.  Another should be circle/left
        assertTrue("One of the shape pairs should be the circle touching the bottom edge", foundCircleBottom);
        assertTrue("One of the shape pairs should be the circle touching the left edge", foundCircleLeft);
    }

        @Test
    public void packsTwoCircle() {
        ArrayList<Double> radii = new ArrayList<Double>();
        radii.add(20.0);
        radii.add(10.0);
        Packing p = new Packing(200.0, 300.0, radii);
        ArrayList<Circle2D> circles = p.packNonRecursive();
        assertEquals("Two circles are packed", 2, circles.size());
    }
}
