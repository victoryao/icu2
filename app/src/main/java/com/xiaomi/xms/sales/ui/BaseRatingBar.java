
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RatingBar;

public class BaseRatingBar extends RatingBar {

    public BaseRatingBar(Context context) {
        super(context);
    }

    public BaseRatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BaseRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * This will ensure that at least one star is selected.
         */
        float x = event.getX();
        if (x < 0) {
            return true;
        }
        return super.onTouchEvent(event);
    }
}
