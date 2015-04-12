
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.ShopIntentService.Listener;
import com.xiaomi.xms.sales.ui.AddressAddFragment;
import com.xiaomi.xms.sales.ui.AddressListFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;

public class AddressActivity extends BaseActivity implements Listener {
    private final static String TAG = "AddressActivity";
    public final static String TAG_LIST_FRAGMENT = "address_list_fragment";
    public final static String TAG_ADD_FRAGMENT = "address_add_fragment";

    private String mAction;
    private Bundle mBundle;
    private AddressListFragment mAddressListFragment;
    private AddressAddFragment mAddressEditFragment;

    private ShopIntentServiceAction mAddAddressAction;
    private ShopIntentServiceAction mEditAddressAction;
    private ShopIntentServiceAction mDelAddressAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.address_list_activity);

        handleIntent();
        if (TextUtils.equals(mAction, Constants.Intent.ACTION_ADD_ADDRESS)) {
            showFragment(TAG_ADD_FRAGMENT, null, false);
            setTitle(R.string.add_address);
        } else if (Constants.Intent.ACTION_EDIT_ADDRESS.equals(mAction)) {
            showFragment(TAG_LIST_FRAGMENT, null, false);
            setTitle(R.string.account_addresslist);
        } else if (Constants.Intent.ACTION_USE_ADDRESS.equals(mAction)) {
            showFragment(TAG_LIST_FRAGMENT, mBundle, false);
            setTitle(R.string.use_address);
        }
        setShoppingBarEnable(false);
        mAddAddressAction = new ShopIntentServiceAction(Constants.Intent.ACTION_ADD_ADDRESS, this);
        mEditAddressAction = new ShopIntentServiceAction(Constants.Intent.ACTION_EDIT_ADDRESS, this);
        mDelAddressAction = new ShopIntentServiceAction(Constants.Intent.ACTION_DEL_ADDRESS, this);
        LogUtil.d(TAG, "AddressActivity created.");
    }

    @Override
    public void onBackPressed() {
        if (mAddressEditFragment != null && mAddressEditFragment.isVisible()) {
            if (mAddressEditFragment.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    public void onBackPressed(boolean force) {
        if (force) {
            super.onBackPressed();
        } else {
            this.onBackPressed();
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mAction = intent.getAction();
        mBundle = intent.getExtras();
        if (TextUtils.isEmpty(mAction)) {
            mAction = Constants.Intent.ACTION_USE_ADDRESS;
        }
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        LogUtil.d(TAG, "service completed. action:" + action);
        int result = callbackIntent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, 0);
        String error = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_RESULT_MSG);
        int errorId = callbackIntent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_RESULT_CODE, 0);
        if (TextUtils.equals(Constants.Intent.ACTION_DEL_ADDRESS, action)) {
            mAddressListFragment.delAddressItemComplete(result, error);
        } else if (TextUtils.equals(Constants.Intent.ACTION_ADD_ADDRESS, action)) {
            String newAddressId = callbackIntent
                    .getStringExtra(Constants.Intent.EXTRA_ADDRESS_NEWID);
            if (mAddressEditFragment != null) {
                mAddressEditFragment.addAddressComplete(result, error, errorId, newAddressId);
            }
        } else if (TextUtils.equals(Constants.Intent.ACTION_EDIT_ADDRESS, action)) {
            if (mAddressEditFragment != null) {
                mAddressEditFragment.editAddressComplete(result, error, errorId);
            }
        }
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TextUtils.equals(tag, TAG_LIST_FRAGMENT)) {
            mAddressListFragment = new AddressListFragment();
            mAddressListFragment.setAction(mAction);
            fragment = mAddressListFragment;
        } else if (TextUtils.equals(tag, TAG_ADD_FRAGMENT)) {
            mAddressEditFragment = new AddressAddFragment();
            mAddressEditFragment.setAction(mAction);
            fragment = mAddressEditFragment;
        }
        return fragment;
    }

    private boolean mAddressListReload = false;

    public void setAddressListReload(boolean reload) {
        mAddressListReload = reload;
    }

    public boolean isAddressListReload() {
        return mAddressListReload;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ShopIntentService.registerAction(mAddAddressAction);
        ShopIntentService.registerAction(mEditAddressAction);
        ShopIntentService.registerAction(mDelAddressAction);
    }

    @Override
    protected void onStop() {
        ShopIntentService.unregisterAction(mAddAddressAction);
        ShopIntentService.unregisterAction(mEditAddressAction);
        ShopIntentService.unregisterAction(mDelAddressAction);
        super.onStop();
    }

}
