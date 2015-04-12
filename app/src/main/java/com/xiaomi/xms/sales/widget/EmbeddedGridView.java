
package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.util.AttributeSet;

public class EmbeddedGridView extends BaseGridView {
    public EmbeddedGridView(Context context) {
        super(context);
    }

    public EmbeddedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmbeddedGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
