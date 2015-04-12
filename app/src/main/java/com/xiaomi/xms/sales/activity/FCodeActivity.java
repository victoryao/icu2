
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.ui.FCodeFragment;
import com.xiaomi.xms.sales.ui.FCodeSelectFragment;
import com.xiaomi.xms.sales.util.Constants;

public class FCodeActivity extends BaseActivity {

    public static final String TAG = "FCodeActivity";
    public static final String TAG_FCODE_FRAGMENT = "tag_fcode_fragment";
    public static final String TAG_SELECT_FRAGMENT = "tag_fcode_select_fragment";

    private FCodeFragment mFCodeFragment;
    private FCodeSelectFragment mSelectFragment;
    private ShopIntentServiceAction mFetchVcodeAction;
    private ShopIntentServiceAction mVerifyVcodeAction;
    private ShopIntentServiceAction mVerifyFcodeAction;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.fcode_activity);
        setTitle(R.string.fcode_title);
        handleIntent(getIntent());
        mFetchVcodeAction = new ShopIntentServiceAction(Constants.Intent.ACTION_FETCH_VCODE, this);
        ShopIntentService.registerAction(mFetchVcodeAction);
        mVerifyVcodeAction = new ShopIntentServiceAction(Constants.Intent.ACTION_VERIFY_VCODE, this);
        ShopIntentService.registerAction(mVerifyVcodeAction);
        mVerifyFcodeAction = new ShopIntentServiceAction(Constants.Intent.ACTION_VERIFY_FCODE, this);
        ShopIntentService.registerAction(mVerifyFcodeAction);
    }

    private void handleIntent(Intent intent) {
        String fcode = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_FCODE);
        if (!TextUtils.isEmpty(fcode)) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Intent.EXTRA_CHECKCODE_FCODE, fcode);
            showFragment(TAG_FCODE_FRAGMENT, bundle, false);
        } else {
            showFragment(TAG_FCODE_FRAGMENT, null, false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (mFCodeFragment != null) {
            mFCodeFragment.onServiceCompleted(action, callbackIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShopIntentService.unregisterAction(mFetchVcodeAction);
        ShopIntentService.unregisterAction(mVerifyVcodeAction);
        ShopIntentService.unregisterAction(mVerifyFcodeAction);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TextUtils.equals(tag, TAG_FCODE_FRAGMENT)) {
            mFCodeFragment = new FCodeFragment();
            fragment = mFCodeFragment;
        } else if (TextUtils.equals(tag, TAG_SELECT_FRAGMENT)) {
            mSelectFragment = new FCodeSelectFragment();
            fragment = mSelectFragment;
        }
        return fragment;
    }
}
