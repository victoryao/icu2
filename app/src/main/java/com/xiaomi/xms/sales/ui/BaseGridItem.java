package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class BaseGridItem<T> extends RelativeLayout {

    public BaseGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void bind(T data);
}
