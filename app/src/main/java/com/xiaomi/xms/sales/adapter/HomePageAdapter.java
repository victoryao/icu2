package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.HomeInfo;
import com.xiaomi.xms.sales.ui.HomeFullScreenListItem;

public class HomePageAdapter extends BasePageAdapter<HomeInfo> {
    private OnPageItemClickListener mPageItemClickListener;

    public interface OnPageItemClickListener {
        public void onClick();
    }

    public void setOnPageItemClickListener(OnPageItemClickListener l) {
        mPageItemClickListener = l;
    }

    public HomePageAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, HomeInfo data, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_full_screen_item, parent,
                false);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPageItemClickListener != null) {
                    mPageItemClickListener.onClick();
                }
            }
        });
        return view;
    }

    @Override
    public void bindView(View view, int position, HomeInfo data) {
        if (view instanceof HomeFullScreenListItem) {
            ((HomeFullScreenListItem) view).bind(data);
        }
    }
}
