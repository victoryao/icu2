
package com.xiaomi.xms.sales;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 执行独立的后台耗时操作
 */
public class ShopIntentService extends IntentService {
    private static final String TAG = "ShopIntentService";

    private static final CopyOnWriteArrayList<ShopIntentServiceAction> sActions =
            new CopyOnWriteArrayList<ShopIntentServiceAction>();

    public interface Listener {
        public void onServiceCompleted(String action, Intent callbackIntent);
    }

    private Handler mMainHandler;

    public ShopIntentService() {
        super(TAG);
        setIntentRedelivery(true);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 当执行耗时操作同时想得到执行完后的状态，注册这个action，在UI线程上进行提示。
     * 如果同一个Listener的不同实例进行注册，那么只会回调最后注册的实例，此时认为之前 注册的实例已经退到后台线程，没有必要再进行回调了。
     */
    public static void registerAction(ShopIntentServiceAction action) {
        if (!(action.getListener() instanceof Activity)) {
            throw new ClassCastException("Only activities can be registered to"
                    + " receive callback from " + ShopIntentService.class.getName());
        }
        if (sActions.contains(action)) {
            sActions.remove(action);
        }
        sActions.add(0, action);
    }

    /**
     * 从列表中注销action
     * 
     * @param action
     */
    public static void unregisterAction(ShopIntentServiceAction action) {
        sActions.remove(action);
    }

    /**
     * 当完成所有操作，回调ui线程处理intent
     * 
     * @param actionName
     * @param callbackIntent
     */
    private void deliverCallback(final String actionName, final Intent callbackIntent) {
        if (callbackIntent == null) {
            return;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                deliverCallbackOnUiThread(actionName, callbackIntent);
            }
        });
    }

    private void deliverCallbackOnUiThread(final String actionName, final Intent callbackIntent) {
        for (ShopIntentServiceAction action : sActions) {
            if (!TextUtils.equals(action.getAction(), actionName)) {
                continue;
            }
            action.getListener().onServiceCompleted(action.getAction(), callbackIntent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (Constants.Intent.ACTION_ORDER_SUBMIT.equals(action)) {
            onActionOrderSubmit(intent);
        } else if (Constants.Intent.ACTION_XIANHUO_ORDER_SUBMIT.equals(action)) {
        	onActionXianhuoOrderSubmit(intent);
        }  else if (Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT.equals(action)) {
            updateShoppingCount(intent);
        } else if (Constants.Intent.ACTION_DEL_ADDRESS.equals(action)) {
            delAddressInfo(intent);
        } else if (Constants.Intent.ACTION_DELETE_CARTITEM.equals(action)) {
            onActionDeleteCartItem(intent);
        } else if (Constants.Intent.ACTION_EDIT_CONSUMPTION.equals(action)) {
            onActionEditConsumption(intent);
        } else if (Constants.Intent.ACTION_ADD_ADDRESS.equals(action)) {
            addAddress(intent);
        } else if (Constants.Intent.ACTION_EDIT_ADDRESS.equals(action)) {
            editAddress(intent);
        } else if (Constants.Intent.ACTION_ADD_SHOPPING_CART.equals(action)) {
            addShoppingCart(intent);
        } else if (Constants.Intent.ACTION_MUTI_ADD_SHOPPING_CART.equals(action)) {
            mutiAddShoppingCart(intent);
        } else if (Constants.Intent.ACTION_CHECK_ACTIVITY.equals(action)) {
            checkActivity(intent);
        } else if (Constants.Intent.ACTION_CHECK_UPDATE.equals(action)) {
            checkUpdate(intent);
        } else if (Constants.Intent.ACTION_CANCEL_ORDER.equals(action)) {
            cancelOrder(intent);
        } else if (Constants.Intent.ACTION_FETCH_VCODE.equals(action)) {
            fetchVcode(intent);
        } else if (Constants.Intent.ACTION_VERIFY_VCODE.equals(action)) {
            verifyVcode(intent);
        } else if (Constants.Intent.ACTION_VERIFY_FCODE.equals(action)) {
            verifyFcode(intent);
        } else if (Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE.equals(action)) {
            fetchDefenseHackerVcode(intent);
        } else if (Constants.Intent.ACTION_UPDATE_MIHOME_SHOPPING_COUNT.equals(action)) {
            updateMihomeShoppingCount(intent);
        } else if (Constants.Intent.ACTION_ADD_PRODUCT_BY_NFC.equals(action)) {
            addProductByNfc(intent);
        } else if (Constants.Intent.ACTION_WRITE_PRODUCT_TO_NFC.equals(action)) {
            writeProducttoNfc(intent);
        } else if (Constants.Intent.ACTION_ORDER_PAYMENT_SUCCESS.equals(action)) {
            orderPaymentSuccess(intent);
        } else if (Constants.Intent.ACTION_ORDER_REFUND.equals(action)) {
            refundOrder(intent);
        }
    }

    private void verifyFcode(Intent intent) {
        String fcode = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_FCODE);
        Request request = new Request(HostManager.getCheckFcode());
        request.addParam(HostManager.Parameters.Keys.FCODE, fcode);

        Intent callbackIntent = new Intent();
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                if (Tags.isJSONResultOK(json)) {
                    JSONObject dataJson = json.optJSONObject(Tags.DATA);
                    if (dataJson != null) {
                        JSONArray ids = dataJson.optJSONArray(Tags.CheckCode.GOODS);
                        if (ids != null && ids.length() > 0) {
                            callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_RESULT, true);
                            String id = ids.optString(0);
                            callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_PRODUCTID, id);
                            if (ids.length() > 0) {
                                String jsonList = dataJson.optString(Tags.CheckCode.LIST);
                                callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_LISTSTR,
                                        jsonList);
                            }
                        }
                    }
                } else {
                    callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_MESSAGE,
                            json.optString(Tags.DESCRIPTION));
                }
            }
        }
        deliverCallback(Constants.Intent.ACTION_VERIFY_FCODE, callbackIntent);
    }

    private void verifyVcode(Intent intent) {
        Request request = new Request(HostManager.getCheckVCode());
        request.addParam(HostManager.Parameters.Keys.CHECKCODE_TYPE,
                HostManager.Parameters.Values.CHECKCODE_TYPE_CHECK);
        String code = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_VCODE);
        request.addParam(HostManager.Parameters.Keys.CHECKCODE_CODE, code);

        Intent callbackIntent = new Intent();
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                if (Tags.isJSONResultOK(json)) {
                    callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_RESULT, true);
                }
            }
        }
        deliverCallback(Constants.Intent.ACTION_VERIFY_VCODE, callbackIntent);
    }

    private void fetchVcode(Intent intent) {
        Request request = new Request(HostManager.getCheckVCode());
        request.addParam(HostManager.Parameters.Keys.CHECKCODE_TYPE,
                HostManager.Parameters.Values.CHECKCODE_TYPE_GET);

        Intent callbackIntent = new Intent();
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                JSONObject data = json.optJSONObject(Tags.DATA);
                if (data != null) {
                    String url = data.optString(Tags.CheckCode.URL);
                    if (url != null) {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_URL, url);
                    }
                }
            }
        }
        deliverCallback(Constants.Intent.ACTION_FETCH_VCODE, callbackIntent);
    }

    private void cancelOrder(Intent intent) {
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        Intent callbackIntent = new Intent();
        try {
            jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
            jsonObject.put("serviceNumber", intent.getStringExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID));
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CANCELSALESORDER, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
                if (request.getStatus() == Request.STATUS_OK) {
                    JSONObject json = request.requestJSON();
                    if (json != null) {
                        if (Tags.isJSONReturnedOK(json)) {
                            callbackIntent.putExtra(Constants.Intent.EXTRA_RESULT, true);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deliverCallback(Constants.Intent.ACTION_CANCEL_ORDER, callbackIntent);
    }

    
    private void orderPaymentSuccess(Intent intent) {
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        Intent callbackIntent = new Intent();
        try {
            jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
            jsonObject.put(Tags.XMSAPI.ORGID,
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
            String ordeId = intent.getStringExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            jsonObject.put("serviceNumber", ordeId);
            jsonObject.put("info", intent.getStringExtra(Constants.Intent.EXTRA_PAYMENT_POS_SUCCESS_INFO));
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_SAVEPAYINFO, jsonObject);
            callbackIntent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, ordeId);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
                if (request.getStatus() == Request.STATUS_OK) {
                    JSONObject json = request.requestJSON();
                    if (json != null) {
                        LogUtil.i(TAG, json.toString());
                        if (Tags.isJSONReturnedOK(json)) {
                            callbackIntent.putExtra(Constants.Intent.EXTRA_RESULT, true);
                        } else {
                            callbackIntent.putExtra(
                                    Constants.Intent.EXTRA_PAYMENT_ERROR_INFO,
                                    getString(R.string.payment_server_error_info1, json.optJSONObject("header")
                                            .optString("desc")));
                        }
                    }
                } else {
                    callbackIntent.putExtra(Constants.Intent.EXTRA_PAYMENT_ERROR_INFO,
                            getString(R.string.payment_server_error_info2));
                    Utils.Preference.setStringPref(ShopApp.getContext(),
                            Constants.Account.PREF_NOTIFY_SERVER_ERROR_ORDERIDS,
                            ordeId);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deliverCallback(Constants.Intent.ACTION_ORDER_PAYMENT_SUCCESS, callbackIntent);
    }

    private void refundOrder(Intent intent) {
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        Intent callbackIntent = new Intent();
        try {
            jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
            jsonObject.put(Tags.XMSAPI.ORGID,
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
            jsonObject.put("serviceNumber", intent.getStringExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID));
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_REFUNDORDERO, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
                if (request.getStatus() == Request.STATUS_OK) {
                    JSONObject json = request.requestJSON();
                    LogUtil.i(TAG, json.toString());
                    if (json != null) {
                        if (Tags.isJSONReturnedOK(json)) {
                            callbackIntent.putExtra(Constants.Intent.EXTRA_RESULT, true);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deliverCallback(Constants.Intent.ACTION_ORDER_REFUND, callbackIntent);
    }

    private void checkUpdate(Intent intent) {
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        Intent callbackIntent = new Intent();
        try {
            jsonObject.put(HostManager.Parameters.Keys.PACKAGE, Device.PACKAGE);
            jsonObject.put(HostManager.Parameters.Keys.VERSION, Device.SHOP_VERSION);
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CHECKUPDATE, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
                if (request.getStatus() == Request.STATUS_OK) {
                    JSONObject json = request.requestJSON();
                    if (json != null) {
                        if (Tags.isJSONReturnedOK(json) && needUpdate(json)) {
                            String bodyStr = json.optString(Tags.BODY);
                            if (!TextUtils.isEmpty(bodyStr)) {
                                JSONObject body = new JSONObject(bodyStr);
                                if (body != null) {
                                    JSONObject dataJson = body.optJSONObject("data");
                                    String url = dataJson.optString(Tags.VersionUpdate.UPDATE_URL, null);
                                    String versionName = dataJson.optString(Tags.VersionUpdate.VERSIONNAME, null);
                                    String updateInfo = dataJson.optString(Tags.VersionUpdate.UPDATEINFO, null);
                                    callbackIntent.putExtra(Constants.Intent.EXTRA_UPDATE_URL, url);
                                    callbackIntent.putExtra(Constants.Intent.EXTRA_UPDATE_VERSION_NAME, versionName);
                                    callbackIntent.putExtra(Constants.Intent.EXTRA_UPDATE_SUMMARY, updateInfo);
                                }
                            }
                        } else {
                            Utils.Preference.setLongPref(this, Constants.AppUpdate.PREF_LAST_UPDATE_IS_OK,
                                    System.currentTimeMillis());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            deliverCallback(Constants.Intent.ACTION_CHECK_UPDATE, callbackIntent);
            LogUtil.e(TAG, "construct check-update request data err.");
            return;
        }
        deliverCallback(Constants.Intent.ACTION_CHECK_UPDATE, callbackIntent);
    }

    private boolean needUpdate(JSONObject json) {
        boolean result = false;
        try {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                if (body != null) {
                    JSONObject data = body.optJSONObject("data");
                    result = data.optBoolean(Tags.VersionUpdate.NEED_UPDATE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void addAddress(Intent intent) {
        String consignee = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE);
        int province = intent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_PROVINCE, 0);
        int city = intent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_CITY, 0);
        int district = intent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_DISTRICT, 0);
        String location = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_LOCATION);
        String zipcode = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_ZIPCODE);
        String tel = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_TEL);

        Request request = new Request(HostManager.getAddAddressInfo());
        request.addParam(HostManager.Parameters.Keys.ADDRESS_CONSIGNEE, consignee);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_PROVINCE, Integer.toString(province));
        request.addParam(HostManager.Parameters.Keys.ADDRESS_CITY, Integer.toString(city));
        request.addParam(HostManager.Parameters.Keys.ADDRESS_DISTRICT, Integer.toString(district));
        request.addParam(HostManager.Parameters.Keys.ADDRESS_LOCATION, location);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_ZIPCODE, zipcode);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_TEL, tel);

        int status = request.getStatus();
        LogUtil.d(TAG, "response status:" + status);

        Intent callbackIntent = new Intent();
        if (status == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            LogUtil.d(TAG, "response:" + json);
            if (json != null) {
                JSONObject dataJson = json.optJSONObject(Tags.DATA);
                if (dataJson != null) {
                    String newAddressId = dataJson.optString(Tags.RESULT);
                    if (!TextUtils.isEmpty(newAddressId)) {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, status);
                        callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_NEWID, newAddressId);
                        deliverCallback(Constants.Intent.ACTION_ADD_ADDRESS, callbackIntent);
                        LogUtil.d(TAG, "deliver callback.");
                        return;
                    }
                } else {
                    String error = json.optString(Tags.DESCRIPTION);
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT_MSG, error);
                    int errorId = json.optInt(Tags.CODE);
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT_CODE, errorId);
                }
            }
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT,
                    Request.STATUS_UNKNOWN_ERROR);
        } else {
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, status);
        }
        deliverCallback(Constants.Intent.ACTION_ADD_ADDRESS, callbackIntent);
    }

    private void editAddress(Intent intent) {
        String addressId = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_ID);
        String consignee = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE);
        int province = intent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_PROVINCE, 0);
        int city = intent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_CITY, 0);
        int district = intent.getIntExtra(Constants.Intent.EXTRA_ADDRESS_DISTRICT, 0);
        String location = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_LOCATION);
        String zipcode = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_ZIPCODE);
        String tel = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_TEL);

        Request request = new Request(HostManager.getEditAddressInfo());
        request.addParam(HostManager.Parameters.Keys.ADDRESS_ID, addressId);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_CONSIGNEE, consignee);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_PROVINCE, Integer.toString(province));
        request.addParam(HostManager.Parameters.Keys.ADDRESS_CITY, Integer.toString(city));
        request.addParam(HostManager.Parameters.Keys.ADDRESS_DISTRICT, Integer.toString(district));
        request.addParam(HostManager.Parameters.Keys.ADDRESS_LOCATION, location);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_ZIPCODE, zipcode);
        request.addParam(HostManager.Parameters.Keys.ADDRESS_TEL, tel);

        int status = request.getStatus();
        LogUtil.d(TAG, "response status:" + status);

        Intent callbackIntent = new Intent();
        if (status == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            LogUtil.d(TAG, "response:" + json);
            if (json != null) {
                JSONObject dataJson = json.optJSONObject(Tags.DATA);
                if (dataJson != null) {
                    if (dataJson.optInt(Tags.RESULT) > 0) {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, status);
                        deliverCallback(Constants.Intent.ACTION_EDIT_ADDRESS, callbackIntent);
                        LogUtil.d(TAG, "deliver callback.");
                        return;
                    }
                } else {
                    String error = json.optString(Tags.DESCRIPTION);
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT_MSG, error);
                    int errorId = json.optInt(Tags.CODE);
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT_CODE, errorId);
                }
            }
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT,
                    Request.STATUS_UNKNOWN_ERROR);
        } else {
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, status);
        }
        deliverCallback(Constants.Intent.ACTION_EDIT_ADDRESS, callbackIntent);
    }

    /**
     * 执行删除个人收件地址的动作
     * 
     * @param requestIntent
     */
    private void delAddressInfo(Intent intent) {
        String addressId = intent.getStringExtra(Constants.Intent.EXTRA_ADDRESS_ID);
        Request request = new Request(HostManager.getDelAddressInfo());
        Intent callbackIntent = new Intent();

        request.addParam(HostManager.Parameters.Keys.ADDRESS_ID, addressId);
        int status = request.getStatus();
        LogUtil.d(TAG, "response status:" + status);

        if (status == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            LogUtil.d(TAG, "response:" + json);
            if (json != null) {
                JSONObject dataJson = json.optJSONObject(Tags.DATA);
                if (dataJson != null) {
                    if (dataJson.optBoolean(Tags.RESULT)) {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, status);
                        deliverCallback(Constants.Intent.ACTION_DEL_ADDRESS, callbackIntent);
                        LogUtil.d(TAG, "deliver callback.");
                        return;
                    }
                }
            }
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT,
                    Request.STATUS_UNKNOWN_ERROR);
        } else {
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_RESULT, status);
        }
        deliverCallback(Constants.Intent.ACTION_DEL_ADDRESS, callbackIntent);
    }

    private void onActionDeleteCartItem(Intent requestIntent) {
        Intent callbackIntent = new Intent();
        String jsonString = requestIntent
                .getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject json = new JSONObject(jsonString);
            jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("goodsId", json.optString(Tags.EditConsumption.ITEM_ID));
            jsonObject.put("itemIds", json.optString(Tags.EditConsumption.ITEM_IDS));
            jsonObject.put("type", "2");
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CARTMANAGEMENT, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
            if (request.getStatus() != Request.STATUS_OK) {
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        false);
            } else {
                JSONObject responseJson = request.requestJSON();
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        true);
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                        responseJson.toString());
            }
        } catch (JSONException e) {
            callbackIntent
                    .putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            e.printStackTrace();
        }
        deliverCallback(Constants.Intent.ACTION_DELETE_CARTITEM, callbackIntent);
    }

    /**
     * 执行checkout submit动作 TODO 后续还需要处理
     * 
     * @param requestIntent
     */
    private void onActionOrderSubmit(Intent requestIntent) {
        Intent callbackIntent = new Intent();
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
        	jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("orgId",
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
            jsonObject.put("serviceNumber", requestIntent.getStringExtra(Constants.serviceNumber));
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CREATESALESORDE, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
            if (request.getStatus() != Request.STATUS_OK) {
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        false);
            } else {
                JSONObject responseJson = request.requestJSON();
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        true);
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                        responseJson.toString());
            }
           
        } catch (JSONException e) {
            callbackIntent
                    .putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            e.printStackTrace();
        }

        deliverCallback(Constants.Intent.ACTION_ORDER_SUBMIT, callbackIntent);
    }
    
    /**
     * 现货销售的提交
     * @param requestIntent
     */
    private void onActionXianhuoOrderSubmit(Intent requestIntent) {
        Intent callbackIntent = new Intent();
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
        	jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("orgId",
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
            jsonObject.put("reducePrice", 0);
            String serviceNumber = requestIntent.getStringExtra(Constants.serviceNumber);
            int orderType = Constants.xianhuo_orderType;
           
            jsonObject.put("serviceNumber", serviceNumber);
            jsonObject.put("orderType", orderType);
            
        	StringBuffer snList = new StringBuffer();   //sn list  
        	JSONObject pList = new JSONObject();
        	SharedPreferences pmySharedPreferences = getSharedPreferences(Constants.productCache, Activity.MODE_PRIVATE);
        	Iterator<String> it = null;
        	if(pmySharedPreferences != null && pmySharedPreferences.getAll() != null && pmySharedPreferences.getAll().size() > 0){
        		 it = pmySharedPreferences.getAll().keySet().iterator();
        	}
        	if(it != null){
        		while(it.hasNext()){
            		String sn = it.next();
            		snList.append(sn+",");
            		String pproductBase64 = pmySharedPreferences.getString(sn, "");
                    if(pproductBase64 != null && pproductBase64.length() > 0){
        	       	     byte[] base64Bytes = Base64.decode(pproductBase64.getBytes(),Base64.DEFAULT);
        	       	     ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
        	       	     ObjectInputStream ois = null;
        	       	    
        	   			try {
        	   				ois = new ObjectInputStream(bais);
        	   			} catch (StreamCorruptedException e) {
        	   				e.printStackTrace();
        	   			} catch (IOException e) {
        	   				e.printStackTrace();
        	   			}
        	   			 // 从ObjectInputStream中读取Product对象
        	      	       try {
        	      	    	
 								ProductInfo p = (ProductInfo) ois.readObject();
               	      	    	JSONObject pp = new JSONObject();
               	      	    	pp.put("productId", p.getProductId());
               	      	    	pp.put("productName", p.getProductName());
               	      	    	pp.put("price", p.getProductPrice());
               	      	    	//pp.put("marketPrice", p.getMarketPrice());
               	      	    	pp.put("sku", p.getContainId());
               	      	    	pp.put("pid", p.getPid());
               	      	    	pp.put("styleName", p.getStyleName());
               	      	    	pp.put("imageUrl", p.getImage().getFileUrl());
               	      	    	pp.put("isBatched", String.valueOf(p.isIsBatched()));
               	      	    	
               	      	    	pList.put(sn, pp);
        	      	    	
        	      	    	 
        	   			} catch (OptionalDataException e1) {
        	   				e1.printStackTrace();
        	   			} catch (ClassNotFoundException e1) {
        	   				e1.printStackTrace();
        	   			} catch (IOException e1) {
        	   				e1.printStackTrace();
        	   			}
                    }
            	}
        	}
        	
        	String snlist = snList.toString();
        	jsonObject.put("snList", snlist.substring(0, snlist.length()-1));
        	jsonObject.put("pList", pList);
            
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CREATESALESORDE, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
            if (request.getStatus() != Request.STATUS_OK) {
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,false);
            } else {
                JSONObject responseJson = request.requestJSON();
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,true);
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,responseJson.toString());
            }
           
        } catch (JSONException e) {
            callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            e.printStackTrace();
        }
      
        deliverCallback(Constants.Intent.ACTION_XIANHUO_ORDER_SUBMIT, callbackIntent);
    }
    

    private void onActionEditConsumption(Intent requestIntent) {
        Intent callbackIntent = new Intent();
        String jsonString = requestIntent
                .getStringExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject json = new JSONObject(jsonString);
            jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("goodsId", json.optString(Tags.EditConsumption.ITEM_ID));
            jsonObject.put("itemIds", json.optString(Tags.EditConsumption.ITEM_IDS));
            jsonObject.put("type", "1");
            jsonObject.put("consumption", json.optString(Tags.EditConsumption.CONSUMPTION));
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CARTMANAGEMENT, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
            if (request.getStatus() != Request.STATUS_OK) {
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        false);
            } else {
                JSONObject responseJson = request.requestJSON();
                if (responseJson != null) {
                    LogUtil.d(TAG, responseJson.toString());
                    if (Tags.isJSONReturnedOK(responseJson)) {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                                true);
                    } else {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                                false);
                    }
                    callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                            responseJson.toString());
                } else {
                    callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                            false);
                }
            }
        } catch (JSONException e) {
            callbackIntent
                    .putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            e.printStackTrace();
        }
        deliverCallback(Constants.Intent.ACTION_EDIT_CONSUMPTION, callbackIntent);
    }

    /**
     * 更新购物车商品数量
     */
    private void updateShoppingCount(Intent intent) {
        int count = Constants.UNINITIALIZED_NUM;
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
            jsonObject.put("orgId",
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETSALESCARTLIST, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                LogUtil.d(TAG, json.toString());
                if (Tags.isJSONReturnedOK(json)) {
                    String bodyStr = json.optString(Tags.BODY);
                    if (!TextUtils.isEmpty(bodyStr)) {
                        try {
                            JSONObject body = new JSONObject(bodyStr);
                            if (body != null) {
                                count = body.optInt(Tags.ShoppingCartList.COUNT);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        intent.putExtra(Constants.Intent.EXTRA_SHOPPING_COUNT, count);
        deliverCallback(intent.getAction(), intent);
    }

    /**
     * 更新小米之家购物车商品数量
     */
    private void updateMihomeShoppingCount(Intent intent) {
        int count = Constants.UNINITIALIZED_NUM;
        String mihomeId = intent.getStringExtra(Constants.Intent.EXTRA_MIHOME_BUY);
        Request request = new Request(HostManager.getShoppingCount());
        request.addParam(Parameters.Keys.MIHOMEBUY_ID, mihomeId);
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (Tags.isJSONResultOK(json)) {
                JSONObject dataJson = json.optJSONObject(Tags.DATA);
                if (dataJson != null) {
                    count = dataJson.optInt(Tags.RESULT);
                }
            }
        }

        intent.putExtra(Constants.Intent.EXTRA_MIHOME_SHOPPING_COUNT, count);
        deliverCallback(intent.getAction(), intent);
    }

    private void addShoppingCart(Intent intent) {
        Intent callbackIntent = new Intent();
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("goodsId", intent.getStringExtra(Parameters.Keys.PRODUCT_ID));
            jsonObject.put("itemIds", intent.getStringExtra(Parameters.Keys.ITEM_IDS));
            jsonObject.put("type", "0");
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_CARTMANAGEMENT, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                LogUtil.d(TAG, json.toString());
                if (Tags.isJSONReturnedOK(json)) {
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG,
                            Constants.AddShoppingCartStatus.ADD_SUCCESS);
                } else {
                    LogUtil.d(TAG, json.optJSONObject(Tags.HEADER).optString(Tags.DESC));
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG,
                            json.optJSONObject(Tags.HEADER).optString(Tags.DESC));
                }
            }
        } else {
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG,
                    Constants.AddShoppingCartStatus.ADD_FAIL);
        }
        deliverCallback(Constants.Intent.ACTION_ADD_SHOPPING_CART, callbackIntent);
    }

    private void mutiAddShoppingCart(Intent intent) {
        Intent callbackIntent = new Intent();
        Request request = new Request(HostManager.getMutiAddShopping());
        LogUtil.d(TAG, Parameters.Keys.PRODUCT);
        LogUtil.d(TAG, intent.getStringExtra(Parameters.Keys.PRODUCT));
        request.addParam(Parameters.Keys.PRODUCT, intent.getStringExtra(Parameters.Keys.PRODUCT));
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                LogUtil.d(TAG, json.toString());
                JSONObject dataJson = json.optJSONObject(Tags.DATA);
                if (dataJson != null && TextUtils.equals(dataJson.optString(Tags.RESULT), "true")) {
                    LogUtil.d(TAG, dataJson.toString());
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG,
                            Constants.AddShoppingCartStatus.ADD_SUCCESS);
                } else if (dataJson == null
                        && TextUtils.equals(json.optString(Tags.RESULT), "error")) {
                    LogUtil.d(TAG, json.optString(Tags.DESCRIPTION));
                    callbackIntent.putExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG,
                            json.optString(Tags.DESCRIPTION));
                }
            }
        } else {
            callbackIntent.putExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG,
                    Constants.AddShoppingCartStatus.ADD_FAIL);
        }
        deliverCallback(Constants.Intent.ACTION_ADD_SHOPPING_CART, callbackIntent);
    }

    private void checkActivity(Intent intent) {
        Request request = new Request(HostManager.getActivity());
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                LogUtil.d(TAG, json.toString());
                boolean status = json.optBoolean(Tags.Activity.STATUS);
                if (status) {
                    String url = json.optString(Tags.Activity.URL);
                    String version = json.optString(Tags.Activity.VERSION);
                    String type = json.optString(Tags.Activity.TYPE);
                    if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(version)) {
                        intent.putExtra(Constants.Intent.EXTRA_ACTIVITY_URL, url);
                        intent.putExtra(Constants.Intent.EXTRA_ACTIVITY_VERSION, version);
                        intent.putExtra(Constants.Intent.EXTRA_ACTIVITY_TYPE, type);
                    }
                }
            }
        }
        deliverCallback(intent.getAction(), intent);
    }

    private void fetchDefenseHackerVcode(Intent intent) {
        Request request = new Request(HostManager.getDefenseCheckCode());

        Intent callbackIntent = new Intent();
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            if (json != null) {
                JSONObject data = json.optJSONObject(Tags.DATA);
                if (data != null) {
                    String url = data.optString(Tags.CheckCode.URL);
                    if (url != null) {
                        callbackIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_URL, url);
                    }
                }
            }
        }
        deliverCallback(Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE, callbackIntent);
    }

    private void addProductByNfc(Intent intent) {

        Intent callbackIntent = new Intent();
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tagId", intent.getStringExtra(Constants.Intent.EXTRA_NFC_TAG_ID));
            jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("orgId",
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_ADDPRODUCTBYNFC, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
            if (request.getStatus() != Request.STATUS_OK) {
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        false);
            } else {
                JSONObject responseJson = request.requestJSON();
                LogUtil.i(TAG, responseJson.toString());
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        true);
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                        responseJson.toString());
            }
        } catch (JSONException e) {
            callbackIntent
                    .putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            e.printStackTrace();
        }

        deliverCallback(Constants.Intent.ACTION_ADD_PRODUCT_BY_NFC, callbackIntent);

    }

    private void writeProducttoNfc(Intent intent) {

        Intent callbackIntent = new Intent();
        Request request = new Request(HostManager.URL_XMS_SALE_API);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tagId", intent.getStringExtra(Constants.Intent.EXTRA_NFC_TAG_ID));
            jsonObject.put("sku", intent.getStringExtra(Constants.Intent.EXTRA_PRODUCT_ID));
            jsonObject.put("userId", LoginManager.getInstance().getUserId());
            jsonObject.put("orgId",
                    Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
            jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_WRITEPRODUCTTONFC, jsonObject);
            if (!TextUtils.isEmpty(data)) {
                request.addParam(Tags.RequestKey.DATA, data);
            }
            if (request.getStatus() != Request.STATUS_OK) {
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        false);
            } else {
                JSONObject responseJson = request.requestJSON();
                LogUtil.i(TAG, responseJson.toString());
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT,
                        true);
                callbackIntent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                        responseJson.toString());
            }
        } catch (JSONException e) {
            callbackIntent
                    .putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_ACTION_RESULT, false);
            e.printStackTrace();
        }

        deliverCallback(Constants.Intent.ACTION_WRITE_PRODUCT_TO_NFC, callbackIntent);

    }

}
