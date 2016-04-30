package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.line.Line2D;

public class Line extends Shape {
    private final Line2D line;

    Line(Line2D l) {
        line = l;
    }

    public Line2D line() {
        return line;
    }

    @Override
    public Collection<Point2D> intersections(Shape s) {
        return s.intersections(this);
    }

    public Collection<Point2D> intersections(Circle c) {
        return c.circle().intersections(line);
    }

    public Collection<Point2D> intersections(Line l) {
        return line.intersections(l.line);
    }
}
