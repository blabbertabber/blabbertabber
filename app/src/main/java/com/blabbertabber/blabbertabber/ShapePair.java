package com.blabbertabber.blabbertabber;

import com.blabbertabber.blabbertabber.shapes.Shape;

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

    public Collection<Point2D> positionsForNewCircle(double radius) {
        Shape locationsForCircleCenter1 = shape1.potentialLocationsForCenterOfCircleWithRadius(radius);
        Shape locationsForCircleCenter2 = shape2.potentialLocationsForCenterOfCircleWithRadius(radius);
        return locationsForCircleCenter1.intersections(locationsForCircleCenter2);
    }
}
