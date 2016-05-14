//package com.blabbertabber.blabbertabber;
//
//import java.security.InvalidParameterException;
//import java.util.HashSet;
//
//import math.geom2d.Point2D;
//import math.geom2d.conic.Circle2D;
//
///**
// * Packs circles in a rectangle as tightly as possible
// */
//public class CirclePacker {
//    private double x;
//    private double y;
//
//    public CirclePacker(double x, double y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    /**
//     * Place the circles within the rectangle.
//     *
//     * @param radii
//     * @return The array of circles packed into the rectangle.
//     * @assumption The radii will be in descending order.
//     */
//    public Circle2D[] addCircles(double[] radii) {
//        if (radii.length == 1) {
//            double radius = (x > y ? x : y) / 2;
//            return new Circle2D[]{new Circle2D(radius, radius, radius)};
//        } else if (radii.length == 2) {
//            Circle2D circle1 = new Circle2D(radii[0], radii[0], radii[0]);
//            Circle2D circle2 = new Circle2D(x - radii[1], radii[1], radii[1]);
//            Point2D center1 = circle1.center();
//            Point2D center2 = circle2.center();
//            double distance = center1.distance(center2.x(), center2.y());
//            double minDistance = radii[0] + radii[1];
//            if (distance < minDistance) {
//                String msg = "The radii must be at least " + minDistance + " apart.  " +
//                        "But they are only " + distance + " apart.";
//                throw new InvalidParameterException(msg);
//            }
//            return new Circle2D[]{circle1, circle2};
//        } else if (radii.length == 3) {
//            // At first 4 corners
//            Circle2D circle1 = new Circle2D(radii[0], radii[0], radii[0]);
//            // with each circle, 4 new corners
//            // circle1 has 2 new corners along the bottom, and 2 new corners along the left edge.
//            // Is it easier to track corners?  Or contact points, each of which is 2 corners?
//
//            HashSet<Double> remainingRadii = new HashSet<>(radii.length);
//            for (int i = 1; i < radii.length; i++) {
//                remainingRadii.add(radii[i]);
//            }
//            for (double radius : remainingRadii) {
//                Circle2D circle2 = new Circle2D(x - radius, radius, radius);
//                /// pack the third circle
//
//            }
//
//
//        }
//        return null;
//    }
//}
