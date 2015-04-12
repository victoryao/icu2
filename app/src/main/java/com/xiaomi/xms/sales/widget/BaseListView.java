
package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class BaseListView extends ListView {

    private OnLayoutListener mOnLayoutListener;

    public BaseListView(Context context) {
        super(context);
    }

    public BaseListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mOnLayoutListener != null) {
            mOnLayoutListener.beforeOnLyaout();
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void setOnLayoutListener(OnLayoutListener l) {
        mOnLayoutListener = l;
    }

    public interface OnLayoutListener {
        public void beforeOnLyaout();
    }
}
