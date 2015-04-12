package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Order.DeliverOrder;
import com.xiaomi.xms.sales.ui.OrderViewDeliverItem;

public class OrderViewDeliverAdapter extends BaseDataAdapter<DeliverOrder>{

    public OrderViewDeliverAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, DeliverOrder data, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.order_deliver_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, DeliverOrder data) {
        if (view instanceof OrderViewDeliverItem) {
            ((OrderViewDeliverItem) view).bind(data);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
