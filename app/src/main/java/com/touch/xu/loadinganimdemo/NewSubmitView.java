package com.touch.xu.loadinganimdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zfxu on 17/7/25.
 */

public class NewSubmitView extends View {


    public interface OnSubmitFinishListener {

        void onSubmitFinish(View view);
    }
    //动画开始，静态的圆圈
    public final static int STEP_0 = 0;

    //动画第一步，正在转圈
    public final static int STEP_1 = 1;

    //动画第二步，转圈结束，圆圈又白色变成蓝色
    public final static int STEP_2 = 2;

    //动画第三部，圆圈的对勾慢慢滑出来
    public final static int STEP_3 = 3;

    //动画结束
    public final static int STEP_4 = 4;

    //圆圈转动开始的角度
    private static final int START_DEGREE = 270;

    //圆圈最终的进度
    private static final float TARGET_PROGRESS = 1f;

    //进度的Index值
    private static final float INDEX_PROGRESS = 0.01f;

    //最后一步画对勾持续多久
    private static final float CORRECT_DURATION = 400;

    private int mWidth;

    private int mHeight;
    private int mRadius;
    private long mStep3StartTime;

    //当前动画的步骤
    private int mStep = STEP_0;

    //圆圈线的画笔
    private Paint mBorderPaint;
    private int mCircleColor;
    private float mCircleWidth;

    //圆圈背景的画笔
    private Paint mBackPaint;
    private int mBackDefaultColor;
    private int mBackEndColor;


    //对勾的颜色和宽度
    private int mCorrectColor;
    private float mCorrectWidth;
    private Paint mCorrectPaint;

    private float mProgress = 0f;

    private OnSubmitFinishListener mOnFinishListener;

    public NewSubmitView(Context context) {
        super(context);
        initPaint(context, null);
    }

    public NewSubmitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context, attrs);
    }

    public NewSubmitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context, attrs);
    }

    private void initPaint(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleSubmitView);

        mBackDefaultColor = ta.getColor(R.styleable.CircleSubmitView_back_default_color, Color.WHITE);
        mBackEndColor = ta.getColor(R.styleable.CircleSubmitView_back_end_color, Color.RED);
        mCircleColor = ta.getColor(R.styleable.CircleSubmitView_circle_color, Color.RED);
        mCircleWidth = ta.getFloat(R.styleable.CircleSubmitView_circle_width, 4f);
        mCorrectColor = ta.getColor(R.styleable.CircleSubmitView_right_color, Color.WHITE);
        mCorrectWidth = ta.getFloat(R.styleable.CircleSubmitView_right_width, 4f);

        ta.recycle();


        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mCircleWidth);
        mBorderPaint.setColor(mCircleColor);
        mBorderPaint.setAntiAlias(true);

        mBackPaint = new Paint();
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setColor(mBackDefaultColor);
        mBackPaint.setAntiAlias(true);

        mCorrectPaint = new Paint();
        mCorrectPaint.setAntiAlias(true);
        mCorrectPaint.setStyle(Paint.Style.STROKE);
        mCorrectPaint.setStrokeWidth(mCorrectWidth);
        mCorrectPaint.setColor(mCorrectColor);
        mCorrectPaint.setDither(true);
        mCorrectPaint.setStrokeJoin(Paint.Join.ROUND);
        mCorrectPaint.setStrokeCap(Paint.Cap.ROUND);

        mProgress = 0;
    }

    public void setOnFinishListener(OnSubmitFinishListener listener) {
        mOnFinishListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        switch (mStep) {
            case STEP_0:
                drawStep0Anim(canvas);
                break;
            case STEP_1:
                drawStep1Anim(canvas);
                break;
            case STEP_2:
                drawStep2Anim(canvas);
                break;
            case STEP_3:
                drawStep3Anim(canvas);
                break;
        }

        if (!isFinish()) {
            invalidate();
        } else {
            drawStep4(canvas);
        }
    }

    private void drawStep0Anim(Canvas canvas) {
        //先画一个白色的圆
        mBackPaint.setColor(mBackDefaultColor);
        canvas.drawCircle(mWidth / 2,  mHeight / 2, mRadius, mBackPaint);

        //边圈
        mBorderPaint.setColor(mCircleColor);
        canvas.drawCircle(mWidth / 2,  mHeight / 2, mRadius, mBorderPaint);
    }

    private void drawStep1Anim(Canvas canvas) {
        if (mProgress == TARGET_PROGRESS) {
            mProgress = 0;
        }
        //先画一个白色的圆
        mBackPaint.setColor(mBackDefaultColor);
        canvas.drawCircle(mWidth / 2,  mHeight / 2, mRadius, mBackPaint);

        //在画一个圆圈周围正在转动的线
        canvas.drawArc(new RectF(mWidth / 2 - mRadius, mHeight / 2 - mRadius, mWidth / 2 + mRadius, mHeight / 2 + mRadius),
                START_DEGREE + mProgress * 360, 120, false, mBorderPaint);
        if (mProgress < TARGET_PROGRESS) {
            mProgress += INDEX_PROGRESS;
            mProgress = Math.min(mProgress, 1);
        }
    }

    private void drawStep2Anim(Canvas canvas) {
        mBackPaint.setColor(mBackEndColor);
        canvas.drawCircle(mWidth / 2,  mHeight / 2, mRadius, mBackPaint);

        mStep3StartTime = System.currentTimeMillis();
        mStep++;
    }

    private void drawStep3Anim(Canvas canvas) {

        mBackPaint.setColor(mBackEndColor);
        canvas.drawCircle(mWidth / 2,  mHeight / 2, mRadius, mBackPaint);

        float ratio = getCorrectRatio();

        drawCorrectSign(canvas, ratio);

        if (ratio == 1f) {
            mStep = STEP_4;

            if (mOnFinishListener != null) {
                mOnFinishListener.onSubmitFinish(this);
            }
        }
    }

    private void drawStep4(Canvas canvas) {
        mBackPaint.setColor(mBackEndColor);
        canvas.drawCircle(mWidth / 2,  mHeight / 2, mRadius, mBackPaint);
        drawCorrectSign(canvas, 1f);
    }

    private void drawCorrectSign(Canvas canvas, float ratio) {

        int centerX = mWidth / 2 - 4;
        int centerY = mHeight / 2 + mRadius / 4;

        Path path = new Path();
        path.moveTo(centerX - (mRadius * ratio / 4), centerY - (mRadius * ratio / 4));
        path.lineTo(centerX, centerY);
        path.lineTo(centerX + (mRadius * ratio / 2), centerY -(mRadius * ratio / 2));

        canvas.drawPath(path, mCorrectPaint);
    }

    private float getCorrectRatio() {
        long now = System.currentTimeMillis();
        float ratio = (now - mStep3StartTime) / CORRECT_DURATION;
        return ratio >= 1 ? 1 : ratio;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mRadius = mWidth / 2 - 4;
        }
    }

    private boolean isFinish() {
        return mStep == STEP_4;
    }

    public void startRotate() {
        mStep = STEP_1;
        invalidate();
    }

    public void stopRotate() {
        mStep = STEP_2;
        invalidate();
    }
}
