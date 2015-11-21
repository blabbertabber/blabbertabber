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

    View speakerBar0;

    public RectangleView(Context context, AttributeSet attrs) {
        super(context);
        Log.i(TAG, "started constructor -------------------------------======");
    }

    public RectangleView(Context context) {
        super(context);
        Log.i(TAG, "started constructor -------------------------------");
        speakerBar0 = findViewById(R.id.bar_speaker_0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "Just started onDraw() -------------------------------");

        RectF rect = new RectF();
        rect.left = getLeft();
        rect.top = getTop();
        rect.right = getRight();
        rect.bottom = getBottom();

        Paint myPaint = new Paint();
        myPaint.setColor(Color.RED);
        myPaint.setStrokeWidth(3);
        canvas.drawRect(rect, myPaint);
        Log.i(TAG, "Just finishing onDraw() -------------------------------" + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
    }
}
