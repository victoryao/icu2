
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class BaseSpinnerItem extends BaseListItem<String> {
    private TextView mItem;

    public BaseSpinnerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mItem = (TextView) findViewById(R.id.spinner_text_item);
    }

    @Override
    public void bind(String data) {
        mItem.setText(data);
    }

}
