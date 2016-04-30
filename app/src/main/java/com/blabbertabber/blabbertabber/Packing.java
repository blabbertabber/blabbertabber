package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import math.geom2d.conic.Circle2D;

/**
 * Created by brendancunnie on 4/30/16.
 */
public class Packing {
    private final static String TAG = "Packing";
    private ArrayList<Circle2D> circles;
    private ArrayList<Double> remainingRadii;
    private ArrayList<ShapePair> shapePairs;
    private double x;
    private double y;

    Packing(double x, double y, ArrayList<Double> radii) {
        this.x = x;
        this.y = y;
        Collections.copy(remainingRadii, radii);
    }

    Packing(Packing p) {
        x = p.x;
        y = p.y;
        Collections.copy(circles, p.circles);
    }

    ArrayList<Circle2D> pack() throws Exception {
        for (ShapePair shapePair : shapePairs)
            try {
                double newCircleRadius = remainingRadii.remove(0);
                packNewCircle(newCircleRadius, shapePair);
                circles = new Packing(this).pack();
                return circles;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "I couldn't pack!");
            }
        return null;
    }

    void packNewCircle(double radius, ShapePair shapePair) {
        // Packing newPacking = new Packing(this);
    }
}
