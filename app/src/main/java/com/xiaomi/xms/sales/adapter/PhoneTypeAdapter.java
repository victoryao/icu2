package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.PhoneModelInfo;
import com.xiaomi.xms.sales.ui.PhoneTypeItem;

public class PhoneTypeAdapter extends BaseDataAdapter<PhoneModelInfo>{

    public PhoneTypeAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, PhoneModelInfo data, ViewGroup parent) {
        return (PhoneTypeItem)LayoutInflater.from(context).inflate(R.layout.phone_type_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, PhoneModelInfo data) {
        if (view instanceof PhoneTypeItem) {
            ((PhoneTypeItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
