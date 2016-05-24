package com.bretdeasy.sliderocker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.bretdeasy.sliderocker.R;

import java.util.HashMap;
import java.util.Map;

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
    private RectF mIndicatorOriginRect;
    private RectF mIndicatorBottomRect;
    private RectF mIndicatorTopRect;
    private RectF mTopCapRect;
    private RectF mBottomCapRect;

    //Dimen fields
    private int mWidth;
    private int mHeight;
    private int mCapRadius;
    private int mIndicatorRadius;

    //Gesture
    private GestureDetector mDetector;
    private boolean isScrolling;
    private Map<String, String> mActionMap;

    //Listener
    private OnIntervalTriggeredListener onIntervalTriggeredListener;
    private IntervalCounterAsyncTask mIntervalCounterAsyncTask;

    //Intervals
    private int mIntervals = 1;
    private int mValue = 1;


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

        mDetector = new GestureDetector(SlideRocker.this.getContext(), new GestureListener());
        mIntervalCounterAsyncTask = new IntervalCounterAsyncTask();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        mIndicatorRadius = mWidth / 2;
        int lineWidth = mWidth / 4;
        mCapRadius = lineWidth / 2;
        int centerX = mWidth / 2;
        int centerY = mHeight / 2;


        mIndicatorRect = new RectF(
                centerX - mIndicatorRadius,
                centerY - mIndicatorRadius,
                centerX + mIndicatorRadius,
                centerY + mIndicatorRadius);

        mIndicatorOriginRect = new RectF(
                centerX - mIndicatorRadius,
                centerY - mIndicatorRadius,
                centerX + mIndicatorRadius,
                centerY + mIndicatorRadius);

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

        //Set the rect for the top-most point and bottom-most point for the indicator
        //noinspection SuspiciousNameCombination
        mIndicatorTopRect = new RectF(
                0,
                0,
                mWidth,
                mWidth);

        mIndicatorBottomRect = new RectF(
                0,
                mHeight - mWidth,
                mWidth,
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);

        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                resetView();
            }

            Log.d("onTouchEvent - False", "Action = " + getActionName(event.getAction()));
        } else {
            Log.d("onTouchEvent - True", "Action = " + getActionName(event.getAction()));
        }

        return result;
    }

    private String getActionName(int id) {
        if (mActionMap == null) {
            initializeActionMap();
        }

        String key = String.valueOf(id);

        return mActionMap.get(key);
    }

    private void initializeActionMap() {
        mActionMap = new HashMap<>();
        mActionMap.put("0", "ACTION_DOWN");
        mActionMap.put("1", "ACTION_UP");
        mActionMap.put("2", "ACTION_MOVE");
        mActionMap.put("3", "ACTION_CANCEL");
        mActionMap.put("4", "ACTION_OUTSIDE");
        mActionMap.put("5", "ACTION_POINTER_DOWN");
        mActionMap.put("6", "ACTION_POINTER_UP");
        mActionMap.put("7", "ACTION_HOVER_MOVE");
        mActionMap.put("8", "ACTION_SCROLL");
        mActionMap.put("9", "ACTION_HOVER_ENTER");
        mActionMap.put("10", "ACTION_HOVER_EXIT");
        mActionMap.put("11", "ACTION_BUTTON_PRESS");
        mActionMap.put("12", "ACTION_BUTTON_RELEASE");
    }

    private void resetView() {
        isScrolling = false;
        mIndicatorRect.set(mIndicatorOriginRect);

        invalidate();
    }

    public OnIntervalTriggeredListener getOnIntervalTriggeredListener() {
        return onIntervalTriggeredListener;
    }

    public void setOnIntervalTriggeredListener(OnIntervalTriggeredListener onIntervalTriggeredListener) {
        this.onIntervalTriggeredListener = onIntervalTriggeredListener;
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isScrolling = true;

            if (mIndicatorRect.bottom < mHeight && mIndicatorRect.top > 0) {
                mIndicatorRect.set(
                        mIndicatorRect.left,
                        mIndicatorRect.top - distanceY,
                        mIndicatorRect.right,
                        mIndicatorRect.bottom - distanceY);
            } else {
                float pointerY = e2.getY();
                Log.d("onScroll", "Pointer Y Position: " + pointerY + " \nHeight: " + mHeight + "\nRadius: " + mIndicatorRadius);
                if (pointerY < mHeight - mIndicatorRadius && pointerY > mIndicatorRadius) {
                    mIndicatorRect.set(
                            mIndicatorRect.left,
                            pointerY - mIndicatorRadius,
                            mIndicatorRect.right,
                            pointerY + mIndicatorRadius);
                } else if (mIndicatorRect.bottom >= mHeight) {
                    mIndicatorRect.set(mIndicatorBottomRect);
                } else {
                    mIndicatorRect.set(mIndicatorTopRect);
                }
            }



            invalidate();
            return true;
        }
    }

    public interface OnIntervalTriggeredListener {
        void onIntervalTriggered(int value);
    }

    private class IntervalCounterAsyncTask extends AsyncTask<Integer, Integer, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int value = params[0];
            int interval = params[1];

            long current = System.currentTimeMillis();

            while (isScrolling) {
                if (current + interval < System.currentTimeMillis()) {
                    current = System.currentTimeMillis();
                    publishProgress(value);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            onIntervalTriggeredListener.onIntervalTriggered(values[0]);
        }
    }
}
