
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.ui.ProductDetailsItem;

public class ProductDetailsAdapter extends BaseDataAdapter<ProductDetailsInfoItem> {
    public ProductDetailsAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, ProductDetailsInfoItem data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_details_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, ProductDetailsInfoItem data) {
        if (view instanceof ProductDetailsItem) {
            ((ProductDetailsItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
