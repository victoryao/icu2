
package com.xiaomi.xms.sales.nfc;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

public class NfcActivity extends BaseActivity {
    public final static String TAG = "NfcActivity";
    private String mAction;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private ShopIntentServiceAction mAddProductByNfcAction;
    private ShopIntentServiceAction mWriteProductToNfcAction;
    private ProgressDialog mProgressDialog;
    private Bundle mBundle;
    private String mSku;
    private boolean mEnterType;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LogUtil.i(TAG, "onCreate");
        if (!LoginManager.getInstance().hasLogin()) {
            gotoAccount();
            finish();
            return;
        }
        setCustomContentView(R.layout.nfc_activity);
        setTitle(R.string.nfc_title);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            ToastUtil.show(this, getResources().getString(R.string.no_nfc));
            finish();
            return;
        }
        initNfc();
        handleIntent();
        mAddProductByNfcAction = new ShopIntentServiceAction(Constants.Intent.ACTION_ADD_PRODUCT_BY_NFC, this);
        ShopIntentService.registerAction(mAddProductByNfcAction);
        mWriteProductToNfcAction = new ShopIntentServiceAction(Constants.Intent.ACTION_WRITE_PRODUCT_TO_NFC, this);
        ShopIntentService.registerAction(mWriteProductToNfcAction);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        resolveIntent(getIntent());
    }

    private void initNfc() {
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter techNdef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        techNdef.addCategory("*/*");
        // 过滤器
        mFilters = new IntentFilter[] {
                techNdef
        };
        // 允许扫描的标签类型
        mTechLists = new String[][] {
                // Touchatag tag
                new String[] {
                        MifareUltralight.class.getName(),
                        NfcA.class.getName(),
                        Ndef.class.getName()
                },
                // DESFire tag
                new String[] {
                        MifareClassic.class.getName(),
                        NfcA.class.getName(),
                        IsoDep.class.getName()
                },
                // Any Tag
                new String[] {
                        NfcA.class.getName()
                }
        };
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mBundle = intent.getExtras();
        if (mBundle != null) {
            mSku = mBundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
            mEnterType = mBundle.getBoolean(Constants.Intent.EXTRA_ENTER_NFC_TYPE, false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i(TAG, "onPause");
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume");
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                    mTechLists);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(TAG, "onNewIntent");
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (TextUtils.equals(Constants.Intent.ACTION_ADD_PRODUCT_BY_NFC, action)) {
            mProgressDialog.dismiss();
            boolean result = callbackIntent.getBooleanExtra(
                    Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            if (result == false) {
                ToastUtil.show(this, getString(R.string.order_submit_exception_send_data));
                return;
            }

            String jsonString = callbackIntent
                    .getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
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
                        String goodsId = body.optString("goods_id");
                        String pId = body.optString("p_id");
                        Intent intent = new Intent();
                        intent.setClass(this, ProductDetailsActivity.class);
                        if (goodsId.startsWith("1")) {
                            // 套餐类型商品
                            intent.putExtra(Constants.Intent.EXTRA_CONTAIN_ID, goodsId);
                        }
                        intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, goodsId);
                        intent.putExtra(Constants.Intent.EXTRA_P_ID, pId);
                        startActivity(intent);
                        if (!mEnterType) {
                            finish();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (TextUtils.equals(Constants.Intent.ACTION_WRITE_PRODUCT_TO_NFC, action)) {
            mProgressDialog.dismiss();
            boolean result = callbackIntent.getBooleanExtra(
                    Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            if (result == false) {
                ToastUtil.show(this, getString(R.string.order_submit_exception_send_data));
                return;
            }
            String jsonString = callbackIntent
                    .getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
            try {
                JSONObject json = new JSONObject(jsonString);
                if (!Tags.isJSONReturnedOK(json)) {
                    ToastUtil.show(this, json.optJSONObject(Tags.HEADER).optString(Tags.DESC));
                    return;
                } else {
                    ToastUtil.show(this, getString(R.string.nfc_product_write_success_info));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void resolveIntent(Intent intent) {
        mAction = intent.getAction();
        if (!TextUtils.isEmpty(mAction)) {
            LogUtil.i(TAG, mAction);
        }
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(mAction)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(mAction)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(mAction)) {
            String tagId = processIntent(intent);
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(false);
            }
            if (!TextUtils.isEmpty(tagId) && !mProgressDialog.isShowing()) {
                LogUtil.i(TAG, tagId);
                if (TextUtils.isEmpty(mSku)) {
                    mProgressDialog.setMessage("正在读取NFC标签商品信息, 请稍等...");
                    mProgressDialog.show();
                    Intent inte = new Intent(this, ShopIntentService.class);
                    inte.setAction(Constants.Intent.ACTION_ADD_PRODUCT_BY_NFC);
                    inte.putExtra(Constants.Intent.EXTRA_NFC_TAG_ID, tagId);
                    startService(inte);
                } else {
                    mProgressDialog.setMessage("正在写入NFC标签商品信息, 请稍等...");
                    mProgressDialog.show();
                    Intent inte = new Intent(this, ShopIntentService.class);
                    inte.setAction(Constants.Intent.ACTION_WRITE_PRODUCT_TO_NFC);
                    inte.putExtra(Constants.Intent.EXTRA_NFC_TAG_ID, tagId);
                    inte.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mSku);
                    startService(inte);
                }
            }
        }
    }

    private String processIntent(Intent intent) {
        byte[] tagIdBytes = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        return getHex(tagIdBytes);
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy");
        ShopIntentService.unregisterAction(mAddProductByNfcAction);
        ShopIntentService.unregisterAction(mWriteProductToNfcAction);
    }

    private void showWirelessSettingsDialog() {
        final BaseAlertDialog dialog = new BaseAlertDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(R.string.open_nfc);
        dialog.setNegativeButton(R.string.dialog_ask_ok, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (android.os.Build.VERSION.SDK_INT >= 10) {
                    intent.setAction("android.settings.NFC_SETTINGS");
                } else {
                    intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        dialog.setPositiveButton(R.string.dialog_ask_cancel, new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.show();
    }

}
