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
 * Used in the SummaryActivity to draw the bars.
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
        Log.i(TAG, "setColor() color: " + color);
        mColor = color;
    }

    public void setVisible(boolean visible) {
        Log.i(TAG, "setVisible() visible: " + visible);
        mVisible = visible;
    }

    public void setBarRatio(float barRatio) {
        // mBarRatio can range from 0 to 1.0
        // 1.0 means it's the longest speaker
        // 0.5 means that this speaker has spoken half as much as the longest speaker
        // etc...
        Log.i(TAG, "setBarRatio() barRatio: " + barRatio);
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
            rect.right = mBarRatio * (float) (getWidth() - getLeft());
            rect.bottom = getHeight();
            Log.i(TAG, "getWidth() " + getWidth() + " getLeft() " + getLeft() + " rect.right " + rect.right + " bottom " + rect.bottom);

            Paint myPaint = new Paint();
            myPaint.setColor(mColor);
            myPaint.setStrokeWidth(1);
            canvas.drawRect(rect, myPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged( " + w + ", " + h + ", " + oldw + ", " + oldh + " )");
    }
}