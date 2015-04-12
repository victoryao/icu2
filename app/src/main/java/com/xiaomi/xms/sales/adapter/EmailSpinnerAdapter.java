
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseSpinnerItem;

public class EmailSpinnerAdapter extends BaseDataAdapter<String> {

    public EmailSpinnerAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, String data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.base_spinner_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, String data) {
        if (view instanceof BaseSpinnerItem) {
            ((BaseSpinnerItem) view).bind(data);
            view.setTag(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
