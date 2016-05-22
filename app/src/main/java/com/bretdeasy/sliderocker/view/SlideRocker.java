package com.bretdeasy.sliderocker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.bretdeasy.sliderocker.R;

/**
 * A SlideRocker is a graphical object that can increment or decrement at variable rates. The circular
 * indicator that tends toward the center of the view will send increments of increasingly large
 * positive or negative values as it is slid up or down from the center.
 */
public class SlideRocker extends View {
    //Paint fields
    private Paint mLinePaint;
    private Paint mIndicatorPaint;
    private int mLineColor;
    private int mIndicatorColor;
    private RectF mIndicatorRect;
    private RectF mTopCapRect;
    private RectF mBottomCapRect;

    //Dimen fields
    private int mWidth;
    private int mHeight;
    private int mCapRadius;

    public SlideRocker(Context context) {
        super(context);
    }

    public SlideRocker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SlideRocker,
                0,
                0);

        try {
            mIndicatorColor = a.getColor(R.styleable.SlideRocker_indicatorColor, Color.BLACK);
            mLineColor = a.getColor(R.styleable.SlideRocker_lineColor, Color.BLUE);

        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
//        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(mIndicatorColor);
        mLinePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        int lineWidth = mWidth / 4;
        mCapRadius = lineWidth / 2;
        int centerX = mWidth / 2;
        int centerY = mHeight / 2;
        int indicatorRadius = mWidth / 2;

        mIndicatorRect = new RectF(
                centerX - indicatorRadius,
                centerY - indicatorRadius,
                centerX + indicatorRadius,
                centerY + indicatorRadius);

        mTopCapRect = new RectF(
                centerX - mCapRadius,
                0,
                centerX + mCapRadius,
                2 * mCapRadius);

        mBottomCapRect = new RectF(
                centerX - mCapRadius,
                mHeight - 2 * mCapRadius,
                centerX + mCapRadius,
                mHeight);

        mLinePaint.setStrokeWidth(2 * mCapRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(mWidth / 2, mCapRadius, mWidth / 2, mHeight - mCapRadius, mLinePaint);
        canvas.drawOval(mTopCapRect, mLinePaint);
        canvas.drawOval(mBottomCapRect, mLinePaint);
        canvas.drawOval(mIndicatorRect, mIndicatorPaint);
    }
}