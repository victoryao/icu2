
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.MiPhoneProductAdapter;
import com.xiaomi.xms.sales.loader.MiPhoneListLoader;
import com.xiaomi.xms.sales.loader.MiPhoneListLoader.Result;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

public class MiPhoneProductFragment extends BaseFragment implements
        LoaderCallbacks<MiPhoneListLoader.Result> {

    private final static int MIPHONE_PRODUCT_LOADER = 0;
    private EmptyLoadingView mLoadingView;
    private BaseListView mListView;
    public String mCategoryId;
    private MiPhoneProductAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.miphone_product_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mAdapter = new MiPhoneProductAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mOnScrollListener);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCategoryId = bundle.getString(Constants.Intent.EXTRA_CATEGORY_ID);
        }
        return view;
    }

    private OnScrollListener mOnScrollListener = new PageScrollListener(new Runnable() {
        @Override
        public void run() {
            if (!mLoader.isLoading()) {
                if (((MiPhoneListLoader) mLoader).hasNextPage()) {
                    mLoader.forceLoad();
                }
            }
        }
    });

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MIPHONE_PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        if (id == MIPHONE_PRODUCT_LOADER) {
            mLoader = new MiPhoneListLoader(getActivity(), mCategoryId, "");
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<MiPhoneListLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> arg0, Result data) {
        mAdapter.updateData(data.mMiPhoneInfos);
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

}
