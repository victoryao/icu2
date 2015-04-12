
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.adapter.BaseDataAdapter;
import com.xiaomi.xms.sales.adapter.OrderListAdapter;
import com.xiaomi.xms.sales.loader.BasePageLoader;
import com.xiaomi.xms.sales.loader.BaseResult;
import com.xiaomi.xms.sales.loader.OrderEditListLoader;
import com.xiaomi.xms.sales.model.OrderPreview;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

public class OrderEditListFragment extends BaseFragment {
    private final static int ORDERLIST_LOADER = 0;

    private BaseListView mListView;
    private EmptyLoadingView mLoadingView;
    private BaseDataAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_list_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mListView.setOnScrollListener(mOnScrollListener);
        mAdapter = new OrderListAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mLoadingView.setEmptyText(R.string.order_list_empty);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoader = (BasePageLoader) getLoader(ORDERLIST_LOADER);
        mLoader.setProgressNotifiable(mLoadingView);
        mListView.setOnItemClickListener(mItemClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.order_edit_list_title);
    }

    public BasePageLoader getLoader() {
        return (BasePageLoader) mLoader;
    }

    private Loader<BaseResult> getLoader(int loaderId) {
        LoaderManager loaderManager = getLoaderManager();
        Loader<BaseResult> loader = loaderManager.getLoader(loaderId);
        if (loader == null) {
            loaderManager.initLoader(loaderId, null, mListCallback);
            loader = loaderManager.getLoader(loaderId);
        }
        return loader;
    }

    private OnScrollListener mOnScrollListener = new PageScrollListener(new Runnable() {
        @Override
        public void run() {
            if (mLoader != null && !mLoader.isLoading() && ((BasePageLoader) mLoader).hasNextPage()) {
                mLoader.forceLoad();
            }
        }
    });

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
            Bundle bundle = new Bundle();
            OrderPreview order = (OrderPreview) mAdapter.getItem(position);
            bundle.putString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, order.getOrderId());
            bundle.putString(Constants.Intent.EXTRA_ORDER_EDIT_ACTION, "EDIT");
            ((OrderEditActivity) getActivity()).showFragment(
                    OrderEditActivity.TAG_EDIT_DETAIL_FRAGMENT, bundle, true);
        }
    };

    private LoaderCallbacks<BaseResult> mListCallback = new LoaderCallbacks<BaseResult>() {
        @Override
        public Loader onCreateLoader(int id, Bundle arg1) {
            mLoader = new OrderEditListLoader(getActivity());
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }

        @Override
        public void onLoadFinished(Loader<BaseResult> loader, BaseResult data) {
            mAdapter.updateData(((OrderEditListLoader.Result) data).mOrderList);
        }

        @Override
        public void onLoaderReset(Loader<BaseResult> loader) {
        }
    };
}
