
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Order.OrderExpressTrace;
import com.xiaomi.xms.sales.ui.OrderExpressTraceItem;

public class OrderExpressAdapter extends BaseDataAdapter<OrderExpressTrace> {

    public OrderExpressAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, OrderExpressTrace data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.order_express_trace_item, parent,
                false);
    }

    @Override
    public void bindView(View view, int position, OrderExpressTrace data) {
        if (view instanceof OrderExpressTraceItem) {
            ((OrderExpressTraceItem) view).bind(data, position);
        }
    }

}
