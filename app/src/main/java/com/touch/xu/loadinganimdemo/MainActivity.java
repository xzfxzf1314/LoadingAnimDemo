package com.touch.xu.loadinganimdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements NewSubmitView.OnSubmitFinishListener, DottedLineView.OnDashFinishListener {

    private NewSubmitView mSubmitView1;
    private DottedLineView mDottedLineView1;

    private NewSubmitView mSubmitView2;
    private DottedLineView mDottedLineView2;

    private NewSubmitView mSubmitView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDottedLineView1 = (DottedLineView) findViewById(R.id.dlv_loading_1);
        mDottedLineView1.setOnDashFinishListener(this);

        mSubmitView2 = (NewSubmitView) findViewById(R.id.nsv_loading_2);
        mSubmitView2.setOnFinishListener(this);

        mSubmitView3 = (NewSubmitView) findViewById(R.id.nsv_loading_3);
        mSubmitView3.setOnFinishListener(this);

        mSubmitView1 = (NewSubmitView) findViewById(R.id.nsv_loading_1);
        mSubmitView1.setOnFinishListener(this);

        mDottedLineView2 = (DottedLineView) findViewById(R.id.dlv_loading_2);
        mDottedLineView2.setOnDashFinishListener(this);

        findViewById(R.id.tv_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubmitView1.startRotate();
                delayEnd();
            }
        });


        findViewById(R.id.bt_wave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchWaveActivity();
            }
        });

        findViewById(R.id.bt_anim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAnimActivity();
            }
        });

        findViewById(R.id.bt_expand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchExpandActivity();
            }
        });

        findViewById(R.id.bt_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchUnderlineActivity();
            }
        });
    }

    private void launchUnderlineActivity() {
        Intent intent = new Intent();
        intent.setClass(this, UnderlineActivity.class);
        startActivity(intent);
    }

    private void launchExpandActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ExpandedActivity.class);
        startActivity(intent);
    }

    private void launchAnimActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AnimActivity.class);
        startActivity(intent);
    }

    private void launchWaveActivity() {
        Intent intent = new Intent();
        intent.setClass(this, WaveActivity.class);
        startActivity(intent);
    }

    private void delayEnd() {
        mSubmitView1.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSubmitView1.stopRotate();
            }
        }, 3000);
    }

    private void submitView2Rotate() {
        mSubmitView2.startRotate();
        mSubmitView2.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSubmitView2.stopRotate();
            }
        }, 3000);
    }

    private void submitView3Rotate() {
        mSubmitView3.startRotate();
        mSubmitView3.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSubmitView3.stopRotate();
            }
        }, 3000);
    }

    @Override
    public void onSubmitFinish(View view) {
        switch (view.getId()) {
            case R.id.nsv_loading_1:
                mDottedLineView1.startMove();
                break;
            case R.id.nsv_loading_2:
                mDottedLineView2.startMove();
                break;
        }
    }

    @Override
    public void onDashFinish(View view) {
        switch (view.getId()) {
            case R.id.dlv_loading_1:
                submitView2Rotate();
                break;
            case R.id.dlv_loading_2:
                submitView3Rotate();
                break;
        }
    }
}
