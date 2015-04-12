package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.PaymentInfoLoader.PayMode;

public class PaymentModeSpinnerItem extends BaseListItem<PayMode> {
    private TextView mItem;

    public PaymentModeSpinnerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mItem = (TextView) findViewById(R.id.spinner_text_item);
    }

    @Override
    public void bind(PayMode data) {
    	if(data != null && data.mModeValue != null && data.mModeValue.length() > 0){
    		mItem.setText(data.mModeValue);
    	}
    }

}