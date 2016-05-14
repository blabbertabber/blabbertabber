package com.blabbertabber.blabbertabber.shapes;

import java.util.ArrayList;
import java.util.List;

import math.geom2d.Box2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.LinearShape2D;

/**
 * Created by brendancunnie on 5/7/16.
 */
public class ShapeFactory {

    public static Circle makeCircle(Circle2D c) {
        return new Circle(c);
    }

    static Line makeLine(LinearShape2D l) {
        return new Line(l);
    }

    public static List<Line> makeLines(Box2D box) {
        Line.setEnclosingBox(box);
        ArrayList<Line> lines = new ArrayList<>();
        for (LinearShape2D linearShape : box.edges()) {
            lines.add(makeLine(linearShape));
        }
        return lines;
    }
}
