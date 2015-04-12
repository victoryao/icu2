
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.OrderViewDeliverAdapter;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.util.Utils.PhoneFormat;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class OrderDeliverViewFragment extends BaseFragment {

    private static final int ORDER_INFO_LOADER = 0;
    private OrderInfoLoader mLoader;
    private String mOrderId;

    private View mHeaderView;
    private View mHeaderViewFooter;
    private BaseListView mListView;
    private EmptyLoadingView mLoadingView;
    private TextView mOrderIdText;
    private TextView mOrderFeeText;
    private TextView mOrderFeeSubtotalText;
    private TextView mOrderAddTime;
    private TextView mReceiveAddressText;
    private TextView mReceiveTimeText;
    private TextView mReceiveTimeLabel;
    private TextView mOrderInvoiceText;
    private View mReceiveInfoView;
    private OrderViewDeliverAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_deliverview_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mHeaderView = inflater.inflate(R.layout.deliver_view_header, null, false);
        mHeaderViewFooter = inflater.inflate(R.layout.order_view_footer, null, false);
        mHeaderViewFooter.setVisibility(View.GONE);
        mOrderIdText = (TextView) mHeaderView.findViewById(R.id.order_id);
        mOrderFeeText = (TextView) mHeaderView.findViewById(R.id.order_fee);
        mOrderFeeSubtotalText = (TextView) mHeaderView.findViewById(R.id.order_fee_subtotal);
        mOrderAddTime = (TextView) mHeaderView.findViewById(R.id.add_time);
        mReceiveInfoView = (LinearLayout) mHeaderView.findViewById(R.id.receive_info);
        mReceiveAddressText = (TextView) mHeaderView.findViewById(R.id.receive_address);
        mReceiveTimeText = (TextView) mHeaderView.findViewById(R.id.receive_time);
        mReceiveTimeLabel = (TextView) mHeaderView.findViewById(R.id.receive_time_label);
        mOrderInvoiceText = (TextView) mHeaderView.findViewById(R.id.invoice_info);
        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addHeaderView(mHeaderViewFooter, null, false);
        mAdapter = new OrderViewDeliverAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding),
                0,
                getResources().getDimensionPixelSize(R.dimen.list_item_padding),
                0);
        handleIntent();
        return view;
    }

    private void handleIntent() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoader.reload();
    }

    private LoaderCallbacks<OrderInfoLoader.Result> mOrderInfoCallback = new LoaderCallbacks<OrderInfoLoader.Result>() {
        @Override
        public Loader onCreateLoader(int id, Bundle arg1) {
            if (id == ORDER_INFO_LOADER) {
                mLoader = new OrderInfoLoader(getActivity());
                mLoader.setNeedSecurityKeyTask(false);
                mLoader.setOrderId(mOrderId);
                mLoader.setProgressNotifiable(mLoadingView);
                return mLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<OrderInfoLoader.Result> loader,
                OrderInfoLoader.Result data) {
            if (data != null && data.mOrderInfo != null) {
                mHeaderView.setVisibility(View.VISIBLE);
                mHeaderViewFooter.setVisibility(View.VISIBLE);
                mAdapter.updateData(data.mOrderInfo.getDeliverOrderList());
                // order id
                mOrderIdText.setText(getString(R.string.deliver_view_orderid,
                        data.mOrderInfo.getOrderId()));
                // order total price
                mOrderFeeText.setText(getString(R.string.order_view_fee,
                        Utils.Money.valueOf(data.mOrderInfo.getFee())));
                mOrderFeeSubtotalText.setText(getString(R.string.order_fee_subtotal_text,
                        data.mOrderInfo.getOriginalPrice(), data.mOrderInfo.getReducePrice(),
                        data.mOrderInfo.getShipmentExpense()));
                // invoice info
                mOrderInvoiceText.setText(data.mOrderInfo.getInvoiceInfo());
                // add time
                mOrderAddTime.setText(Utils.DateTime.formatTime(getActivity(),
                        data.mOrderInfo.getAddTime()));
                mReceiveInfoView.setVisibility(View.VISIBLE);
                mReceiveAddressText.setText(getActivity().getString(
                        R.string.order_view_address,
                        data.mOrderInfo.getProvince(),
                        data.mOrderInfo.getCity(),
                        data.mOrderInfo.getDistrict(),
                        data.mOrderInfo.getConsigneeAddress().trim(),
                        data.mOrderInfo.getConsignee(),
                        PhoneFormat.valueOf(data.mOrderInfo.getConsigneePhone())));
                mReceiveTimeText.setText(getActivity().getString(
                        R.string.order_view_address_time,
                        data.mOrderInfo.getDeliveryTime().trim()));
                if (data.mOrderInfo.isMihomeBuy()) {
                    mReceiveTimeText.setVisibility(View.GONE);
                    mReceiveTimeLabel.setVisibility(View.GONE);
                    mReceiveAddressText.setText(data.mOrderInfo.getConsigneeAddress() + "\n"
                            + data.mOrderInfo.getConsignee() + " "
                            + data.mOrderInfo.getConsigneePhone());
                }
            } else {
                if (TextUtils.isEmpty(data.mOrderError)) {
                    mLoadingView.setEmptyText(R.string.order_err);
                } else {
                    mLoadingView.setEmptyText(data.mOrderError);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<OrderInfoLoader.Result> loader) {
        }
    };
}
