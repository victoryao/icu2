
package com.xiaomi.xms.sales.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode.IncastProduct;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.SelectableProduct;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.CheckoutFragment;
import com.xiaomi.xms.sales.ui.EditCartItemFragment;
import com.xiaomi.xms.sales.ui.IncastProductsFragment;
import com.xiaomi.xms.sales.ui.OrderSubmitFragment;
import com.xiaomi.xms.sales.ui.ShoppingFragment;
import com.xiaomi.xms.sales.ui.ShoppingFragment.OnCheckStatusListener;
import com.xiaomi.xms.sales.ui.ShoppingProductFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;

public class ShoppingActivity extends BaseActivity implements
        OnCheckStatusListener {
    private static final String TAG = "ShoppingActivity";
    private ShopIntentServiceAction mActionDelete;
    private ShopIntentServiceAction mActionEdit;
    private ShopIntentServiceAction mOrderSubmitAction;
    private ShopIntentServiceAction mAddCartServiceAction;
    private ShopIntentServiceAction mAddCartIncastProductServiceAction;
    // private ShopIntentServiceAction mFetchDefenseVcodeAction;
    private Object mNextStepAfterDelete;
    private String mMihomeBuyId = null;

    public static class Fragments {
        public static final String TAG_SHOPPING_FRAGMENT = "shopping_fragment";
        public static final String TAG_CHECKOUT_FRAGMENT = "checkout_fragment";
        public static final String TAG_EDIT_CARTITEM_FRAGMENT = "edit_cartitem_fragment";
        public static final String TAG_ORDER_SUBMIT_FRAGMENT = "order_submit_fragment";
        public static final String TAG_SHOPPING_PRODUCT_FRAGMENT = "shopping_product_fragment";
        public static final String TAG_INCAST_PRODUCTS_FRAGMENT = "incast_products_fragment";

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.shopping_activity);
        Bundle bundle = getIntent().getExtras();
        showFragment(Fragments.TAG_SHOPPING_FRAGMENT, bundle, false);
        if (bundle != null)
            mMihomeBuyId = bundle.getString(Constants.Intent.EXTRA_MIHOME_BUY);
        if (mMihomeBuyId == null)
            mMihomeBuyId = HostManager.Parameters.Values.MIHOME_BUY_NULL;
        setShoppingBarEnable(false);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (Fragments.TAG_CHECKOUT_FRAGMENT.equals(tag)) {
            fragment = new CheckoutFragment();
        } else if (Fragments.TAG_EDIT_CARTITEM_FRAGMENT.equals(tag)) {
            fragment = new EditCartItemFragment();
            ((EditCartItemFragment) fragment).setOnCheckStatusListener(this);
        } else if (Fragments.TAG_ORDER_SUBMIT_FRAGMENT.equals(tag)) {
            fragment = new OrderSubmitFragment();
        }else if (Fragments.TAG_SHOPPING_FRAGMENT.equals(tag)) {
            fragment = new ShoppingFragment();
            ((ShoppingFragment) fragment).setOnCheckStatusListener(this);
        } else if (Fragments.TAG_SHOPPING_PRODUCT_FRAGMENT.equals(tag)) {
            fragment = new ShoppingProductFragment();
            ((ShoppingProductFragment) fragment).setOnCheckStatusListener(this);
        } else if (Fragments.TAG_INCAST_PRODUCTS_FRAGMENT.equals(tag)) {
            fragment = new IncastProductsFragment();
        }
        return fragment;
    }

    public BaseFragment getFragment(String tag) {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

    public static class Signal {
        public static boolean CART_RELOAD = false;
    }

    @Override
    protected void onStart() {
        mOrderSubmitAction = new ShopIntentServiceAction(Constants.Intent.ACTION_ORDER_SUBMIT, this);
        ShopIntentService.registerAction(mOrderSubmitAction);
        
        mActionDelete = new ShopIntentServiceAction(Constants.Intent.ACTION_DELETE_CARTITEM, this);
        ShopIntentService.registerAction(mActionDelete);
        mActionEdit = new ShopIntentServiceAction(Constants.Intent.ACTION_EDIT_CONSUMPTION, this);
        ShopIntentService.registerAction(mActionEdit);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ShopIntentService.unregisterAction(mOrderSubmitAction);
        ShopIntentService.unregisterAction(mActionDelete);
        ShopIntentService.unregisterAction(mActionEdit);
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (TextUtils.equals(action, Constants.Intent.ACTION_DELETE_CARTITEM)) {
            String jsonString = callbackIntent
                    .getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
            LogUtil.d(TAG, "onServiceCompleted: json string is" + jsonString);
            boolean isDel = false;
            try {
                JSONObject json = new JSONObject(jsonString);
                if (Tags.isJSONReturnedOK(json)) {
                    isDel = true;
                } else {
                    isDel = false;
                }
            } catch (Exception e) {
                isDel = false;
            }
            if (isDel) {
                ShoppingActivity.Signal.CART_RELOAD = true;
                if (mNextStepAfterDelete != null) {
                    if (OnCheckStatusListener.NEXT_ACTION_BACK.equals(mNextStepAfterDelete)) {
                        onBackPressed();
                    } else if (mNextStepAfterDelete instanceof EditCartItemFragment.NextStepInfo) {
                        SelectableProduct selectableProduct = ((EditCartItemFragment.NextStepInfo) mNextStepAfterDelete).selectableProduct;
                        onAddShoppingCart(selectableProduct.actId, selectableProduct.productId,
                                selectableProduct.promotionType, mNextStepAfterDelete, "");
                    } else if (mNextStepAfterDelete instanceof ShoppingProductFragment.NextStepInfo) {
                        ShoppingProductFragment.NextStepInfo info = (ShoppingProductFragment.NextStepInfo) mNextStepAfterDelete;
                        onAddShoppingCart(info.actId, info.productId, info.promotionType,
                                OnCheckStatusListener.NEXT_ACTION_BACK, "");
                    }
                } else {
                    ShoppingFragment fragment = (ShoppingFragment) getFragment(Fragments.TAG_SHOPPING_FRAGMENT);
                    if (fragment != null) {
                        fragment.onSupplyCallback(callbackIntent);
                    }
                }
            } else {
                ToastUtil.show(ShopApp.getContext(), R.string.data_error);
            }
        } else if (TextUtils.equals(action, Constants.Intent.ACTION_EDIT_CONSUMPTION)) {
            LogUtil.d(TAG, "onServiceCompleted: ACTION_EDIT_CONSUMPTION");
            ShoppingActivity.Signal.CART_RELOAD = true;
            EditCartItemFragment fragment = (EditCartItemFragment) getFragment(Fragments.TAG_EDIT_CARTITEM_FRAGMENT);
            if (fragment != null) {
                fragment.onSubmitCallback(action, callbackIntent);
            }
            String jsonString = callbackIntent
                    .getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
            if (!TextUtils.isEmpty(jsonString)) {
                try {
                    JSONObject json = new JSONObject(jsonString);
                    if (!Tags.isJSONReturnedOK(json)) {
                        ToastUtil.show(this, json.optJSONObject(Tags.HEADER).optString(Tags.DESC));
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (TextUtils.equals(action, Constants.Intent.ACTION_ORDER_SUBMIT) ) {
            OrderSubmitFragment fragment = (OrderSubmitFragment) getFragment(Fragments.TAG_ORDER_SUBMIT_FRAGMENT);
            if (fragment != null) {
                fragment.onSubmitCallback(callbackIntent);
            }
            boolean result = callbackIntent.getBooleanExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            if (result == false) {
                ToastUtil.show(this, getString(R.string.order_submit_exception_send_data));
                return;
            }

            updateShoppingCount();

            String jsonString = callbackIntent.getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
            try {
                JSONObject json = new JSONObject(jsonString);
                if (!Tags.isJSONReturnedOK(json)) {
                    ToastUtil.show(this, json.optJSONObject(Tags.HEADER).optString(Tags.DESC));
                    return;
                }
                String bodyStr = json.optString(Tags.BODY);
                if (!TextUtils.isEmpty(bodyStr)) {
                    JSONObject body = new JSONObject(bodyStr);
                    if (body != null) {
                        String orderId = body.optString(Tags.OrderSubmit.SERVICENUMBER);
                        double totalPrice = body.optDouble(Tags.OrderSubmit.TOTALPRICE);
                        String orderTime = body.optString(Tags.OrderSubmit.ADDDATE);
                        Intent intent = new Intent(this, PaymentActivity.class);
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, orderId);
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_TOTAL_PRICE, String.valueOf(totalPrice));
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ADD_TIME, Utils.DateTime.formatTime(this,
                                String.valueOf(Long.parseLong(orderTime) / 1000)));
                        intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeBuyId);
                        startActivity(intent);
                        finish();
                    }
                }
                return;
            } catch (JSONException e) {
                ToastUtil.show(this, getString(R.string.order_submit_exception_send_data));
                e.printStackTrace();
                return;
            }

        } else if (TextUtils.equals(action, Constants.Intent.ACTION_ADD_SHOPPING_CART)) {
            ShopIntentService.unregisterAction(mAddCartServiceAction);
            if (mAddCartServiceAction == mAddCartIncastProductServiceAction) {
                IncastProductsFragment fragment = (IncastProductsFragment) getFragment(Fragments.TAG_INCAST_PRODUCTS_FRAGMENT);
                if (fragment != null) {
                    fragment.setAddButtonEnabled(true);
                }
                String result = callbackIntent.getStringExtra(
                        Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG);
                if (!TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_SUCCESS)) {
                    if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_FAIL)) {
                        ToastUtil.show(ShopApp.getContext(), R.string.add_shopping_cart_fail);
                    } else {
                        ToastUtil.show(ShopApp.getContext(), result);
                    }
                    return;
                }
                onBackPressed();// 退出凑单fragment
            }
            ShoppingActivity.Signal.CART_RELOAD = false;
            if (mNextStepAfterDelete != null) {
                if (mNextStepAfterDelete instanceof EditCartItemFragment.NextStepInfo) {
                    EditCartItemFragment fragment = (EditCartItemFragment) getFragment(Fragments.TAG_EDIT_CARTITEM_FRAGMENT);
                    if (fragment != null) {
                        fragment.onRefresh();
                        ShoppingActivity.Signal.CART_RELOAD = true;
                    }
                } else if (OnCheckStatusListener.NEXT_ACTION_BACK.equals(mNextStepAfterDelete)) {
                    ShoppingActivity.Signal.CART_RELOAD = true;
                    onBackPressed();
                }
            } else {
                ShoppingFragment fragment = (ShoppingFragment) getFragment(Fragments.TAG_SHOPPING_FRAGMENT);
                if (fragment != null) {
                    fragment.onSupplyCallback(callbackIntent);
                }
            }
        } else if (Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE.equals(action)) {
            OrderSubmitFragment fragment = (OrderSubmitFragment) getFragment(Fragments.TAG_ORDER_SUBMIT_FRAGMENT);
            if (fragment != null) {
                fragment.onFetchVcodeCompleted(action, callbackIntent);
            }

        }
    }

    @Override
    public void onDelShoppingCartItem(String itemId, Object nextStep, String itemIds) {
        mNextStepAfterDelete = nextStep;
        Intent intent = new Intent(this, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_DELETE_CARTITEM);
        JSONObject json = new JSONObject();
        try {
            json.put(Tags.DelCart.ITEM_ID, itemId);
            json.put(Tags.DelCart.ITEM_IDS, itemIds);
            intent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                    json.toString());
            intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeBuyId);
            startService(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddShoppingCart(String actId, String productId, String promotionType,
            Object nextStep, String itemIds) {
        mNextStepAfterDelete = nextStep;
        mAddCartServiceAction = new ShopIntentServiceAction(
                Constants.Intent.ACTION_ADD_SHOPPING_CART, this);
        ShopIntentService.registerAction(mAddCartServiceAction);
        Intent intent = new Intent(this, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_ADD_SHOPPING_CART);
        intent.putExtra(Parameters.Keys.PRODUCT_ID, productId);
        intent.putExtra(Parameters.Keys.ITEM_IDS, itemIds);
        intent.putExtra(Parameters.Keys.CONSUMPTION, "1");
        intent.putExtra(Parameters.Keys.PROMOTION_ID, actId);
        intent.putExtra(Parameters.Keys.PROMOTION_TYPE, promotionType);
        startService(intent);
    }

    public void onAddPostFreeProduct(IncastProduct product) {
        mNextStepAfterDelete = null;
        // 使用mAddCartIncastProductServiceAction作为mAddCartServiceAction的值，用于与Supply类型的添加做区分
        mAddCartServiceAction = mAddCartIncastProductServiceAction = new ShopIntentServiceAction(
                Constants.Intent.ACTION_ADD_SHOPPING_CART, this);
        ShopIntentService.registerAction(mAddCartServiceAction);
        Intent intent = new Intent(this, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_ADD_SHOPPING_CART);
        intent.putExtra(Parameters.Keys.PRODUCT_ID, product.getProductId());
        intent.putExtra(Parameters.Keys.CONSUMPTION, "1");
        startService(intent);
    }

}
