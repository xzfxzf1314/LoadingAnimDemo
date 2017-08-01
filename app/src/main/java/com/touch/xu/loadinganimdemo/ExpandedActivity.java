package com.touch.xu.loadinganimdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ExpandedActivity extends AppCompatActivity {

    private ExpandedView mExpandedView;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanded);


        mExpandedView  = (ExpandedView) findViewById(R.id.ev_demo);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (view.isExpand()) {
//                    view.setStatusCollapse();
//                } else if (view.isCollapse()) {
//                    view.setStatusExpand();
//                }
//            }
//        });

        mListView = (ListView) findViewById(R.id.expand_lv);
        DemoAdapter adapter = new DemoAdapter(this);
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            texts.add("一列....." + i);
        }
        adapter.setDatas(texts);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mExpandedView.setNextText("低电量" + position);
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lvIndext = 0;
            /**
             *滑动距离响应的临界值，这个值可根据需要自己指定
             *只有只有滑动距离大于mScrollThreshold，才会响应滑动动作
             */
            private int mScrollThreshold;
            private int mLastScrollY; //第一个可视的item的顶部坐标
            private int mPreviousFirstVisibleItem; //上一次滑动的第一个可视item的索引值
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(totalItemCount != 0) {
                    // 滑动距离：不超过一个item的高度
                    if(this.isSameRow(firstVisibleItem)) {
                        int newScrollY = this.getTopItemScrollY();
                        //判断滑动距离是否大于 mScrollThreshold
                        boolean isSignificantDelta = Math.abs(this.mLastScrollY - newScrollY) > this.mScrollThreshold;
                        if(isSignificantDelta) {
                            //对于第一个可视的item，根据其前后两次的顶部坐标判断滑动方向
                            if(this.mLastScrollY > newScrollY) {
//                                this.onScrollUp();
                                mExpandedView.setStatusCollapse();
                            } else {
//                                this.onScrollDown();
                                mExpandedView.setStatusExpand();
                            }
                        }
                        this.mLastScrollY = newScrollY;
                    } else {//根据第一个可视Item的索引值不同，判断滑动方向
                        if(firstVisibleItem > this.mPreviousFirstVisibleItem) {
                            mExpandedView.setStatusCollapse();
//                            this.onScrollUp();
                        } else {
//                            this.onScrollDown();
                            mExpandedView.setStatusExpand();
                        }
                        this.mLastScrollY = this.getTopItemScrollY();
                        this.mPreviousFirstVisibleItem = firstVisibleItem;
                    }
                }
            }

            private int getTopItemScrollY() {
                if(mListView != null && mListView.getChildAt(0) != null) {
                    View topChild = mListView.getChildAt(0);
                    return topChild.getTop();
                } else {
                    return 0;
                }
            }

            private boolean isSameRow(int firstVisibleItem) {
                return firstVisibleItem == this.mPreviousFirstVisibleItem;
            }
        });
    }


    private class DemoAdapter extends BaseAdapter {

        private final Context mContext;
        private List<String> mDatas;
        private LayoutInflater mInflater;

        public DemoAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public void setDatas(List<String> datas) {
            mDatas = datas;
        }

        @Override
        public int getCount() {
            return mDatas == null ? 0 : mDatas.size();
        }

        @Override
        public String getItem(int position) {
            return mDatas == null || mDatas.isEmpty() ? null : mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder.mTextView = (TextView)convertView.findViewById(R.id.tv_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mTextView.setText(getItem(position));

            return convertView;
        }
    }

    private class ViewHolder {
        TextView mTextView;
    }
}
