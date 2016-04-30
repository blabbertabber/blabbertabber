package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

public class Circle extends Shape {
    private final Circle2D circle;

    Circle(Circle2D c) {
        circle = c;
    }

    Circle2D circle() {
        return circle;
    }

    @Override
    public Shape potentialLocationsForCenterOfCircleWithRadius(double radius) {
        Circle2D circle2D = new Circle2D(this.circle.center(), this.circle.radius() + radius);
        return Shape.makeCircle(circle2D);
    }

    @Override
    public Collection<Point2D> intersections(Shape s) {
        return s.intersections(this);
    }

    @Override
    public Collection<Point2D> intersections(Circle c) {
        return circle.intersections(c.circle);
    }

    @Override
    public Collection<Point2D> intersections(Line l) {
        return circle.intersections(l.line());
    }

}
