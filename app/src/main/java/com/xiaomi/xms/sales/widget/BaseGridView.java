package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class BaseGridView extends GridView{
    private int mNumOfColumns;

    public BaseGridView(Context context) {
        super(context);
    }

    public BaseGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        mNumOfColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumOfColumns;
    }
}
