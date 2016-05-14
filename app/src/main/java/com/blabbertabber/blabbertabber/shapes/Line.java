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

    private boolean pointsEqual(Point2D p1, Point2D p2) {
        return (p1.x() == p2.x()) && (p1.y() == p2.y());
    }

    @Override
    public boolean equals(Object l) {
        if (l instanceof Line) {
            LinearShape2D line2 = ((Line) l).line();
            Point2D firstPoint = line.firstPoint();
            Point2D firstPoint2 = line2.firstPoint();
            Point2D lastPoint = line.lastPoint();
            Point2D lastPoint2 = line2.lastPoint();
            return (pointsEqual(firstPoint, firstPoint2) && pointsEqual(lastPoint, lastPoint2)) ||
                    (pointsEqual(firstPoint, lastPoint2) && pointsEqual(lastPoint, firstPoint2));
        }
        return false;
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
