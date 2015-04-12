package com.xiaomi.xms.sales.widget;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.xiaomi.xms.sales.loader.ImageLoader;

public class PageScrollListener implements OnScrollListener {
    private Runnable mCallback;
    private boolean mIsLastItem;
    private boolean mOnlyOnePage;
    public PageScrollListener(Runnable callback) {
        mCallback = callback;
        mIsLastItem = false;
        mOnlyOnePage = false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        mIsLastItem = (totalItemCount == firstVisibleItem + visibleItemCount);
        mOnlyOnePage = (totalItemCount == visibleItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mIsLastItem && mCallback != null && !mOnlyOnePage) {
                mCallback.run();
            }
        }

        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            ImageLoader.getInstance().pauseLoading();
        } else {
            ImageLoader.getInstance().resumeLoading();
        }
    }
}
