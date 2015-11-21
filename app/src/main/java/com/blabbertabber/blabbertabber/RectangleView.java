package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by brendancunnie on 11/21/15.
 */
public class RectangleView extends View {
    private static final String TAG = "RectangleView";
    private boolean mVisible = false;
    private int mColor = Color.GREEN;
    private float mBarRatio = (float) 0.50;

    public RectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "RectangleView(Context, AttributeSet)");
    }

    public RectangleView(Context context) {
        super(context);
        Log.i(TAG, "RectangleView(Context)");
    }

    public void setColor(int color) {
        mColor = color;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public void setBarRatio(float barRatio) {
        // mBarRatio can range from 0 to 1.0
        // 1.0 means it's the longest speaker
        // 0.5 means that this speaker has spoken half as much as the longest speaker
        // etc...
        mBarRatio = barRatio;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw()");

        if (mVisible) {
            RectF rect = new RectF();
            rect.left = 0;
            rect.top = 0;
            rect.right = mBarRatio * (float) getWidth();
            Log.i(TAG, "getWidth() " + getWidth() + " rect.right " + rect.right);
            rect.bottom = getHeight();

            Paint myPaint = new Paint();
            myPaint.setColor(mColor);
            myPaint.setStrokeWidth(1);
            canvas.drawRect(rect, myPaint);
        }
    }
}