
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.loader.PackedLoader;
import com.xiaomi.xms.sales.loader.PackedLoader.Result;
import com.xiaomi.xms.sales.loader.PrinterSaveLoader;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.ui.BluetoothFragment;
import com.xiaomi.xms.sales.ui.PaymentCashFragment;
import com.xiaomi.xms.sales.ui.PaymentFragment;
import com.xiaomi.xms.sales.ui.PaymentPosFragment;
import com.xiaomi.xms.sales.ui.PaymentResultFragment;
import com.xiaomi.xms.sales.ui.PaymentWebFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;

public class PaymentActivity extends BaseActivity {
    public static final String TAG_PAYMENT_INFO_FRAGMENT = "payment_info_fragment";
    public static final String TAG_WEB_PAYMENT_FRAGMENT = "web_payment_fragment";
    public static final String TAG_PAYMENT_RESULT_FRAGMENT = "payment_result_fragment";
    public static final String TAG_RECHARGE_RESULT_FRAGMENT = "recharge_result_fragment";
    public static final String TAG_BLUETOOTH_FRAGMENT = "bluetooth_fragment";
    public static final String TAG_PAYMENT_POS_FRAGMENT = "payment_pos_fragment";
    public static final String TAG_PAYMENT_CASH_FRAGMENT = "payment_cash_fragment";
    

    private static final int PACKED_INFO_LOADER = 0;
    private static final int SAVE_PRINTER = 100;
    
    private String mCurrentFragment;
    private String mResultFragmentTag;
    private boolean addToBackStack = false;
    private ShopIntentServiceAction mCancelOrderAction;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.pay_activity);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        }
        String action = getIntent().getAction();
        if (TextUtils.equals(action, Constants.Intent.ACTION_PAYMENT_DIRECT)) {
            mResultFragmentTag = TAG_RECHARGE_RESULT_FRAGMENT;
        } else {
            mResultFragmentTag = TAG_PAYMENT_RESULT_FRAGMENT;
            showFragment(TAG_PAYMENT_INFO_FRAGMENT, extras, false);
        }
        setShoppingBarEnable(false);
        mCancelOrderAction = new ShopIntentServiceAction(Constants.Intent.ACTION_ORDER_PAYMENT_SUCCESS, this);
        ShopIntentService.registerAction(mCancelOrderAction);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            showFragment(TAG_PAYMENT_INFO_FRAGMENT, intent.getExtras(), false);
        }
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_PAYMENT_INFO_FRAGMENT.equals(tag)) {
            fragment = new PaymentFragment();
            setTitle(R.string.title_online_payment);
        } else if (TAG_WEB_PAYMENT_FRAGMENT.equals(tag)) {
            fragment = new PaymentWebFragment();
            setTitle(R.string.title_online_payment);
        } else if (TAG_PAYMENT_RESULT_FRAGMENT.equals(tag)) {
            fragment = new PaymentResultFragment();
            setTitle(R.string.title_online_payment);
        } else if (TextUtils.equals(tag, TAG_BLUETOOTH_FRAGMENT)) {
            fragment = new BluetoothFragment();
            setTitle(R.string.bluetooth_connect_title);
        } else if (TextUtils.equals(tag, TAG_PAYMENT_POS_FRAGMENT)) {
            fragment = new PaymentPosFragment();
            setTitle(R.string.payment_pos_title);
        }else if(TextUtils.equals(tag, TAG_PAYMENT_CASH_FRAGMENT)){
        	fragment = new PaymentCashFragment();
            setTitle(R.string.pay_cash_title);
        }
        return fragment;
    }

  
    
    @Override
    public void showFragment(String tag, Bundle bundle, boolean addToBackStack) {
        mCurrentFragment = tag;
        super.showFragment(tag, bundle, addToBackStack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShopIntentService.unregisterAction(mCancelOrderAction);
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
     /*   if (TextUtils.equals(Constants.Intent.ACTION_ORDER_PAYMENT_SUCCESS, action)) {
            boolean result = callbackIntent.getBooleanExtra(Constants.Intent.EXTRA_RESULT, false);
            final String orderId = callbackIntent.getStringExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            if (!result) {
                String errorInfo = callbackIntent.getStringExtra(Constants.Intent.EXTRA_PAYMENT_ERROR_INFO);
                ToastUtil.show(this, errorInfo, Toast.LENGTH_LONG);
                
                String orderIds = Utils.Preference.getStringPref(ShopApp.getContext(),
                        Constants.Account.PREF_NOTIFY_SERVER_ERROR_ORDERIDS, "");
                StringBuilder sb = new StringBuilder(orderIds);
                if (!TextUtils.isEmpty(orderIds)) {
                    if (sb.indexOf(orderId) == -1) {
                        sb.append(Constants.Account.USER_NAME_SEPARATOR);
                        sb.append(orderId);
                    }
                } else {
                    sb.append(orderId);
                }
                Utils.Preference.setStringPref(ShopApp.getContext(),
                        Constants.Account.PREF_NOTIFY_SERVER_ERROR_ORDERIDS,
                        sb.toString());
            }
        }
        */
    }

    @Override
    public void onBackPressed() {
        boolean handled = false;
        if (TAG_WEB_PAYMENT_FRAGMENT.equals(mCurrentFragment)) {
            PaymentWebFragment f = (PaymentWebFragment) getSupportFragmentManager()
                    .findFragmentByTag(mCurrentFragment);
            if (f != null) {
                handled = f.handleBackPressed();
            }
        }

        if (!handled) {
            super.onBackPressed();
        }
    }

    public void showPaymentConfirmDialog(final Bundle bundle) {
        BaseAlertDialog dialog = new BaseAlertDialog(this);
        dialog.setTitle(R.string.payment_in_progress);
        dialog.setNegativeButton(R.string.payment_success, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bundle.putBoolean(Constants.Intent.EXTRA_PAYMENT_USER_CONFIRM_RESULT, true);
                showFragment(mResultFragmentTag, bundle, false);
            }
        });

        dialog.setPositiveButton(R.string.payment_fail, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bundle.putBoolean(Constants.Intent.EXTRA_PAYMENT_USER_CONFIRM_RESULT, false);
                showFragment(mResultFragmentTag, bundle, true);
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    
}
