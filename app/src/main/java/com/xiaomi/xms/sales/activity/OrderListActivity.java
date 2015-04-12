
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.ui.OrderDeliverViewFragment;
import com.xiaomi.xms.sales.ui.OrderListFragment;
import com.xiaomi.xms.sales.ui.OrderViewExpressFragment;
import com.xiaomi.xms.sales.ui.OrderViewFragment;
import com.xiaomi.xms.sales.util.Constants;

public class OrderListActivity extends BaseActivity implements
        OrderViewFragment.OnOrderStatusChangedListener {

    public static final String TAG_ORDER_LIST = "tag_order_list";
    public static final String TAG_ORDER_VIEW = "tag_order_view";
    public static final String TAG_ORDER_EXPRESS = "tag_order_express";
    public static final String TAG_ORDER_DELIVERVIEW = "tag_order_deliverview";

    private OrderViewFragment mOrderViewFragment;
    private OrderListFragment mOrderListFragment;
    private OrderDeliverViewFragment mOrderDeliverViewFragment;
    private OrderViewExpressFragment mOrderViewExpressFragment;
    private ShopIntentServiceAction mCancelOrderAction;
    private ShopIntentServiceAction mRefundOrderAction;
    private String mType;
    private String mAction;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.order_list_activity);
        handleIntent();

        if (TextUtils.equals(mAction, Constants.Intent.ACTION_VIEW_ORDER)) {
            showFragment(TAG_ORDER_VIEW, getIntent().getExtras(), false);
        } else if (TextUtils.equals(mAction, Constants.Intent.ACTION_ORDER_LIST)) {
            showFragment(TAG_ORDER_LIST, null, false);
        } else if (TextUtils.equals(mAction, Constants.Intent.ACTION_REPAIR_LIST)) {
            showFragment(TAG_ORDER_LIST, null, false);
            setTitle(R.string.account_my_repair_list);
        } else if ((TextUtils.equals(mAction, Constants.Intent.ACTION_VIEW_DELIVERORDER))) {
            showFragment(TAG_ORDER_DELIVERVIEW, getIntent().getExtras(), false);
        }
        mCancelOrderAction = new ShopIntentServiceAction(Constants.Intent.ACTION_CANCEL_ORDER, this);
        mRefundOrderAction = new ShopIntentServiceAction(Constants.Intent.ACTION_ORDER_REFUND, this);
        ShopIntentService.registerAction(mCancelOrderAction);
        ShopIntentService.registerAction(mRefundOrderAction);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShopIntentService.unregisterAction(mCancelOrderAction);
        ShopIntentService.unregisterAction(mRefundOrderAction);
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (mOrderViewFragment != null) {
            mOrderViewFragment.onServiceCompleted(action, callbackIntent);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mAction = intent.getAction();
        mType = intent.getStringExtra(Constants.Intent.EXTRA_ORDER_LIST_TYPE);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_ORDER_LIST.equals(tag)) {
            mOrderListFragment = new OrderListFragment();
            mOrderListFragment.setAction(mAction);
            mOrderListFragment.setType(mType);
            fragment = mOrderListFragment;
        } else if (TAG_ORDER_VIEW.equals(tag)) {
            mOrderViewFragment = new OrderViewFragment();
            mOrderViewFragment.setOrderStatusListener(this);
            fragment = mOrderViewFragment;
        } else if (TAG_ORDER_EXPRESS.equals(tag)) {
            mOrderViewExpressFragment = new OrderViewExpressFragment();
            fragment = mOrderViewExpressFragment;
        } else if (TAG_ORDER_DELIVERVIEW.equals(tag)) {
             mOrderDeliverViewFragment = new OrderDeliverViewFragment();
            fragment = mOrderDeliverViewFragment;
        }
        return fragment;
    }

    @Override
    public void onOrderStatusChanged() {
        if (mOrderListFragment != null) {
            mOrderListFragment.getLoader().reload();
        }
    }
}
