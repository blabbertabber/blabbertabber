package com.blabbertabber.blabbertabber;

import math.geom2d.conic.Circle2D;

/**
 * Packs circles in a rectangle as tightly as possible
 */
public class CirclePacker {
    private double x;
    private double y;

    public CirclePacker(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Circle2D[] addCircle(double[] radii) {
        if (radii.length == 1) {
            double radius = (x > y ? x : y) / 2;
            return new Circle2D[]{new Circle2D(radius, radius, radius)};
        }
        return null;
    }
}
