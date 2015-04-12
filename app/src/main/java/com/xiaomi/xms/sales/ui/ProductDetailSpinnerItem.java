package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class ProductDetailSpinnerItem extends BaseListItem<String> {
    private TextView mChiose;

    public ProductDetailSpinnerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChiose = (TextView)findViewById(R.id.spinner_text_item);
    }

    @Override
    public void bind(String data) {
        mChiose.setText(data);
        mChiose.setTag(data);
    }

}
