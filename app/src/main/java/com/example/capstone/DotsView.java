package com.example.capstone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DotsView extends View {

    private static final int DOT_SIZE = 24;
    private static final int DOT_SPACING = 48;

    private int mActiveDot = 0;

    public DotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int cx = width / 2 - (DOT_SIZE + DOT_SPACING);
        int cy = height / 2;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        for (int i = 0; i < 4; i++) {
            if (i == mActiveDot) {
                paint.setColor(Color.WHITE);
            } else {
                paint.setColor(Color.GRAY);
            }
            canvas.drawCircle(cx, cy, DOT_SIZE / 2, paint);
            cx += DOT_SIZE + DOT_SPACING;
        }
    }

    public void setActiveDot(int activeDot) {
        mActiveDot = activeDot;
        invalidate();
    }

}
