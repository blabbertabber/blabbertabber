package com.blabbertabber.blabbertabber;

import com.blabbertabber.blabbertabber.shapes.Shape;

import java.util.Arrays;
import java.util.Collection;

import math.geom2d.Point2D;

/**
 * Created by brendancunnie on 4/30/16.
 */
public class ShapePair {
    private Shape shape1;
    private Shape shape2;

    public ShapePair(Shape shape1, Shape shape2) {
        this.shape1 = shape1;
        this.shape2 = shape2;
    }

    /**
     * Create 2 new ShapePairs where <code>shape</code> touches each Shape in this ShapePair.
     * Ideally, we should confirm that each of this ShapePair's Shapes intersect the
     * <code>shape</code> parameter at exactly one point.  But because of the vagaries of floating
     * point calculations we are not performing that check.
     * Possible future check:  if there *are* 2 points of intersection, confirm they are
     * *extremely* close to each other.  Close enough to be considered a single point for practical
     * purposes.
     *
     * @param shape The new Shape from which to construct 2 new ShapePairs: one against each of
     *              the Shapes in this ShapePair.
     * @return A Collection of the 2 new ShapePairs created.
     */
    public Collection<ShapePair> newShapePairs(Shape shape) {
        return Arrays.asList(new ShapePair[]{new ShapePair(shape1, shape), new ShapePair(shape2, shape)});
    }

    public Collection<Point2D> positionsForNewCircle(double radius) {
        Shape locationsForCircleCenter1 = shape1.potentialLocationsForCenterOfCircleWithRadius(radius);
        Shape locationsForCircleCenter2 = shape2.potentialLocationsForCenterOfCircleWithRadius(radius);
        return locationsForCircleCenter1.intersections(locationsForCircleCenter2);
    }
}
