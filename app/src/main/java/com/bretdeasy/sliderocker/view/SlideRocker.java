package com.bretdeasy.sliderocker.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

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

    //Drawing
    private RectF mIndicatorRect;
    private RectF mIndicatorOriginRect;
    private RectF mIndicatorBottomRect;
    private RectF mIndicatorTopRect;

    //Dimen fields
    private int mWidth;
    private int mHeight;
    private int mIndicatorRadius;

    //Gesture
    private GestureDetector mDetector;
    private boolean isScrolling;

    //Listener
    private OnSlideUpdateListener onSlideUpdateListener;
    private IntervalCounterAsyncTask mIntervalCounterAsyncTask;

    //Intervals
    private int mIntervalCount = 1;
    private int mIntervalRate = 1000;
    private int mCurrentInterval = 0;
    private boolean positive;

    //Animation
    private ValueAnimator mValueAnimator;

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
            mIntervalCount = a.getInteger(R.styleable.SlideRocker_intervalCount, 1);
            mIntervalRate = a.getInteger(R.styleable.SlideRocker_intervalRate, 1000);

        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(mIndicatorColor);
        mLinePaint.setStyle(Paint.Style.FILL);

        mDetector = new GestureDetector(SlideRocker.this.getContext(), new GestureListener());

        setAnimator();
    }

    private void setAnimator() {
        float interpolatorTension = 3.0f;

        mValueAnimator = new ValueAnimator();
        mValueAnimator.setInterpolator(new OvershootInterpolator(interpolatorTension));
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newCenterY = (float)animation.getAnimatedValue();
                mIndicatorRect.set(0, newCenterY - mIndicatorRadius, mWidth, newCenterY + mIndicatorRadius);

                invalidate();
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIndicatorRect.set(mIndicatorOriginRect);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        mIndicatorRadius = mWidth / 2;
        int lineWidth = mWidth / 4;
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

        //Set the rect for the top-most point and bottom-most point for the indicator
        mIndicatorTopRect = new RectF(
                0,
                0,
                2 * mIndicatorRadius,
                2 * mIndicatorRadius);

        mIndicatorBottomRect = new RectF(
                0,
                mHeight - (2 * mIndicatorRadius),
                2 * mIndicatorRadius,
                mHeight);

        mLinePaint.setStrokeWidth(lineWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(mWidth / 2, mWidth / 4, mWidth / 2, mHeight - (mWidth / 4), mLinePaint);
        canvas.drawOval(mIndicatorRect, mIndicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);

        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                resetView();
            }
        }

        return result;
    }

    private void resetView() {
        isScrolling = false;

        mValueAnimator.setFloatValues(mIndicatorRect.centerY(), mHeight / 2);
        mValueAnimator.start();
    }

    public void setOnSlideUpdateListener(OnSlideUpdateListener onSlideUpdateListener) {
        this.onSlideUpdateListener = onSlideUpdateListener;
    }

    /**
     *
     * @return true if the interval is in the positive range
     * (Indicator is pulled up or to the right), otherwise false
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     * Control the number of variable speed settings to move from. This number will be the number
     * of intervals for each the positive and negative sides.
     * @param numberOfIntervals number of intervals
     */
    public void setIntervalCount(int numberOfIntervals) {
        this.mIntervalCount = numberOfIntervals;
    }

    /**
     * the base rate (ms) at which updates are sent to the listener. This rate is divided by the current
     * interval to determine the actual rate of updating the listener.
     *
     * E.g. If the intervalRate is set to 1000 and the current interval is 2, the listener is updated
     * every 1000 / 2 = 500ms.
     *
     * @param intervalRate base rate at which updates will repeat
     */
    public void setIntervalRate(int intervalRate) {
        this.mIntervalRate = intervalRate;
    }

    /**
     * Listener to receive updates triggered the indicator is slid in either direction
     */
    public interface OnSlideUpdateListener {
        void onSlideUpdate(SlideRocker slideRocker);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Calculate distance as a ratio of indicator position and height from midpoint
            //if the indicator is in the bottom section, reduce the distance as it approaches the bottom
            //if the indicator is in the top section, reduce the distance as it approaches the top
            float midPoint = mHeight / 2;
            float indicatorCenterY = mIndicatorRect.centerY();
            float indicatorBottomY = mIndicatorRect.bottom;
            float indicatorTopY = mIndicatorRect.top;

            if ((indicatorCenterY > midPoint && e2.getY() > indicatorCenterY)
                    || indicatorCenterY < midPoint && e2.getY() < indicatorCenterY) {
                if (indicatorCenterY > midPoint) {
                    distanceY = distanceY - (distanceY * (indicatorBottomY / mHeight));
                    positive = false;
                } else {
                    distanceY = distanceY - (distanceY * ((midPoint - indicatorTopY) / midPoint));
                    positive = true;
                }
            }


            if (mIndicatorRect.bottom < mHeight && mIndicatorRect.top > 0) {
                mIndicatorRect.set(
                        mIndicatorRect.left,
                        mIndicatorRect.top - distanceY,
                        mIndicatorRect.right,
                        mIndicatorRect.bottom - distanceY);
            } else {
                float pointerY = e2.getY();
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
            updateCurrentInterval(mIndicatorRect.centerY());
            if (!isScrolling) {
                isScrolling = true;
                startIntervalUpdate();
            }
            return true;
        }
    }

    private void startIntervalUpdate() {
        if (mIntervalCounterAsyncTask == null || !mIntervalCounterAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mIntervalCounterAsyncTask = new IntervalCounterAsyncTask();
            mIntervalCounterAsyncTask.execute();
        }
    }

    private void updateCurrentInterval(float value) {
        int midpoint = mHeight / 2;

        if (value == midpoint) {
            mCurrentInterval = 0;
            return;
        }

        int intervalRange = (mHeight - midpoint) / mIntervalCount;
        if (value > midpoint) {
            for (int i = 1; i <= mIntervalCount; i++) {
                if (value < midpoint + intervalRange * i) {
                    mCurrentInterval = i;
                    return;
                }
            }
        } else {
            for (int i = 1; i <= mIntervalCount; i++) {
                if (value > midpoint - intervalRange * i) {
                    mCurrentInterval = i;
                    return;
                }
            }
        }
    }

    private class IntervalCounterAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... values) {
            long current = System.currentTimeMillis();
            publishProgress();

            while (isScrolling && mCurrentInterval != 0) {
                if (current + (mIntervalRate / mCurrentInterval) < System.currentTimeMillis()) {
                    current = System.currentTimeMillis();
                    publishProgress();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (onSlideUpdateListener != null) {
                onSlideUpdateListener.onSlideUpdate(SlideRocker.this);
            }
        }
    }
}
