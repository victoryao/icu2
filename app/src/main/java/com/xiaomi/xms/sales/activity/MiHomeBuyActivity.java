
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.ui.MiHomeBuyErrorFragment;
import com.xiaomi.xms.sales.ui.MiHomeCheckFragment;
import com.xiaomi.xms.sales.ui.MiHomeProductDetailFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;

public class MiHomeBuyActivity extends BaseActivity {

    public static final String TAG_MIHOME_CHECK_FRAGMENT = "tag_mihome_check_fragment";
    public static final String TAG_MIHOME_PRODUCT_DETAIL_FRAGMENT = "tag_mihome_product_detail_fragment";
    public static final String TAG_MIHOME_BUY_ERROR_FRAGMENT = "tag_mihome_buy_error_fragment";

    private String mAction;
    private MiHomeCheckFragment mMiHomeCheckFragment;
    private MiHomeProductDetailFragment mMiHomeProductDetailFragment;
    private MiHomeBuyErrorFragment mMiHomeBuyErrorFragment;
    private ShopIntentServiceAction mAddShoppingcartAction;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.mihomebuy_activity);
        handleIntent();
        if (TextUtils.equals(mAction, Constants.Intent.ACTION_MIHOME_CHECK)) {
            showFragment(TAG_MIHOME_CHECK_FRAGMENT, getIntent().getExtras(), false);
        } else if (TextUtils.equals(mAction, Constants.Intent.ACTION_MIHOME_PRODUCT_DETAIL)) {
            showFragment(TAG_MIHOME_PRODUCT_DETAIL_FRAGMENT, getIntent().getExtras(), false);
        } else {
            showFragment(TAG_MIHOME_BUY_ERROR_FRAGMENT, getIntent().getExtras(), false);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mAction = intent.getAction();
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_MIHOME_CHECK_FRAGMENT.equals(tag)) {
            mMiHomeCheckFragment = new MiHomeCheckFragment();
            fragment = mMiHomeCheckFragment;
        } else if (TAG_MIHOME_PRODUCT_DETAIL_FRAGMENT.equals(tag)) {
            mMiHomeProductDetailFragment = new MiHomeProductDetailFragment();
            fragment = mMiHomeProductDetailFragment;
        } else if (TAG_MIHOME_BUY_ERROR_FRAGMENT.equals(tag)) {
            mMiHomeBuyErrorFragment = new MiHomeBuyErrorFragment();
            fragment = mMiHomeBuyErrorFragment;
        }
        return fragment;
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (TextUtils.equals(action, Constants.Intent.ACTION_ADD_SHOPPING_CART)) {
            MiHomeProductDetailFragment mpdFragment = (MiHomeProductDetailFragment) getFragmentByTag(TAG_MIHOME_PRODUCT_DETAIL_FRAGMENT);

            String result = callbackIntent.getStringExtra(
                    Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG);
            if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_SUCCESS)) {
                if (mpdFragment != null && mpdFragment.isVisible()) {
                    mpdFragment.playAddCartAnimation();
                }
            } else if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_FAIL)) {
                if (mpdFragment != null && mpdFragment.isVisible()) {
                    ((MiHomeProductDetailFragment) mpdFragment).onAddShoppingCartFinish();
                }
                ToastUtil.show(this, getString(R.string.add_shopping_cart_fail));
            } else {
                if (mpdFragment != null && mpdFragment.isVisible()) {
                    ((MiHomeProductDetailFragment) mpdFragment).onAddShoppingCartFinish();
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
