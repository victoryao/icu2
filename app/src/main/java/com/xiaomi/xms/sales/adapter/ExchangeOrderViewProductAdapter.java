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
import com.xiaomi.xms.sales.ui.ExchangeOrderViewProductItem;

public class ExchangeOrderViewProductAdapter extends BaseDataAdapter<ProductBrief> {

    public ExchangeOrderViewProductAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, ProductBrief data, ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.exchange_order_view_product_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, ProductBrief data) {
        if (view instanceof ExchangeOrderViewProductItem) {
            ((ExchangeOrderViewProductItem) view).bind(data);
        }
        view.setTag(data);
    }

   

}
