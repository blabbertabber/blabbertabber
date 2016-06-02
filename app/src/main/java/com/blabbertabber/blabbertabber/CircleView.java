package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.blabbertabber.blabbertabber.shapes.ContainingRectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import math.geom2d.Box2D;
import math.geom2d.conic.Circle2D;

/**
 * Created by brendancunnie on 5/14/16.
 */
public class CircleView extends View {
    private static final String TAG = "CircleView";
    Collection<Circle2D> circles;
    ArrayList<Double> radii = new ArrayList<>();
    private Paint paint = new Paint();

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
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw()");
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(1);
        // FIXME: move this OUT of onDraw ASAP
        Box2D box = new Box2D(0, canvas.getHeight(), 0, canvas.getWidth());
        ContainingRectangle rect = new ContainingRectangle(box);
        List<Double> radiusList = Arrays.asList(new Double[]{200.0, 100.0, 30.0});
        ArrayList radii = new ArrayList(radiusList);
        Collection<Stack<Circle2D>> solutions = rect.placeFirstCircle(radii);
        Stack<Circle2D> circles = (Stack<Circle2D>) solutions.toArray()[0];
        for (Circle2D c : circles) {
            Log.i(TAG, "c.radius(): " + c.radius());
            canvas.drawCircle((float) c.center().x(), (float) c.center().y(), (float) c.radius(), paint);
        }
    }

}
