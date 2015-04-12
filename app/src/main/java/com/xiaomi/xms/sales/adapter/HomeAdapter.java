
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.HomeInfo;
import com.xiaomi.xms.sales.ui.HomeListItem;

public class HomeAdapter extends BaseDataAdapter<HomeInfo> {
    public HomeAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, HomeInfo data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.home_list_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, HomeInfo data) {
        if (view instanceof HomeListItem) {
            ((HomeListItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
