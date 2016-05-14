package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;

import math.geom2d.conic.Circle2D;

/**
 * Created by brendancunnie on 5/14/16.
 */
public class CircleView extends View {
    private static final String TAG = "CircleView";
    Collection<Circle2D> circles;
    ArrayList<Double> radii = new ArrayList<>();
    private Paint paint = new Paint();
    private Packing p;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "CircleView(Context, AttributeSet)");
        init();
    }

    public CircleView(Context context) {
        super(context);
        Log.i(TAG, "CircleView(Context)");
        init();
    }

    private void init() {
        radii.add((double) getWidth() / 2);
        radii.add(100.0);
        radii.add(200.0);
        p = new Packing(getWidth(), getHeight(), radii);
        circles = p.packNonRecursive();
        Log.i(TAG, "circles.size(): " + circles.size());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw()");
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(1);
        canvas.drawCircle(50, 50, 45, paint);
        for (Circle2D c : circles) {
            Log.i(TAG, "c.radius(): " + c.radius());
            canvas.drawCircle((float) c.center().x(), (float) c.center().y(), (float) c.radius(), paint);
        }
    }

}
