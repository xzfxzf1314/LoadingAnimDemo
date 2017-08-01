package com.touch.xu.loadinganimdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by zfxu on 17/7/31.
 */

public class ExpandedView extends TextView {

    private static final String TAG = "ExpandedView";

    private final static float DEFAULT_RADIUS = 5f;
    private final static int DEFAULT_BORDER_COLOR = Color.RED;
    private static final int DEFAULT_BACK_COLOR = Color.BLUE;
    private static final float DEFAULT_BORDER_WIDTH = 5f;

    private static final float DEFAULT_DURATION = 500;
    //展开的状态
    private final static int STATUS_EXPAND = 1;
    //折叠的状态
    private final static int STATUS_COLLAPSE = 2;
    //展开->折叠
    private final static int STATUS_EXPAND_TO_COLLAPSE = 3;
    //折叠->展开
    private final static int STATUS_COLLAPSE_TO_EXPAND = 4;
    //展开后，显示下一个文字
    private static final int STATUS_EXPAND_NEXT_TEXT = 5;

    private int mCurrentStatus;

    private int mWidth;
    private int mHeight;

    //圆角矩形的半径
    private float mLeftTopRectRadius;
    private float mRightTopRectRadius;
    private float mLeftBottomRectRadius;
    private float mRightBottomRectRadius;

    private float[] mRadiusRectF = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    private int mBorderColor;
    private float mBorderWidth;
    private int mBackColor;

    private Paint mBorderPaint;
    private Paint mBackPaint;

    private Path mDrawPath;

    //折叠或者展开的时间(ms)
    private float mDrawerAnimDuration;
    //翻滚的时间
    private float mRollAnimDuration;

    private long mStartTime;

    private int mExpandOrCollapseDistance;

    private RectF mExpandViewRect;

    private Paint mTextPaint;

    private String mCurrentText;
    private String mNextText;
    private long mNextTextStartTime;

    public ExpandedView(Context context) {
        super(context);

        initPaint(context, null);
    }

    public ExpandedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initPaint(context, attrs);
    }

    public ExpandedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPaint(context, attrs);
    }

    private void initPaint(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandedView);

        mLeftTopRectRadius = ta.getDimension(R.styleable.ExpandedView_back_left_top_radius, DEFAULT_RADIUS);
        mRightBottomRectRadius = ta.getDimension(R.styleable.ExpandedView_back_right_top_radius, DEFAULT_RADIUS);
        mLeftBottomRectRadius = ta.getDimension(R.styleable.ExpandedView_back_left_bottom_radius, DEFAULT_RADIUS);
        mRightTopRectRadius = ta.getDimension(R.styleable.ExpandedView_back_right_bottom_radius, DEFAULT_RADIUS);
        mBorderColor = ta.getColor(R.styleable.ExpandedView_back_border_color, DEFAULT_BORDER_COLOR);
        mBackColor = ta.getColor(R.styleable.ExpandedView_background_color, DEFAULT_BACK_COLOR);
        mBorderWidth = ta.getFloat(R.styleable.ExpandedView_back_border_width, DEFAULT_BORDER_WIDTH);
        mDrawerAnimDuration = ta.getFloat(R.styleable.ExpandedView_drawer_duration, DEFAULT_DURATION);
        mRollAnimDuration = ta.getFloat(R.styleable.ExpandedView_roll_duration, DEFAULT_DURATION);

        mCurrentText = ta.getString(R.styleable.ExpandedView_fore_text);
        ta.recycle();

        mExpandViewRect = new RectF();

        mCurrentStatus = STATUS_EXPAND;

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAntiAlias(true);

        mBackPaint = new Paint();
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setColor(mBackColor);
        mBackPaint.setAntiAlias(true);

        setRadius(mLeftTopRectRadius, mRightTopRectRadius, mRightBottomRectRadius, mLeftBottomRectRadius);

        mDrawPath = new Path();

        mTextPaint = new Paint();
        mTextPaint.setColor(getCurrentTextColor());
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setFakeBoldText(true);
    }

    public void setStatusExpand() {
        if (isExpand() || isExpanding()) {
            return;
        }
        mCurrentStatus = STATUS_COLLAPSE_TO_EXPAND;
        mStartTime = System.currentTimeMillis();
        postInvalidate();
    }

    public void setStatusCollapse() {
        if (isCollapse() || isCollapsing()) {
            return;
        }
        mCurrentStatus = STATUS_EXPAND_TO_COLLAPSE;
        mStartTime = System.currentTimeMillis();
        postInvalidate();
    }

    public boolean isCollapse() {
        return mCurrentStatus == STATUS_COLLAPSE;
    }

    public boolean isExpand() {
        return mCurrentStatus == STATUS_EXPAND;
    }

    //正在折叠
    public boolean isCollapsing() {
        return mCurrentStatus == STATUS_EXPAND_TO_COLLAPSE;
    }

    //正在展开
    public boolean isExpanding() {
        return mCurrentStatus == STATUS_COLLAPSE_TO_EXPAND;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentStatus) {
            case STATUS_EXPAND:
                drawExpandView(canvas);
                break;
            case STATUS_EXPAND_TO_COLLAPSE:
                drawExpand2CollapseView(canvas);
                break;
            case STATUS_COLLAPSE:
                drawCollapseView(canvas);
                break;
            case STATUS_COLLAPSE_TO_EXPAND:
                drawCollapse2ExpandView(canvas);
                break;
            case STATUS_EXPAND_NEXT_TEXT:
                drawNextTextView(canvas);
                break;
        }
    }

    private void drawNextTextView(Canvas canvas) {
        drawExpandBack(canvas);

        float ratio = getNextTextRatio();
        String text;
        Rect bounds;
        Paint.FontMetricsInt fontMetrics = null;
        int baseline;
        float start;
        if (ratio != 1) {
            bounds = new Rect();
            mTextPaint.getTextBounds(mCurrentText, 0, mCurrentText.length(), bounds);
            start = mExpandViewRect.left + mLeftTopRectRadius * 0.8f + (mExpandViewRect.width() - mLeftTopRectRadius - bounds.width()) / 2;
            fontMetrics = mTextPaint.getFontMetricsInt();
            baseline = (int) ((mHeight * (1 - ratio) - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top);

            mTextPaint.setAlpha((int) (255 *  (1 - ratio)));
            canvas.drawText(mCurrentText, start, baseline, mTextPaint);

            bounds = new Rect();
            mTextPaint.getTextBounds(mNextText, 0, mNextText.length(), bounds);
            start = mExpandViewRect.left + mLeftTopRectRadius * 0.8f + (mExpandViewRect.width() - mLeftTopRectRadius - bounds.width()) / 2;
            baseline = (int) (((mHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top) + mHeight / 2 * (1 - ratio));

            mTextPaint.setAlpha((int) (255 * ratio));
            canvas.drawText(mNextText, start, baseline, mTextPaint);

            postInvalidateDelayed(50);
        } else {
            mTextPaint.setAlpha(255);
            bounds = new Rect();
            mTextPaint.getTextBounds(mNextText, 0, mNextText.length(), bounds);
            fontMetrics = mTextPaint.getFontMetricsInt();
            start = mExpandViewRect.left + mLeftTopRectRadius * 0.8f + (mExpandViewRect.width() - mLeftTopRectRadius - bounds.width()) / 2;
            baseline = ((mHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top);

            canvas.drawText(mNextText, start, baseline, mTextPaint);

            String text1 = mNextText;
            mNextText = mCurrentText;
            mCurrentText = text1;
        }
    }

    private float getNextTextRatio() {
        long now = System.currentTimeMillis();
        float ratio = (now - mNextTextStartTime) / mRollAnimDuration;
        return ratio >= 1 ? 1 : ratio;
    }

    private void drawText(Canvas canvas, String text) {
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;

        float padding = mLeftTopRectRadius;
        if (mLeftTopRectRadius + bounds.width() + mExpandViewRect.left > mExpandViewRect.width()) {
            Log.e(TAG, " 大于 。。。");
            padding *= 0.8f;
        }
        float start = mExpandViewRect.left + padding * 0.8f + (mExpandViewRect.width() - padding - bounds.width()) / 2;

        Log.i(TAG, " start: " + start);
        canvas.drawText(text, start, baseline, mTextPaint);
    }

    private void drawCollapse2ExpandView(Canvas canvas) {
        float ratio = getAnimRatio();
        int left = (int) (mWidth - mBorderWidth - 2 * mLeftBottomRectRadius - mExpandOrCollapseDistance * ratio - 60);
        left = left < mBorderWidth ? (int) mBorderWidth : left;
        mExpandViewRect.set(left, mExpandViewRect.top, mExpandViewRect.right, mExpandViewRect.bottom);
        drawView(canvas);
        drawExpandText(canvas);
        if (ratio == 1) {
            mCurrentStatus = STATUS_EXPAND;
        } else {
            invalidate();
        }
    }

    private void drawCollapseView(Canvas canvas) {
        int left = (int) (mBorderWidth + mExpandOrCollapseDistance);
        mExpandViewRect.set(left, mExpandViewRect.top, mExpandViewRect.right, mExpandViewRect.bottom);
        drawView(canvas);
        String text = mCurrentText.substring(0, 2) + "..";
        drawText(canvas, text);
    }

    private void drawExpand2CollapseView(Canvas canvas) {
        float ratio = getAnimRatio();
        float distance = mExpandOrCollapseDistance * ratio;
        int left = (int) (mBorderWidth + distance);

        mExpandViewRect.set(left, mExpandViewRect.top, mExpandViewRect.right, mExpandViewRect.bottom);
        drawView(canvas);
        drawCollapseText(canvas);
        if (ratio == 1) {
            mCurrentStatus = STATUS_COLLAPSE;
        } else {
            invalidate();
        }
    }

    private void drawCollapseText(Canvas canvas) {
        String text = mCurrentText;
        text = text.substring(0, 2) + "..";
        drawText(canvas, text);
    }

    private void drawExpandText(Canvas canvas) {
        String text = mCurrentText;
        drawText(canvas, text);
    }

    private float getAnimRatio() {
        long now = System.currentTimeMillis();
        float ratio = (now - mStartTime) / mDrawerAnimDuration;
        return ratio >= 1 ? 1 : ratio;
    }

    private void drawView(Canvas canvas) {
        mDrawPath.reset();
        mDrawPath.addRoundRect(mExpandViewRect, mRadiusRectF, Path.Direction.CW);
        canvas.drawPath(mDrawPath, mBorderPaint);
        //绘制背景
        mDrawPath.reset();
        mDrawPath.addRoundRect(mExpandViewRect, mRadiusRectF, Path.Direction.CW);
        canvas.drawPath(mDrawPath, mBackPaint);
    }

    //绘制展开好的视图
    private void drawExpandView(Canvas canvas) {
        drawExpandBack(canvas);
        String text = mCurrentText.toString();
        drawText(canvas, text);
    }

    private void drawExpandBack(Canvas canvas) {
        mExpandViewRect.set(mBorderWidth, mBorderWidth, mWidth - mBorderWidth, mHeight - mBorderWidth);
        drawView(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG, " onLayout " + " " + changed);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mExpandOrCollapseDistance = (int) (mWidth -  2 * mLeftTopRectRadius - 60);
            mExpandViewRect.set(mBorderWidth, mBorderWidth, mWidth - mBorderWidth, mHeight - mBorderWidth);
        }
    }

    public void setRadius(float leftTop, float rightTop, float rightBottom, float leftBottom) {
        mRadiusRectF[0] = leftTop;
        mRadiusRectF[1] = leftTop;
        mRadiusRectF[2] = rightTop;
        mRadiusRectF[3] = rightTop;
        mRadiusRectF[4] = rightBottom;
        mRadiusRectF[5] = rightBottom;
        mRadiusRectF[6] = leftBottom;
        mRadiusRectF[7] = leftBottom;
    }

    public void setBorderColor(int borderColor) {
        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
    }

    public void setBorderWidth(float borderWidth) {
        mBorderWidth = borderWidth;
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    public void setBackColor(int backColor) {
        mBackColor = backColor;
        mBackPaint.setColor(mBackColor);
    }

    public void setDrawerAnimDuration(float animDuration) {
        mDrawerAnimDuration = animDuration;
    }

    public void setNextText(String nextText) {
        if (isCollapse() || isCollapsing()) {
            return;
        }
        mNextTextStartTime = System.currentTimeMillis();
        mNextText = nextText;
        mCurrentStatus = STATUS_EXPAND_NEXT_TEXT;
        invalidate();
    }
}
