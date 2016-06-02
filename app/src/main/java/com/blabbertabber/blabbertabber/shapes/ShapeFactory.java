package com.blabbertabber.blabbertabber.shapes;

import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import math.geom2d.Box2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.LinearShape2D;

/**
 * Created by brendancunnie on 5/7/16.
 */
public class ShapeFactory {
    static final String TAG = "ShapeFactory";

    public static Circle makeCircle(Circle2D c) {
        return new Circle(c);
    }

    // TODO:  see if we can rewrites tests so this is package protected
    public static Line makeLine(LinearShape2D l) {
        Log.i(TAG, "makeLines() l: " + l);
        return new Line(l);
    }

    public static List<Line> makeLines(Box2D box) throws InvalidParameterException {
        Log.i(TAG, "makeLines() box: " + box);
        if (box.getHeight() == 0 || box.getWidth() == 0) {
            throw new InvalidParameterException("The box has 0 area!");
        }
        Line.setEnclosingBox(box);
        ArrayList<Line> lines = new ArrayList<>();
        for (LinearShape2D linearShape : box.edges()) {
            lines.add(makeLine(linearShape));
        }
        return lines;
    }
}
