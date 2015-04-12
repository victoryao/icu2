
package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SelfManagedTouchLinearLayout extends LinearLayout {

    public SelfManagedTouchLinearLayout(Context context) {
        super(context);
    }

    public SelfManagedTouchLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

}
