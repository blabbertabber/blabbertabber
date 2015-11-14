package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

/**
 * Created by brendancunnie on 11/14/15.
 */
public class PieChart extends View {

    private static final String TAG = "PieChart";
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double[] value_degree;
    private int[] COLORS = {Color.RED, Color.GREEN, Color.GRAY, Color.CYAN, Color.RED};
    RectF rectf = new RectF(100, 50, 610, 700);
    double total = 0;

    public PieChart(Context context, double[] values) {

        super(context);
        value_degree = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            value_degree[i] = values[i];
            total += values[i];
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double temp = 0;

        super.onDraw(canvas);

        for (int i = 0; i < value_degree.length; i++) {//values2.length; i++) {
            paint.setColor(Color.YELLOW);
            canvas.drawLine(100, 50, 610, 700, paint);
            canvas.drawLine(100, 700, 610, 50, paint);
            canvas.drawLine(100, 50, 610, 700, paint);
            if (i == 0) {
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, 0, (float) (value_degree[i] * 360.0 / total), true, paint);
                Log.wtf(TAG, "arc: " + (value_degree[i] * 360.0 / total));
            } else {
                temp += value_degree[i - 1];
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, (float)(temp * 360.0/total), (float)(value_degree[i] * 360.0/total), true, paint);
                Log.wtf(TAG, "arc: " + (float) (temp * 360.0 / total) + " sweep " + (float) (value_degree[i] * 360.0 / total));

            }

            paint.setColor(Color.MAGENTA);
        }
    }
}
