
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.adapter.BaseDataAdapter;
import com.xiaomi.xms.sales.adapter.OrderListAdapter;
import com.xiaomi.xms.sales.loader.BasePageLoader;
import com.xiaomi.xms.sales.loader.BaseResult;
import com.xiaomi.xms.sales.loader.OrderListLoader;
import com.xiaomi.xms.sales.model.OrderPreview;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;

public class OrderListFragment extends BaseFragment {
    private static final String TAG = "OrderListFragment";
    private BaseListView mListView;
    private EmptyLoadingView mLoadingView;
    private BaseDataAdapter mAdapter;
    private String mAction;
    private String mType;
    private final static int ORDERLIST_LOADER = 0;

    public void setAction(String action) {
        mAction = action;
    }

    public void setType(String type) {
        mType = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_list_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mListView.setOnScrollListener(mOnScrollListener);
        if (TextUtils.equals(mAction, Constants.Intent.ACTION_ORDER_LIST)) {
            mAdapter = new OrderListAdapter(getActivity());
        }
        mListView.setAdapter(mAdapter);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mLoadingView.setEmptyText(R.string.order_list_empty);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (TextUtils.equals(mAction, Constants.Intent.ACTION_ORDER_LIST)) {
            mLoader = (BasePageLoader) getLoader(ORDERLIST_LOADER);
        }
        if (mLoader != null) {
            mLoader.setProgressNotifiable(mLoadingView);
        }
        mListView.setOnItemClickListener(mItemClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        if (mLoader != null) {
        	mLoader.reload();
        }
    }

    private void setTitle() {
        if (TextUtils.equals(mType, Tags.Order.ORDER_STATUS_OPEN)) {
            getActivity().setTitle(R.string.account_my_order_list);
        } else if (TextUtils.equals(mType, Tags.Order.ORDER_STATUS_CLOSE)) {
            getActivity().setTitle(R.string.account_my_end_list);
        } else if (TextUtils.equals(mType, Tags.Order.ORDER_STATUS_CHANGE)) {
            getActivity().setTitle(R.string.account_my_change_list);
        } else if (TextUtils.equals(mType, Tags.Order.ORDER_STATUS_REFUND)) {
            getActivity().setTitle(R.string.account_my_refund_list);
        } else if (TextUtils.equals(mType, Tags.Order.ORDER_STATUS_WAIT_PAYMENT)) {
            getActivity().setTitle(R.string.account_wait_pay_list);
        } else if (TextUtils.equals(mType, Tags.Order.ORDER_STATUS_EXPRESS)) {
            getActivity().setTitle(R.string.account_express_list);
        }
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
            if (TextUtils.equals(mAction, Constants.Intent.ACTION_ORDER_LIST) && mLoader != null
                    && !mLoader.isLoading() && ((BasePageLoader) mLoader).hasNextPage()) {
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
            if (order.getDeliverCount() > 1) {
                ((OrderListActivity) getActivity()).showFragment(
                        OrderListActivity.TAG_ORDER_DELIVERVIEW,
                        bundle, true);
                return;
            }
            bundle.putBoolean(Constants.Intent.EXTRA_ONLY_VIEW, true);
            ((OrderListActivity) getActivity()).showFragment(OrderListActivity.TAG_ORDER_VIEW,
                    bundle, true);
        }
    };

    private LoaderCallbacks<BaseResult> mListCallback = new LoaderCallbacks<BaseResult>() {
        @Override
        public Loader onCreateLoader(int id, Bundle arg1) {
            if (id == ORDERLIST_LOADER) {
                mLoader = new OrderListLoader(getActivity(), mType);
            }
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }

        @Override
        public void onLoadFinished(Loader<BaseResult> loader, BaseResult data) {
        	if(data != null ){
        		OrderListLoader.Result result = (OrderListLoader.Result) data;
        		if(result != null && result.mOrderList != null){
        			mAdapter.updateData(result.mOrderList);
        		}
        	}
            
        }

        @Override
        public void onLoaderReset(Loader<BaseResult> loader) {
        }
    };

    @Override
    protected void onNetworkConnected(int type) {
        if (TextUtils.equals(mAction, Constants.Intent.ACTION_ORDER_LIST)
                || TextUtils.equals(mAction, Constants.Intent.ACTION_REPAIR_LIST)) {
            if (mLoader != null) {
                mLoader.reload();
            }
        }
    }
}
