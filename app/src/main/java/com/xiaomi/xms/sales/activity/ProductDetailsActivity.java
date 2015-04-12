
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.ui.ProductDetailsFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;

public class ProductDetailsActivity extends BaseActivity {
    public static final String TAG = "ProductDetailsActivity";
    public static final String TAG_PRODUCT_DETAILS = "tag_product_details";

    private ProductDetailsFragment mProductDetailsFragment;
    private ShopIntentServiceAction mAddShoppingcartAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.product_details_activity);
        showFragment(TAG_PRODUCT_DETAILS, getIntent().getExtras(), false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_PRODUCT_DETAILS.equals(tag)) {
            mProductDetailsFragment = new ProductDetailsFragment();
            fragment = mProductDetailsFragment;
        }
        return fragment;
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (TextUtils.equals(action, Constants.Intent.ACTION_ADD_SHOPPING_CART)) {
            if (mProductDetailsFragment != null) {
                String result = callbackIntent.getStringExtra(
                        Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG);
                if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_SUCCESS)) {
                    if (mProductDetailsFragment != null && mProductDetailsFragment.isVisible()) {
                        mProductDetailsFragment.playAddCartAnimation();
                    }
                } else if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_FAIL)) {
                    if (mProductDetailsFragment != null && mProductDetailsFragment.isVisible()) {
                        mProductDetailsFragment.onAddShoppingCartFinish();
                    }
                    ToastUtil.show(this, getString(R.string.add_shopping_cart_fail));
                } else {
                    if (mProductDetailsFragment != null && mProductDetailsFragment.isVisible()) {
                        mProductDetailsFragment.onAddShoppingCartFinish();
                    }
                    ToastUtil.show(this, result);
                }
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

    @Override
    public void onBackPressed() {
        if (mProductDetailsFragment != null && mProductDetailsFragment.isVisible()) {
            if (mProductDetailsFragment.styleWindowStatus()) {
                mProductDetailsFragment.hideStyleView();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

}
