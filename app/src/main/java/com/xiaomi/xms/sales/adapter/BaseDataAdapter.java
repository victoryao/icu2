package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.LogUtil;

import java.util.ArrayList;

public abstract class BaseDataAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected ArrayList<T> mData;
    protected boolean mDataValid;
    private final static String TAG = "BaseDataAdapter";

    public BaseDataAdapter(Context context) {
        mContext = context;
        mDataValid = false;
    }

    public void updateData(ArrayList<T> data) {
        if (data != null) {
            mDataValid = true;
            mData = data;
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }

    /**
     * get a copy of the data from the adapter change the data won't affect the
     * adapter use updateData() to change the adapter content if needed
     *
     * @return
     */
    public ArrayList<T> getData() {
        ArrayList<T> data = new ArrayList<T>();
        if (mData != null) {
            data.addAll(mData);
        }
        return data;
    }

    @Override
    public int getCount() {
        if (mDataValid && mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mDataValid && mData != null) {
            return mData.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mData != null) {
            return position;
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtil.d(TAG , "getView: " + position);
        if (!mDataValid) {
            throw new IllegalStateException(
                    "this should only be called when the data is valid");
        }
        if (position < 0 || position >= mData.size()) {
            throw new IllegalStateException(
                    "couldn't get view at this position " + position);
        }
        T data = mData.get(position);
        View v;
        if (convertView == null) {
            v = newView(mContext, data, parent);
        } else {
            v = convertView;
        }
        bindView(v, position, data);
        bindBackground(v, position);
        return v;
    }

    protected void bindBackground(View view, int position) {
        // 如果仅有一条
        if (getCount() == 1) {
            view.setBackgroundResource(R.drawable.list_item_single_bg);
        } else if (position == 0) {
            view.setBackgroundResource(R.drawable.list_item_top_bg);
        } else if (position == getCount() - 1) {
            view.setBackgroundResource(R.drawable.list_item_bottom_bg);
        } else {
            view.setBackgroundResource(R.drawable.list_item_middle_bg);
        }
    }

    public abstract View newView(Context context, T data, ViewGroup parent);

    public abstract void bindView(View view, int position, T data);
}
