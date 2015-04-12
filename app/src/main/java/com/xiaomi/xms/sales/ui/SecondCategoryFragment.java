
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.ProductActivity;
import com.xiaomi.xms.sales.activity.SecondCategoryActivity;
import com.xiaomi.xms.sales.adapter.CategoryAdapter;
import com.xiaomi.xms.sales.loader.CategoryLoader;
import com.xiaomi.xms.sales.model.CategoryInfo;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseGridView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class SecondCategoryFragment extends BaseFragment implements
        LoaderCallbacks<CategoryLoader.Result> {

    private final static int CATEGORY_LOADER = 0;

    private EmptyLoadingView mLoadingView;
    private BaseGridView mGridView;
    private CategoryAdapter mCategoryAdapter;
    public String mCategoryId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.second_category_fragment, container, false);
        mGridView = (BaseGridView) view.findViewById(R.id.grid_view);
        mCategoryAdapter = new CategoryAdapter(getActivity());
        mGridView.setAdapter(mCategoryAdapter);
        mGridView.setOnItemClickListener(mGridItemClickListner);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
    }

    @Override
    public Loader<CategoryLoader.Result> onCreateLoader(int id, Bundle arg1) {
        if (id == CATEGORY_LOADER) {
            mLoader = new CategoryLoader(getActivity(), mCategoryId);
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<CategoryLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<CategoryLoader.Result> arg0,
            CategoryLoader.Result data) {
        mCategoryAdapter.updateData(data.mCategoryInfos);
    }

    @Override
    public void onLoaderReset(Loader<CategoryLoader.Result> arg0) {
    }

    private OnItemClickListener mGridItemClickListner = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CategoryInfo mCateInfo = (CategoryInfo) mCategoryAdapter.getItem(position);
            if (mCateInfo.hasChildren()) {
                Intent intent = new Intent(getActivity(), SecondCategoryActivity.class);
                if (!TextUtils.isEmpty(mCateInfo.getCategoryId())) {
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID, mCateInfo.getCategoryId());
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_NAME, mCateInfo.getName());
                    getActivity().startActivity(intent);
                }
            } else {
                Intent intent = new Intent(getActivity(), ProductActivity.class);
                if (!TextUtils.isEmpty(mCateInfo.getCategoryId())) {
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID, mCateInfo.getCategoryId());
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_NAME, mCateInfo.getName());
                    getActivity().startActivity(intent);
                }
            }
        }
    };

    public void setCateId(String cateId) {
        mCategoryId = cateId;
    }
}
