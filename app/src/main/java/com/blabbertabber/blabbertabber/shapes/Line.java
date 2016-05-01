package com.blabbertabber.blabbertabber.shapes;

import java.util.Collection;
import java.util.Locale;

import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.LinearShape2D;

public class Line implements Shape {
    private static Box2D enclosingBox;
    private final LinearShape2D line;

    Line(LinearShape2D l) {
        line = l;
    }

    static void setEnclosingBox(Box2D box) {
        enclosingBox = box;
    }

    public LinearShape2D line() {
        return line;
    }

    public Shape potentialLocationsForCenterOfCircleWithRadius(double radius) {
        LinearShape2D leftOrBelowParallelLine = ((LineSegment2D) line).parallel(-radius);
        LinearShape2D rightOrAboveParallelLine = ((LineSegment2D) line).parallel(radius);
        if (enclosingBox.containsBounds(leftOrBelowParallelLine)) {
            return ShapeFactory.makeLine(leftOrBelowParallelLine);
        } else if (enclosingBox.containsBounds(rightOrAboveParallelLine)) {
            return ShapeFactory.makeLine(rightOrAboveParallelLine);
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "[ ( %f, %f )( %f, %f ) ]",
                line.firstPoint().x(), line.firstPoint().y(),
                line.lastPoint().x(), line.lastPoint().y());
    }

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
