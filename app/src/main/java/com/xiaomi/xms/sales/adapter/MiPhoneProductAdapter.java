
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.MiPhoneInfo;
import com.xiaomi.xms.sales.ui.MiPhoneProductListItem;

public class MiPhoneProductAdapter extends BaseDataAdapter<MiPhoneInfo> {

    public MiPhoneProductAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, MiPhoneInfo data, ViewGroup parent) {
        return (MiPhoneProductListItem) LayoutInflater.from(context).inflate(
                R.layout.miphone_product_list_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, MiPhoneInfo data) {
        if (view instanceof MiPhoneProductListItem) {
            ((MiPhoneProductListItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }

}
