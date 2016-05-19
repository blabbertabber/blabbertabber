package com.blabbertabber.blabbertabber;

import android.util.Log;

import com.blabbertabber.blabbertabber.shapes.Line;
import com.blabbertabber.blabbertabber.shapes.ShapeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;

/**
 * Created by brendancunnie on 4/30/16.
 */
public class Packing {
    private final static String TAG = "Packing";
    public ArrayList<ShapePair> shapePairs = new ArrayList<>();
    private ArrayList<Circle2D> packedCircles = new ArrayList<>();
    private ArrayList<Double> remainingRadii = new ArrayList<>();
    private List<Line> lines;
    private double x;
    private double y;

    Packing(double x, double y, ArrayList<Double> radii) {
        this.x = x;
        this.y = y;

        remainingRadii.addAll(radii);
        Log.i(TAG, "remainingRadii.size(): " + remainingRadii.size());
        Log.i(TAG, "radii.size(): " + radii.size());

        // initialize the first 4 shapePairs
        lines = ShapeFactory.makeLines(new Box2D(0, x, 0, y));
        for (int i = 0; i < lines.size() - 1; i++) {
            shapePairs.add(new ShapePair(lines.get(i), lines.get(i + 1)));
        }
        shapePairs.add(new ShapePair(lines.get(lines.size() - 1), lines.get(0)));
    }

    Packing(Packing p) {
        x = p.x;
        y = p.y;
        Collections.copy(packedCircles, p.packedCircles);
    }

    public List<ShapePair> getShapePairs() {
        ArrayList<ShapePair> safeCopyShapePairs = new ArrayList<>(shapePairs.size());
        safeCopyShapePairs.addAll(shapePairs);
        return safeCopyShapePairs;
    }

    public List<Line> getEdges() {
        return lines;
    }

    ArrayList<Circle2D> packNonRecursive() {
        if (remainingRadii.size() == 0) {
            return packedCircles;
        }
        double x = remainingRadii.get(0);
        double y = remainingRadii.get(0);
        Circle2D firstCircle = new Circle2D(x, y, remainingRadii.get(0));
        /// TODO: Add 2 ShapePairs to shapePairs
        packedCircles.add(firstCircle);
        remainingRadii.remove(0);
        return packRecursive(remainingRadii);
    }

    ArrayList<Circle2D> packRecursive(ArrayList<Double> remainingRadii) {
        Log.i(TAG, "packRecursive()");
        ArrayList<Circle2D> circles = new ArrayList<>();
        if (remainingRadii.size() == 0) {
            return packedCircles;
        }
        for (ShapePair shapePair : shapePairs)
            try {
                double nextCircleRadius = remainingRadii.remove(0);
                circles.addAll(placeNewCircle(nextCircleRadius, shapePair));
                circles = new Packing(this).packRecursive(remainingRadii);
                return circles;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "I couldn't pack!");
            }
        return new ArrayList<Circle2D>();
    }

    Collection<Circle2D> placeNewCircle(double radius, ShapePair corner) {
        ArrayList<Circle2D> placedCircles = new ArrayList<>();
        Collection<Point2D> positions = corner.positionsForNewCircle(radius);
        for (Point2D position : positions) {
            Circle2D circle = new Circle2D(position.x(), position.y(), radius);
            /// continue if circle is outside the box
            /// continue if circle collides with other circles
            placedCircles.add(circle);
        }
        return placedCircles;
    }
}
