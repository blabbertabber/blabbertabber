package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Show a section of a pie slice around the speaker ball.
 * Used for animations.
 */
public class PieSlice extends View {
    private static final String TAG = "PieSlice";
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color = Color.YELLOW;
    private float startAngle = 0;
    private float sweepAngle = 45;

    /**
     * Constructor.  PieSlice is used in the layout.xml, so you need a constructor to which
     * the framework will pass Context and Attribute set.
     *
     * @param context required parameter by framework, but unnused here.
     * @param attrs   required parameter by framework, but unnused here.
     */
    public PieSlice(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /// TODO: needed?
    public PieSlice(Context context) {
        super(context);
    }

    /**
     * Initialize color and angle from attributes set in layout.xml
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        // http://stackoverflow.com/questions/8302229/accessing-attrs-in-attributeset-for-custom-components
        /// TODO: can we use the passed context instead of getContext()?
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PieSlice);
        color = a.getColor(R.styleable.PieSlice_pieColor, Color.BLACK);
        startAngle = a.getFloat(R.styleable.PieSlice_startAngle, 0);
        sweepAngle = a.getFloat(R.styleable.PieSlice_sweepAngle, 0);
        a.recycle();
        Log.i(TAG, "init() color: " + color + " startAngle " + startAngle + " sweepAngle " + sweepAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw() color: " + color + " startAngle " + startAngle + " sweepAngle " + sweepAngle);

        int width = getWidth();
        int height = getHeight();
        int squareLength = (height > width) ? width : height;
        int topPad = (height - squareLength) / 2;
        int leftPad = (width - squareLength) / 2;
        RectF rectf = new RectF(leftPad, topPad, squareLength + leftPad, squareLength + topPad);

        paint.setColor(color);
        canvas.drawArc(rectf, startAngle, sweepAngle, true, paint);
    }
}
