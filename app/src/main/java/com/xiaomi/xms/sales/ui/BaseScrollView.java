
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class BaseScrollView extends ScrollView {

    private boolean mInterceptTouch;
    private boolean mModifyInterceptTouch = false;

    public BaseScrollView(Context context) {
        super(context);
    }

    public BaseScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BaseScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInterceptTouch(boolean interceptTouch) {
        mModifyInterceptTouch = true;
        mInterceptTouch = interceptTouch;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mModifyInterceptTouch)
            return mInterceptTouch;
        return super.onInterceptTouchEvent(ev);
    }
}
