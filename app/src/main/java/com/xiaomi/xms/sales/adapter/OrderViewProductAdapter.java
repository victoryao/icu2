/**
 * @author
 * @since
**/

package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.ui.OrderViewProductItem;

public class OrderViewProductAdapter extends BaseDataAdapter<ProductBrief> {

    public OrderViewProductAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, ProductBrief data, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.order_view_product_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, ProductBrief data) {
        if (view instanceof OrderViewProductItem) {
            ((OrderViewProductItem) view).bind(data);
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
