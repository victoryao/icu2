
package com.xiaomi.xms.sales.activity;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.ShoppingFragment.OnCheckStatusListener;
import com.xiaomi.xms.sales.ui.XianhuoOrderSubmitFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;

public class XianhuoOrderActivity extends BaseActivity implements OnCheckStatusListener{
    public static final String TAG = "XianhuoOrderActivity";
    public static final String TAG_Order_DETAILS = "tag_order_details";
    private ShopIntentServiceAction mXianhuoOrderSubmitAction;
    private XianhuoOrderSubmitFragment xianhuoOrderSubmitFragment;
    private String mMihomeBuyId = null;
    private LinearLayout cart;
    public static class Fragments {
        public static final String TAG_XIANHUO_ORDER_SUBMIT_FRAGMENT = "xianhuo_order_submit_fragment";
        public static final String TAG_XIANHUO_EMPTY_FRAGMENT ="xianhuo_empty_fragment";
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.product_details_activity);
        Bundle bundle = getIntent().getExtras();
        cart = (LinearLayout)findViewById(R.id.title_right_bar);
	    cart.setVisibility(View.GONE);
        if (bundle != null)
            mMihomeBuyId = bundle.getString(Constants.Intent.EXTRA_MIHOME_BUY);
        if (mMihomeBuyId == null)
            mMihomeBuyId = HostManager.Parameters.Values.MIHOME_BUY_NULL;
       
        showFragment(Fragments.TAG_XIANHUO_ORDER_SUBMIT_FRAGMENT, bundle, false);
        
        setShoppingBarEnable(false);
    }


	@Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (Fragments.TAG_XIANHUO_ORDER_SUBMIT_FRAGMENT.equals(tag)) {
        	xianhuoOrderSubmitFragment = new XianhuoOrderSubmitFragment();
        	fragment = xianhuoOrderSubmitFragment;
        }
       
        return fragment;
    }

    public BaseFragment getFragment(String tag) {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }
    
    @Override
    protected void onStart() {
        mXianhuoOrderSubmitAction = new ShopIntentServiceAction(Constants.Intent.ACTION_XIANHUO_ORDER_SUBMIT, this);
        ShopIntentService.registerAction(mXianhuoOrderSubmitAction);
       
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //ShopIntentService.unregisterAction(mXianhuoOrderSubmitAction);
    }
    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (TextUtils.equals(action, Constants.Intent.ACTION_XIANHUO_ORDER_SUBMIT) ) {
        	//ShopIntentService.unregisterAction(mXianhuoOrderSubmitAction);
        	XianhuoOrderSubmitFragment fragment = (XianhuoOrderSubmitFragment) getFragment(Fragments.TAG_XIANHUO_ORDER_SUBMIT_FRAGMENT);
            if (fragment != null) {
                fragment.onSubmitCallback(callbackIntent);
            }
            boolean result = callbackIntent.getBooleanExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            if (result == false) {
                ToastUtil.show(this, getString(R.string.order_submit_exception_send_data));
                return;
            }

            //updateShoppingCount();
            String jsonString = callbackIntent.getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
            try {
                JSONObject json = new JSONObject(jsonString);
                if (!Tags.isJSONReturnedOK(json)) {
                	JSONObject header = json.optJSONObject(Tags.HEADER);
                	if(header != null && header.optString(Tags.DESC) != null ){
                		ToastUtil.show(this, header.optString(Tags.DESC));
                	}
                    return;
                }
                String bodyStr = json.optString(Tags.BODY);
                if (!TextUtils.isEmpty(bodyStr)) {
                    JSONObject body = new JSONObject(bodyStr);
                    if (body != null) {
                    	 //跳转到支付界面前清空商品
                        
                    	SharedPreferences psp = getSharedPreferences(Constants.productCache, Activity.MODE_PRIVATE);
                    	Editor pEditor = psp.edit();
                    	pEditor.clear();
                    	pEditor.commit();
                    	
                    	//清空订单号信息
                    	SharedPreferences numberCache = getSharedPreferences(Constants.serviceNumberCache, Context.MODE_PRIVATE);
                    	Editor e = numberCache.edit();
                    	e.clear();
                    	e.commit();
                    	
                        String orderId = body.optString(Tags.OrderSubmit.SERVICENUMBER);
                        double totalPrice = body.optDouble(Tags.OrderSubmit.TOTALPRICE);
                        String orderTime = body.optString(Tags.OrderSubmit.ADDDATE);
                        Intent intent = new Intent(this, PaymentActivity.class);
                        intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE,Constants.xianhuo_orderType);  //现货销售的
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, orderId);
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_TOTAL_PRICE, String.valueOf(totalPrice));
                        if(orderTime != null && orderTime.length() > 0){
                        	intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ADD_TIME, Utils.DateTime.formatTime(this,
                                    String.valueOf(Long.parseLong(orderTime) / 1000)));
                        }
                        else{
                        	intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ADD_TIME, Utils.DateTime.formatTime(this,
                                    String.valueOf(new Date().getTime() / 1000)));
                        }
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
        } 
    }


	@Override
	public void onDelShoppingCartItem(String item, Object nextStep,
			String itemIds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddShoppingCart(String actId, String productId,
			String promotionType, Object nextStep, String itemIds) {
		// TODO Auto-generated method stub
		
	}

}
