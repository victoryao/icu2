
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.OrderPreview;
import com.xiaomi.xms.sales.ui.OrderListItem;

public class OrderListAdapter extends BaseDataAdapter<OrderPreview> {

    public OrderListAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, OrderPreview data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.order_list_item,
                parent, false);
    }

    @Override
    public void bindView(View view, int position, OrderPreview data) {
        if (view instanceof OrderListItem) {
            ((OrderListItem) view).bind(data);
        }
    }

    public void deleteItem(String orderId) {
        for (OrderPreview item : getData()) {
            if (TextUtils.equals(item.getOrderId(), orderId)) {
                mData.remove(item);
                notifyDataSetChanged();
                break;
            }
        }
    }
}
