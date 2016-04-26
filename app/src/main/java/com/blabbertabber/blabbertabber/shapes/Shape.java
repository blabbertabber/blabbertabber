package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;

import math.geom2d.line.Line2D;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

public abstract class Shape {
    abstract Collection<Point2D> intersections(Shape s);

    Circle makeCircle(Circle2D c) {
        return new Circle(c);
    }

    Line makeLine(Line2D l) {
        return new Line(l);
    }
}
