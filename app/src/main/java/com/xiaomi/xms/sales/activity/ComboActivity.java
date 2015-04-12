
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.ui.ComboFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;

public class ComboActivity extends BaseActivity {

    public static final String TAG_COMBO_FRAGMENT = "combo_fragment";

    private ComboFragment mComboFragment;
    private Bundle mBundle;
    private String mTag;
    private ShopIntentServiceAction mAddShoppingcartAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.combo_activity);
        handleIntent(getIntent());
        showFragment(mTag, mBundle, false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(getIntent());
        showFragment(mTag, mBundle, false);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_COMBO_FRAGMENT.equals(tag)) {
            mComboFragment = new ComboFragment();
            fragment = mComboFragment;
        }
        return fragment;
    }

    private void handleIntent(Intent intent) {
        mBundle = intent.getExtras();
        mTag = TAG_COMBO_FRAGMENT;
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (TextUtils.equals(action, Constants.Intent.ACTION_ADD_SHOPPING_CART)) {

            String result = callbackIntent.getStringExtra(
                    Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG);
            if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_SUCCESS)) {
                if (mComboFragment != null && mComboFragment.isVisible()) {
                    mComboFragment.playAddCartAnimation();
                }
            } else if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_FAIL)) {
                if (mComboFragment != null && mComboFragment.isVisible()) {
                    mComboFragment.onAddShoppingCartFinish();
                }
                ToastUtil.show(this, getString(R.string.add_shopping_cart_fail));
            } else {
                if (mComboFragment != null && mComboFragment.isVisible()) {
                    mComboFragment.onAddShoppingCartFinish();
                }
                ToastUtil.show(this, result);
            }

            unRegisterServiceAction();
        }
    }

    public void registerServiceAction() {
        mAddShoppingcartAction = new ShopIntentServiceAction(
                Constants.Intent.ACTION_ADD_SHOPPING_CART, this);
        ShopIntentService.registerAction(mAddShoppingcartAction);
    }

    public void unRegisterServiceAction() {
        ShopIntentService.unregisterAction(mAddShoppingcartAction);
    }
}
