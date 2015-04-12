package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader.PrinterMode;

public class PrinterModeSpinnerItem extends BaseListItem<PrinterMode> {
    private TextView mItem;

    public PrinterModeSpinnerItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mItem = (TextView) findViewById(R.id.spinner_text_item);
    }

    @Override
    public void bind(PrinterMode data) {
    	if(data != null && data.mModeValue != null && data.mModeValue.length() > 0){
    		mItem.setText(data.mModeValue);
    	}
    }

}