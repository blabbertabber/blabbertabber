package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by brendancunnie on 11/14/15.
 */
public class PieChart extends View {

    private static final String TAG = "PieChart";
    RectF rectf = new RectF(100, 50, 610, 700);
    double total = 0;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double[] value_degree;
    private int[] COLORS = TheSpeakers.speakerColors;

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public PieChart(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        double[] values = TheSpeakers.getSpeakersTimes();

        value_degree = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            value_degree[i] = values[i];
            total += values[i];
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        double temp = 0;

        int width = getWidth();
        int height = getHeight();
        int squareLength = (height > width) ? width : height;
        int topPad = (height - squareLength) / 2;
        int leftPad = (width - squareLength) / 2;
        RectF rectf = new RectF(leftPad, topPad, squareLength + leftPad, squareLength + topPad);

        for (int i = 0; i < value_degree.length; i++) {//values2.length; i++) {
            temp += i == 0 ? 0 : value_degree[i - 1];
            paint.setColor(COLORS[i]);
            float startAngle = (float) (temp * 360.0 / total);
            float stopAngle = (float) (value_degree[i] * 360.0 / total);
            canvas.drawArc(rectf, startAngle, stopAngle, true, paint);
        }
    }
}
