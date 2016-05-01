package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;

import math.geom2d.Point2D;

public interface Shape {
    Collection<Point2D> intersections(Shape s);

    Collection<Point2D> intersections(Line l);

    Collection<Point2D> intersections(Circle c);

    Shape potentialLocationsForCenterOfCircleWithRadius(double radius);
}
