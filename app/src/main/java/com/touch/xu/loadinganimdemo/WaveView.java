package com.touch.xu.loadinganimdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zfxu on 17/7/26.
 */

public class WaveView extends View {

    private static final int DEFAULT_INTERVAL_TIME = 50;

    private int[] mStrokeWidthArr;
    private int mMaxStrokeWidth;
    private int mRippleCount;

    private int mWidth;
    private int mHeight;

    private Paint mCirclePaint;

    private boolean mRunning;

    //波纹变化的时间(ms)
    private int mIntervalTime;

    //中间的图片
    private Drawable mCenterDrawable;
    private Rect mCenterDrawableLoc;
    private int mCircleColor;
    private float mCircleWidth;

    public WaveView(Context context) {
        super(context);
        initPaint(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context, attrs);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context, attrs);
    }

    private void initPaint(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WaveView);

        mIntervalTime = ta.getInteger(R.styleable.WaveView_interval_time, DEFAULT_INTERVAL_TIME);
        mCenterDrawable = ta.getDrawable(R.styleable.WaveView_center_drawable);
        mRippleCount = ta.getInteger(R.styleable.WaveView_wave_count, 5);
        mCircleColor = ta.getColor(R.styleable.WaveView_wave_circle_color, Color.RED);
        mCircleWidth = ta.getFloat(R.styleable.WaveView_wave_circle_width, 4f);

        ta.recycle();

        mCenterDrawableLoc = new Rect();

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(mCircleColor);

        mRunning = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //现在圆心位置draw一个图片
        drawCenterBitmap(canvas);

        if (mRunning) {
            drawRipple(canvas);
            postInvalidateDelayed(mIntervalTime);
        }
    }

    private void drawCenterBitmap(Canvas canvas) {
        if (mCenterDrawable == null) {
            return;
        }

        mCenterDrawable.setBounds(mCenterDrawableLoc);
        mCenterDrawable.draw(canvas);
    }

    private void initArray() {
        mStrokeWidthArr = new int[mRippleCount];
        for (int i = 0; i < mStrokeWidthArr.length; i++) {
            mStrokeWidthArr[i] = - mMaxStrokeWidth / mRippleCount * i;
        }
    }

    private void drawRipple(Canvas canvas) {
        for (int strokeWidth : mStrokeWidthArr) {
            if (strokeWidth < 0) {
                continue;
            }
            mCirclePaint.setStrokeWidth(mCircleWidth);
            mCirclePaint.setAlpha(255 - 255 * strokeWidth / mMaxStrokeWidth);
            canvas.drawCircle(mWidth / 2, mHeight / 2, (mCenterDrawable == null ? 0 : mCenterDrawable.getIntrinsicWidth() / 2) + strokeWidth/2,
                    mCirclePaint);
        }

        for (int i = 0; i < mStrokeWidthArr.length; i++) {
            if ((mStrokeWidthArr[i] += 4) > mMaxStrokeWidth) {
                mStrokeWidthArr[i] = 0;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = (mCenterDrawable == null ? 0 : mCenterDrawable.getIntrinsicWidth() / 2) * 2;
        mWidth = resolveSize(size, widthMeasureSpec);
        mHeight = resolveSize(size, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);

        mMaxStrokeWidth = (mWidth - (mCenterDrawable == null ? 0 : mCenterDrawable.getIntrinsicWidth() / 2)) / 2;
        initArray();

        if (mCenterDrawable != null) {
            int left = (mWidth - mCenterDrawable.getIntrinsicWidth()) / 2;
            int top = (mHeight - mCenterDrawable.getIntrinsicHeight()) / 2;
            mCenterDrawableLoc.set(left, top, left + mCenterDrawable.getIntrinsicWidth(), top + mCenterDrawable.getIntrinsicHeight());
        }
    }
}
