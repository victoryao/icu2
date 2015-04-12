package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class BaseListItem<T> extends RelativeLayout {

    public BaseListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void bind(T data);
}
