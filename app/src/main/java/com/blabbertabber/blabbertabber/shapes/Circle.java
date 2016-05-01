package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

public class Circle implements Shape {
    private final Circle2D circle;

    Circle(Circle2D c) {
        circle = c;
    }

    Circle2D circle() {
        return circle;
    }

    public Shape potentialLocationsForCenterOfCircleWithRadius(double radius) {
        Circle2D circle2D = new Circle2D(this.circle.center(), this.circle.radius() + radius);
        return ShapeFactory.makeCircle(circle2D);
    }

    public Collection<Point2D> intersections(Shape s) {
        return s.intersections(this);
    }

    public Collection<Point2D> intersections(Circle c) {
        return circle.intersections(c.circle);
    }

    public Collection<Point2D> intersections(Line l) {
        return circle.intersections(l.line());
    }

}
