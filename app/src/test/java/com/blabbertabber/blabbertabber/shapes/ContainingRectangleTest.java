package com.blabbertabber.blabbertabber.shapes;

import com.blabbertabber.blabbertabber.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import math.geom2d.Box2D;
import math.geom2d.conic.Circle2D;

import static junit.framework.Assert.assertEquals;


/**
 * Created by cunnie on 6/4/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ContainingRectangleTest {

    @Test
    public void placesOneCircle() {
        ContainingRectangle cr = new ContainingRectangle(new Box2D(0, 3, 0, 3));
        List<Double> radii = new ArrayList<>();
        radii.add(1.0);
        Collection<Stack<Circle2D>> csc = cr.placeFirstCircle(radii);
        Stack<Circle2D> circleStack = csc.iterator().next();
        Circle2D soleCircle = circleStack.peek();
        assertEquals("The X coordinate is at 1", 1.0, soleCircle.center().x(), 0.001);
        assertEquals("The Y coordinate is at 1", 1.0, soleCircle.center().y(), 0.001);
    }

    @Test
    public void placesTwoCircles() {
        ContainingRectangle cr = new ContainingRectangle(new Box2D(0, 5, 0, 5));
        List<Double> radii = new ArrayList<>();
        radii.add(1.5);
        radii.add(1.0);
        Collection<Stack<Circle2D>> cscSolutions = cr.placeFirstCircle(radii);
        Stack<Circle2D> solution = cscSolutions.iterator().next();
        Circle2D soleCircle = solution.pop();
        assertEquals("The X coordinate is at 1", 4.0, soleCircle.center().x(), 0.001);
        assertEquals("The Y coordinate is at 1", 1.0, soleCircle.center().y(), 0.001);
        soleCircle = solution.pop();
        assertEquals("The X coordinate is at 1", 1.5, soleCircle.center().x(), 0.001);
        assertEquals("The Y coordinate is at 1", 1.5, soleCircle.center().y(), 0.001);
    }

    @Test
    public void placesThreeCircles() {
        ContainingRectangle cr = new ContainingRectangle(new Box2D(0, 5, 0, 5));
        List<Double> radii = new ArrayList<>();
        radii.add(1.5);
        radii.add(1.0);
        radii.add(0.5);
        Collection<Stack<Circle2D>> cscSolutions = cr.placeFirstCircle(radii);
        Stack<Circle2D> solution = cscSolutions.iterator().next();
        Circle2D soleCircle = solution.pop();
        assertEquals("The X coordinate is at 1", 4.5, soleCircle.center().x(), 0.001);
        assertEquals("The Y coordinate is at 1", 4.5, soleCircle.center().y(), 0.001);
        soleCircle = solution.pop();
        assertEquals("The X coordinate is at 1", 4.0, soleCircle.center().x(), 0.001);
        assertEquals("The Y coordinate is at 1", 1.0, soleCircle.center().y(), 0.001);
        soleCircle = solution.pop();
        assertEquals("The X coordinate is at 1", 1.5, soleCircle.center().x(), 0.001);
        assertEquals("The Y coordinate is at 1", 1.5, soleCircle.center().y(), 0.001);
    }
}
