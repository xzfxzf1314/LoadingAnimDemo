package com.touch.xu.loadinganimdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zfxu on 17/7/25.
 */

public class DottedLineView extends View {

    public interface OnDashFinishListener {
        void onDashFinish(View view);
    }

    private static final String TAG = DottedLineView.class.getSimpleName();
    //持续200ms
    private final static int MOVE_DURATION = 500;

    private int mWidth;
    private int mHeight;
    private Rect mRect;

    //绘制虚线的paint
    private Paint mDashPaint;

    private Paint mSolidPaint;
    private long mStartMoveTime;
    private boolean mStartMove;

    private OnDashFinishListener mOnDashFinishListener;

    public DottedLineView(Context context) {
        super(context);
        initPaint();
    }

    public DottedLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public DottedLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public void setOnDashFinishListener(OnDashFinishListener listener) {
        mOnDashFinishListener = listener;
    }

    private void initPaint() {

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        mRect = new Rect();

        mDashPaint = new Paint();
        mDashPaint.setColor(Color.RED);
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(4f);
        mDashPaint.setAntiAlias(true);
        mDashPaint.setPathEffect(new DashPathEffect(new float[] { 3f, 3f }, 0));

        mSolidPaint = new Paint();
        mSolidPaint.setColor(Color.RED);
        mSolidPaint.setStyle(Paint.Style.STROKE);
        mSolidPaint.setStrokeWidth(4f);
        mSolidPaint.setAntiAlias(true);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mStartMove) {
            drawDashLine(canvas);
        } else {
            float ratio = getMoveRatio();
            if (ratio < 1) {
                drawDashLine(canvas);
                drawSolidLine(canvas, ratio);
                invalidate();
            } else {
                drawSolidLine(canvas, 1f);
                mStartMove = false;
                if (mOnDashFinishListener != null) {
                    mOnDashFinishListener.onDashFinish(this);
                }
            }
        }
    }

    private void drawSolidLine(Canvas canvas, float ratio) {
        float x0 = (mRect.right - mRect.left) / 2f;
        float y0 = 0;
        float x1 = x0;
        float y1 = y0 + mRect.bottom - mRect.top;
        canvas.drawLine(x0, y0, x1, y1 * ratio, mSolidPaint);
    }

    private void drawDashLine(Canvas canvas) {
        float x0 = (mRect.right - mRect.left) / 2f;
        float y0 = 0;
        float x1 = x0;
        float y1 = y0 + mRect.bottom - mRect.top;
        canvas.drawLine(x0, y0, x1, y1, mDashPaint);
    }

    public void startMove() {
        mStartMove = true;
        mStartMoveTime = System.currentTimeMillis();
        invalidate();
    }

    private float getMoveRatio() {
        long now = System.currentTimeMillis();
        float ratio = (now - mStartMoveTime) / (float) MOVE_DURATION;
        return ratio >= 1 ? 1 : ratio;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mRect.left = left;
            mRect.top = top;
            mRect.right = right;
            mRect.bottom = bottom;
        }
    }
}
