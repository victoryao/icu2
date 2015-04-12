
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.Order.PickupInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PaymentInfoLoader extends BaseLoader<PaymentInfoLoader.Result> {

    private static final String CACHE_KEY = "Payment_mode_Info";

    private String mOrderId;

    public PaymentInfoLoader(Context context, String orderId) {
        super(context);
        mOrderId = orderId;
    }

    public static final class Result extends BaseResult {
        public Order mOrderInfo;
        public String mOrderError;
        public ArrayList<PayMode> mPayModeList;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mOrderInfo = mOrderInfo;
            newResult.mOrderError = mOrderError;
            newResult.mPayModeList = mPayModeList;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mOrderInfo == null ? 0 : 1;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new PaymentInfoUpdateTask();
    }

    private class PaymentInfoUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getPaymentMode());
            request.addParam(Parameters.Keys.ORDER_ID, mOrderId);
            return request;
        }

    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        result.mOrderInfo = parseOrderOfJson(json);
        if (result.mOrderInfo == null) {
            result.mOrderError = Order.getErrorInfo(json);
        }
        result.mPayModeList = parsePaymentModeOfJson(json);
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY + mOrderId;
    }

    public Order parseOrderOfJson(JSONObject json) {
        Order order = null;
        if (Tags.isJSONResultOK(json)) {
            JSONObject dataJson = json.optJSONObject(Tags.DATA);
            if (dataJson != null) {
                JSONObject orderJson = dataJson.optJSONObject("order");
                if (orderJson != null) {

                    // area obj
                    JSONObject provinceObj = orderJson.optJSONObject(Tags.Order.PROVINCE);
                    JSONObject cityObj = orderJson.optJSONObject(Tags.Order.CITY);
                    JSONObject districtObj = orderJson.optJSONObject(Tags.Order.DISTRICT);
                    if (provinceObj == null || cityObj == null || districtObj == null) {
                        return null;
                    }
                    String province = provinceObj.optString(Tags.Order.AREA_NAME);
                    int provinceId = provinceObj.optInt(Tags.Order.AREA_ID);
                    String city = cityObj.optString(Tags.Order.AREA_NAME);
                    int cityId = cityObj.optInt(Tags.Order.AREA_ID);
                    String district = districtObj.optString(Tags.Order.AREA_NAME);
                    int districtId = districtObj.optInt(Tags.Order.AREA_ID);

                    // pickup info
                    JSONObject pickupObject = orderJson.optJSONObject(Tags.Order.PICKUP_INFO);
                    PickupInfo pickupInfo = null;
                    if (pickupObject != null) {
                        String pickupName = pickupObject.optString(Tags.Order.PICKUP_NAME);
                        String pickupAddress = pickupObject.optString(Tags.Order.PICKUP_ADDRESS);
                        String pickupTel = pickupObject.optString(Tags.Order.PICKUP_TEL);
                        String pickupLonLat = pickupObject.optString(Tags.Order.PICKUP_LONLAT);
                        pickupInfo = new PickupInfo(pickupAddress, pickupName, pickupTel,
                                pickupLonLat);
                    }

                    // own fields
                    String consignee = orderJson.optString(Tags.Order.CONSIGNEE);
                    double fee = orderJson.optDouble(Tags.Order.FEE);
                    String orderId = orderJson.optString(Tags.Order.ID);
                    String invoiceTitle = orderJson.optString(Tags.Order.INVOICE_TITLE);
                    String deliveryTime = orderJson.optString(Tags.Order.BEST_TIME);
                    String addTime = orderJson.optString(Tags.Order.ADD_TIME);
                    String consigneePhone = orderJson.optString(Tags.Order.CONSIGNEE_PHONE);
                    String address = orderJson.optString(Tags.Order.ADDRESS);
                    int status = orderJson.optInt(Tags.Order.STATUS);
                    String zipcode = orderJson.optString(Tags.Order.ZIPCODE);
                    boolean hasPhone = orderJson.optBoolean(Tags.Order.HAS_PHONE, false);
                    order = new Order(orderId, fee, consignee, consigneePhone, address,
                            deliveryTime,
                            addTime, invoiceTitle, status, null, province, city, district,
                            zipcode, null, null, null, hasPhone, null, null);
                    order.setPickupInfo(pickupInfo);
                    order.setProvinceId(provinceId);
                    order.setCityId(cityId);
                    order.setDistrictId(districtId);
                }
            }
        }
        return order;
    }

    public ArrayList<PayMode> parsePaymentModeOfJson(JSONObject json) {
        ArrayList<PayMode> list = null;
        if (Tags.isJSONResultOK(json)) {
            JSONObject dataJson = json.optJSONObject(Tags.DATA);
            if (dataJson != null) {
                JSONArray modeArray = dataJson.optJSONArray("payments");
                list = new ArrayList<PayMode>();
                if (modeArray != null) {
                    if (!modeArray.isNull(0)) {
                        for (int i = 0; i < modeArray.length(); i++) {
                            JSONObject r = (JSONObject) modeArray.opt(i);
                            String modeKey = r.optString("key");
                            String modeValue = r.optString("value");
                            list.add(new PayMode(modeKey, modeValue));
                        }
                    }
                }
            }
        }
        return list;
    }
    
    public static class PayMode {
        public String mModeKey;
        public String mModeValue;

        public PayMode(String modeKey, String modeValue) {
            mModeKey = modeKey;
            mModeValue = modeValue;
        }
    }
}
