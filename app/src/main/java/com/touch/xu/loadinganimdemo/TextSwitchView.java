package com.touch.xu.loadinganimdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfxu on 17/5/16.
 */

public class TextSwitchView extends TextSwitcher implements ViewSwitcher.ViewFactory {

    private final static float DEFAULT_RADIUS = 5f;
    private final static int DEFAULT_BORDER_COLOR = Color.RED;
    private static final int DEFAULT_BACK_COLOR = Color.BLUE;
    private static final float DEFAULT_BORDER_WIDTH = 5f;

    private static final float DEFAULT_DURATION = 500;

    private static final int MSG_LOOP = 1;
    private static final int MSG_NEXT = 2;

    private int mIndex = -1;
    private Context mContext;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOOP:
                    mIndex = next(); //取得下标值
                    updateText(); //更新TextSwitcherd显示内容;
                    flag++;
                    break;
                case MSG_NEXT:
                    setText((String)msg.obj);
                    break;
            }
        }

        ;
    };
    private String[] resources;

    private Timer timer; //
    private int flag;
    private OnClickListener mOnClickListener;
    private float mLeftTopRectRadius;
    private float mRightBottomRectRadius;
    private float mLeftBottomRectRadius;
    private float mRightTopRectRadius;
    private int mBorderColor;
    private int mBackColor;
    private float mBorderWidth;
    private float mAnimDuration;

    private int mPaddingLeft;
    private int mPaddingRight;

    public TextSwitchView(Context context) {
        super(context);
        this.mContext = context;
        init(context, null);
    }

    public TextSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        timer = new Timer();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandedView);

        mLeftTopRectRadius = ta.getDimension(R.styleable.ExpandedView_back_left_top_radius, DEFAULT_RADIUS);
        mRightBottomRectRadius = ta.getDimension(R.styleable.ExpandedView_back_right_top_radius, DEFAULT_RADIUS);
        mLeftBottomRectRadius = ta.getDimension(R.styleable.ExpandedView_back_left_bottom_radius, DEFAULT_RADIUS);
        mRightTopRectRadius = ta.getDimension(R.styleable.ExpandedView_back_right_bottom_radius, DEFAULT_RADIUS);
        mBorderColor = ta.getColor(R.styleable.ExpandedView_back_border_color, DEFAULT_BORDER_COLOR);
        mBackColor = ta.getColor(R.styleable.ExpandedView_background_color, DEFAULT_BACK_COLOR);
        mBorderWidth = ta.getFloat(R.styleable.ExpandedView_back_border_width, DEFAULT_BORDER_WIDTH);
//        mAnimDuration = ta.getFloat(R.styleable.ExpandedView_anim_duration, DEFAULT_DURATION);

        mPaddingLeft = ta.getDimensionPixelSize(R.styleable.ExpandedView_text_padding_left, 0);
        mPaddingRight = ta.getDimensionPixelSize(R.styleable.ExpandedView_text_padding_right, 0);

        ta.recycle();

        this.setFactory(this);
        this.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_animation));
        this.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_animation));
    }

    public void setResources(String[] res) {
        this.resources = res;
    }

    public void setTextStillTime(long time) {
        timer.scheduleAtFixedRate(new MyTask(), 1, time);//每time秒更新
    }

    public void setNextText(String text) {
        Message message = Message.obtain();
        message.what = MSG_NEXT;
        message.obj = text;
        mHandler.sendMessageDelayed(message, 1000);
    }

    public void setStatusCollapse() {
        ExpandedView view = (ExpandedView) getCurrentView();
        view.setStatusCollapse();
    }

    public void setStatusExpand() {
        ExpandedView view = (ExpandedView) getCurrentView();
        view.setStatusExpand();
    }

    private class MyTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(MSG_LOOP);
        }
    }

    private int next() {
        if (flag > resources.length - 1) {
            flag = 0;
        }
        return flag;
    }

    private void updateText() {
        this.setText(resources[mIndex]);
    }


    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    @Override
    public View makeView() {
        ExpandedView tv = new ExpandedView(mContext);
        tv.setGravity(Gravity.CENTER);
        tv.setId(getId());
        tv.setText("体检报告");
        tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setPadding(mPaddingLeft, 0, mPaddingRight, 0);
        tv.setRadius(mLeftTopRectRadius, mRightTopRectRadius, mRightBottomRectRadius, mLeftBottomRectRadius);
        tv.setBorderColor(mBorderColor);
        tv.setBorderWidth(mBorderWidth);
        tv.setBackColor(mBackColor);
        tv.setDrawerAnimDuration(mAnimDuration);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                }
            }
        });
        return tv;
    }

}
