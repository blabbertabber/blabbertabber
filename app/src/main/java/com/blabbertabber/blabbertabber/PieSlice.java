package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by cunnie on 12/15/15.
 */
public class PieSlice extends View {
    private static final String TAG = "PieSlice";
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double[] value_degree;
    private int color = Color.YELLOW;

    public PieSlice(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieSlice(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int squareLength = (height > width) ? width : height;
        int topPad = (height - squareLength) / 2;
        int leftPad = (width - squareLength) / 2;
        RectF rectf = new RectF(leftPad, topPad, squareLength + leftPad, squareLength + topPad);

        paint.setColor(color);
        float startAngle = 0;
        float stopAngle = 45;
        canvas.drawArc(rectf, startAngle, stopAngle, true, paint);
    }
}
