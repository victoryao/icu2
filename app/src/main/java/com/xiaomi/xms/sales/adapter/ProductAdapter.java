
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.ui.ProductListItem;
import com.xiaomi.xms.sales.util.LogUtil;

import java.util.ArrayList;

public class ProductAdapter extends BaseDataAdapter<ProductInfo> {

    public static final int HEADER_ITEM_NUM = 2;
    private boolean mWithExtraHeader;

    public ProductAdapter(Context context, boolean withExtraHeader) {
        super(context);
        mWithExtraHeader = withExtraHeader;
    }

    public boolean isHeaderItem(int position) {
        return mWithExtraHeader && position < HEADER_ITEM_NUM;
    }

    @Override
    public void updateData(ArrayList<ProductInfo> data) {
        if (mWithExtraHeader && data != null && !data.isEmpty()) {
            ArrayList<ProductInfo> wrappedData = new ArrayList<ProductInfo>();
            ProductInfo header = new ProductInfo("", "", "", "", "", false, null);
            wrappedData.add(header);
            wrappedData.add(header);
            wrappedData.addAll(data);
            super.updateData(wrappedData);
        } else {
            super.updateData(data);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtil.d("ProductAdapter", "getView: " + position);
        if (!mDataValid) {
            throw new IllegalStateException(
                    "this should only be called when the data is valid");
        }
        if (position < 0 || position >= mData.size()) {
            throw new IllegalStateException(
                    "couldn't get view at this position " + position);
        }
        if (isHeaderItem(position)) {
            Button b = new Button(mContext);
            b.setHeight((int) ShopApp.getContext().getResources()
                    .getDimension(R.dimen.product_grid_header_view_height));
            b.setBackgroundColor(0x00000000);
            return b;
        }
        ProductInfo data = mData.get(position);
        View v = newView(mContext, data, parent);
        bindView(v, position, data);
        bindBackground(v, position);
        return v;
    }

    @Override
    public View newView(Context context, ProductInfo data, ViewGroup parent) {
        return (ProductListItem) LayoutInflater.from(context).inflate(R.layout.product_grid_item,
                parent, false);
    }

    @Override
    public void bindView(View view, int position, ProductInfo data) {
        if (view instanceof ProductListItem) {
            ((ProductListItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
