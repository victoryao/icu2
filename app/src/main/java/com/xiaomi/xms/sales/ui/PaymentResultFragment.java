
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.loader.OrderInfoLoader.Result;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import java.lang.ref.WeakReference;

public class PaymentResultFragment extends BaseFragment implements
        LoaderCallbacks<OrderInfoLoader.Result> {

    private int mStatus = PAYMENT_STATUS_UNKNOWN;
    private String mOrderId;
    private boolean mUserConfirmResult;
    private TextView mPaymentResult;
    private Button mPaymentExplainBtn;
    private EmptyLoadingView mLoadingView;

    private static final int ORDER_LOADER = 0;

    public static final int PAYMENT_STATUS_OK = 0;
    public static final int PAYMENT_STATUS_FIALED = 1;
    public static final int PAYMENT_STATUS_UNKNOWN = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.payment_progress_fragment, container, false);
        mPaymentResult = (TextView) v.findViewById(R.id.payment_result);
        mPaymentExplainBtn = (Button) v.findViewById(R.id.payment_explain_btn);
        mLoadingView = (EmptyLoadingView) v.findViewById(R.id.loading);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            mUserConfirmResult = bundle.getBoolean(
                    Constants.Intent.EXTRA_PAYMENT_USER_CONFIRM_RESULT, false);
        } else {
            getActivity().finish();
        }
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ORDER_LOADER, null, this);
    }

    private void onPaymentStatusQueryCompleted() {
        mPaymentExplainBtn.setVisibility(View.GONE);
        if (mStatus == PAYMENT_STATUS_OK) {
            // 如果支付成功，那么不管用户确认是什么，都跳到该条订单的详情页
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.setAction(Constants.Intent.ACTION_VIEW_ORDER);
            intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            getActivity().finish();
        } else if (mStatus == PAYMENT_STATUS_FIALED) {
            // 支付失败，如果用户确认支付成功，那么可能由于延迟，尚未收到支付宝通知，那么提示用户
            if (mUserConfirmResult) {
                mPaymentResult.setText(R.string.payment_fail_text);
                mPaymentExplainBtn.setVisibility(View.VISIBLE);
                mPaymentExplainBtn.setText(R.string.payment_fail_explain);
                mPaymentExplainBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(HostManager.URL_XIAOMI_SHOP_PAYMENT_EXPLAIN);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
            } else {
                // 如果用户也点击支付失败，返回上页
                mHandler.sendEmptyMessage(MSG_PRESS_BACK);
            }
        } else {
            // 如果网络出现了异常，支付结果没有查询到
            mPaymentResult.setText(getActivity().getString(R.string.order_pay_state_unknown,
                    mOrderId));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        if (id == ORDER_LOADER) {
            mLoader = new OrderInfoLoader(getActivity());
            mLoader.setNeedDatabase(false);
            ((OrderInfoLoader) mLoader).setNeedSecurityKeyTask(false);
            ((OrderInfoLoader) mLoader).setOrderId(mOrderId);
            mLoader.setProgressNotifiable(mLoadingView);
            return (Loader<Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result result) {
        mStatus = PAYMENT_STATUS_UNKNOWN;
        if (result != null) {
            Order orderInfo = result.mOrderInfo;
            if (orderInfo != null) {
                int orderStatus = orderInfo.getOrderStatus();
                if (orderStatus == Tags.Order.PAYMENT_STATUS_OK
                        || orderStatus == Tags.Order.PAYMENT_STATUS_WAIT_NOTIFY_WAREHOUSE
                        || orderStatus == Tags.Order.PAYMENT_STATUS_NOTIFIED_WAREHOUST
                        || orderStatus == Tags.Order.PAYMENT_STATUS_CONTRACTPHONE_WAIT_VERIFY) {
                    mStatus = PAYMENT_STATUS_OK;
                } else if (orderStatus == Tags.Order.PAYMENT_STATUS_WAIT_PAY) {
                    mStatus = PAYMENT_STATUS_FIALED;
                }
            }
        }
        onPaymentStatusQueryCompleted();
    }

    @Override
    public void onLoaderReset(Loader<Result> loader) {
        if (loader != null) {
            ((OrderInfoLoader) mLoader).setOrderId(mOrderId);
        }
    }

    private UIHandler mHandler = new UIHandler(this);
    private static final int MSG_PRESS_BACK = 0;

    private static class UIHandler extends Handler {
        private final WeakReference<PaymentResultFragment> mFragment;

        public UIHandler(PaymentResultFragment fragment) {
            mFragment = new WeakReference<PaymentResultFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_PRESS_BACK) {
                PaymentResultFragment refer = mFragment.get();
                if (refer != null)
                    refer.getActivity().onBackPressed();
            }
        }
    }
}
