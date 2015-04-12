
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.CategoryInfo;
import com.xiaomi.xms.sales.ui.CategoryListItem;

public class CategoryAdapter extends BaseDataAdapter<CategoryInfo> {
    private static final int LIST_ITEM_TYPE_COUNT = 2;
    private static final int LIST_ITME_TYPE_ITEM = 1;

    public boolean hasItemViewBound;

    public CategoryAdapter(Context context) {
        super(context);
    }

    @Override
    public void bindView(View view, int position, CategoryInfo data) {
        if (view instanceof CategoryListItem) {
            ((CategoryListItem) view).bind(data);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return LIST_ITME_TYPE_ITEM;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public boolean isEnabled(int position) {
        return position >= 0;
    }

    @Override
    public int getViewTypeCount() {
        return LIST_ITEM_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.category_list_item, parent, false);
        }

        bindView(convertView, position, (CategoryInfo) getItem(position));
        hasItemViewBound = true;
        return convertView;
    }

    @Override
    public View newView(Context context, CategoryInfo data, ViewGroup parent) {
        return null;
    }
}
