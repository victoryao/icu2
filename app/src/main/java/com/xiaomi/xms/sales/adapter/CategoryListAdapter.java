
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.widget.EmbeddedGridView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

public class CategoryListAdapter extends BaseDataAdapter<Object> {

    private CategoryAdapter mCategoryAdapter;

    private OnItemClickListener mItemClickListner;

    private View mCoverView;

    public CategoryListAdapter(Context context, CategoryAdapter categoryAdapter,
            OnItemClickListener itemClickListner) {
        super(context);
        mCategoryAdapter = categoryAdapter;
        mItemClickListner = itemClickListner;
    }

    @Override
    public View newView(Context context, Object data, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_gridview, parent, false);
        EmbeddedGridView gridView = (EmbeddedGridView) view.findViewById(R.id.grid_view);
        gridView.setAdapter(mCategoryAdapter);
        gridView.setOnItemClickListener(mItemClickListner);
        gridView.setOnScrollListener(new PageScrollListener(null));
        mCoverView = view.findViewById(R.id.cover_view);
        return view;
    }

    @Override
    public void bindView(View view, int position, Object data) {
    }

    @Override
    protected void bindBackground(View view, int position) {
    }

    public void showCoverView(boolean show) {
        mCoverView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
