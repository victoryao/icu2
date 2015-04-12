
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.OrderEditAddressFragment;
import com.xiaomi.xms.sales.ui.OrderEditDeliverTimeFragment;
import com.xiaomi.xms.sales.ui.OrderEditDetailFragment;
import com.xiaomi.xms.sales.ui.OrderEditListFragment;
import com.xiaomi.xms.sales.ui.OrderEditTypeFragment;
import com.xiaomi.xms.sales.util.Constants;

public class OrderEditActivity extends BaseActivity {
    private final static String TAG = "OrderEditActivity";
    public final static String TAG_EDIT_ADDRESS_FRAGMENT = "edit_address_fragment";
    public final static String TAG_ORDER_LIST_FRAGMENT = "order_list_fragment";
    public final static String TAG_EDIT_TYPE_FRAGMENT = "edit_type_fragment";
    public final static String TAG_EDIT_DELIVERTIME_FRAGMENT = "edit_delivertime_fragment";
    public final static String TAG_EDIT_DETAIL_FRAGMENT = "tag_edit_detail_fragment";

    private OrderEditAddressFragment mEditAddressFragment;
    private OrderEditListFragment mOrderListFragment;
    private OrderEditTypeFragment mEditTypeFragment;
    private OrderEditDeliverTimeFragment mEditDeliverFragment;
    private OrderEditDetailFragment mOrderEditDetailFragment;
    private Bundle mBundle;
    private String mAction;
    private String mOpertion;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.order_edit_activity);
        handleIntent();
        if (TextUtils.equals(mAction, Constants.Intent.ACTION_ORDER_EDIT)) {
            showFragment(TAG_EDIT_DETAIL_FRAGMENT, mBundle, false);
        } else {
            showFragment(TAG_ORDER_LIST_FRAGMENT, null, false);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mAction = intent.getAction();
        mBundle = intent.getExtras();
        if (mBundle != null) {
            mOpertion = mBundle.getString(Constants.Intent.EXTRA_ORDER_EDIT_ACTION);
        }
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TextUtils.equals(TAG_EDIT_ADDRESS_FRAGMENT, tag)) {
            mEditAddressFragment = new OrderEditAddressFragment();
            fragment = mEditAddressFragment;
        } else if (TextUtils.equals(TAG_ORDER_LIST_FRAGMENT, tag)) {
            mOrderListFragment = new OrderEditListFragment();
            fragment = mOrderListFragment;
        } else if (TextUtils.equals(TAG_EDIT_TYPE_FRAGMENT, tag)) {
            mEditTypeFragment = new OrderEditTypeFragment();
            fragment = mEditTypeFragment;
        } else if (TextUtils.equals(TAG_EDIT_DELIVERTIME_FRAGMENT, tag)) {
            mEditDeliverFragment = new OrderEditDeliverTimeFragment();
            fragment = mEditDeliverFragment;
        } else if (TextUtils.equals(TAG_EDIT_DETAIL_FRAGMENT, tag)) {
            mOrderEditDetailFragment = new OrderEditDetailFragment();
            fragment = mOrderEditDetailFragment;
        }
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if (mOrderEditDetailFragment != null && mOrderEditDetailFragment.isVisible()
                && TextUtils.equals(mOpertion, "ADD")) {
            mOrderEditDetailFragment.showExitDialog();
        } else {
            super.onBackPressed();
        }
    }

    public void onBackPressed(boolean force) {
        if (force) {
            super.onBackPressed();
        } else {
            this.onBackPressed();
        }
    }
}
