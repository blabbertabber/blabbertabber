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
    Collection<Point2D> intersections(Shape s) {
        return s.intersections(this);
    }

    Collection<Point2D> intersections(Circle c) {
        return circle.intersections(c.circle);
    }

    Collection<Point2D> intersections(Line l) {
        return circle.intersections(l.line());
    }

}
