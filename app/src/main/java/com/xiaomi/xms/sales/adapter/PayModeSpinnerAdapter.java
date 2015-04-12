
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.PaymentInfoLoader.PayMode;
import com.xiaomi.xms.sales.ui.PaymentModeSpinnerItem;

public class PayModeSpinnerAdapter extends BaseDataAdapter<PayMode> {
    public PayModeSpinnerAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, PayMode data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.payment_mode_spinner_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, PayMode data) {
        if (view instanceof PaymentModeSpinnerItem) {
            ((PaymentModeSpinnerItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
