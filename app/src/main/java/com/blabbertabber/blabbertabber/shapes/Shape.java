package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;

public abstract class Shape {
    static Circle makeCircle(Circle2D c) {
        return new Circle(c);
    }

    static Line makeLine(Line2D l) {
        return new Line(l);
    }

    abstract Collection<Point2D> intersections(Shape s);
}
