
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class BasePageAdapter<T> extends PagerAdapter {
    private ArrayList<T> mData;
    private Context mContext;
    private HashMap<Integer, View> mViews;

    public BasePageAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<T>();
        mViews = new HashMap<Integer, View>();
    }

    public void updateData(ArrayList<T> data) {
        if (data != null) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public T getData(int position) {
        return mData.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((View) object) == view;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = position < mViews.size() ? mViews.get(position) : null;
        T data = getData(position);
        if (view == null) {
            view = newView(mContext, data, container);
            mViews.put(position, view);
        }
        container.addView(view);
        bindView(view, position, data);
        view.setId(position);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView(mViews.get(position));
    }

    public abstract View newView(Context context, T data, ViewGroup parent);

    public abstract void bindView(View view, int position, T data);
}
