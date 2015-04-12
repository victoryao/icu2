
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.OrderExpressAdapter;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.Order.OrderExpress;
import com.xiaomi.xms.sales.model.Order.OrderExpressTrace;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import org.json.JSONObject;

import java.util.ArrayList;

public class OrderViewExpressFragment extends BaseFragment {

    public static final String ORDER_EXPRESS_LIST_TYPE_HEAD = "HEAD";
    public static final int EXPRESS_LOADER = 0;
    private OrderExpressAdapter mAdapter;
    private BaseListView mListView;
    private EmptyLoadingView mLoadingView;
    private RequestLoader mExpressLoader;
    private String mDeliverId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_view_express_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mLoadingView.setEmptyText(R.string.order_express_empty);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mDeliverId = bundle.getString(Constants.Intent.EXTRA_ORDER_EXPRESS);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(EXPRESS_LOADER, null, mExpressCallback);
        if (mExpressLoader != null) {
            Request request = new Request(HostManager.getOrderExpress());
            request.addParam(Parameters.Keys.ORDER_ID, mDeliverId);
            mExpressLoader.load(EXPRESS_LOADER, request);
        }
    }

    private LoaderCallbacks<RequestLoader.Result> mExpressCallback = new LoaderCallbacks<RequestLoader.Result>() {

        @Override
        public Loader<Result> onCreateLoader(int id, Bundle arg1) {
            mExpressLoader = new RequestLoader(getActivity());
            mLoadingView.startLoading(true);
            return mExpressLoader;
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result result) {
            getLoaderManager().destroyLoader(EXPRESS_LOADER);
            // express
            OrderExpress express = new OrderExpress();
            if (result.mData != null) {
                JSONObject expressJson = result.mData.optJSONObject(Tags.DATA);
                if (expressJson != null) {
                    express.mTraces = Order.getExpressTraces(expressJson);
                    express.mExpressId = expressJson.optString(Tags.Order.EXPRESS_ID);
                    express.mExpressName = expressJson.optString(Tags.Order.EXPRESS_NAME);
                    express.mExpressSN = expressJson.optString(Tags.Order.EXPRESS_SN);
                    express.mIsShow = expressJson.optBoolean(Tags.Order.EXPRESS_SHOW);
                    express.mUpdateTime = expressJson.optString(Tags.Order.EXPRESS_UPDATE_TIME);
                    ArrayList<OrderExpressTrace> tracks = new ArrayList<OrderExpressTrace>();
                    OrderExpressTrace oet = new OrderExpressTrace();
                    oet.mText = getString(R.string.order_express_list_sn_label, express.mExpressSN);
                    oet.mTime = getString(R.string.order_express_name_label, express.mExpressName);
                    oet.mType = Constants.OrderExpressType.ORDER_EXPRESS_LIST_TYPE_HEAD;
                    tracks.add(0, oet);
                    if (express != null && express.mTraces != null && express.mTraces.size() > 0) {
                        mLoadingView.stopLoading(true);
                        mAdapter = new OrderExpressAdapter(getActivity());
                        mListView.setAdapter(mAdapter);
                        tracks.addAll(0, express.mTraces);
                        mAdapter.updateData(tracks);
                    } else {
                        mLoadingView.stopLoading(false);
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Result> arg0) {
        }
    };
}
