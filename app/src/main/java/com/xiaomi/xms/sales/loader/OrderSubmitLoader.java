
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.OrderSubmitInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderSubmitLoader extends BaseLoader {

    //private final static String TAG = "OrderSubmitLoader";
    private String mJsonData;

    public OrderSubmitLoader(Context context) {
        super(context);
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public OrderSubmitInfo info;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.info = info;
            return newResult;
        }

        @Override
        protected int getCount() {
            return info == null ? 0 : 1;
        }
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    @Override
    protected DatabaseTask getDatabaseTask() {
        return null;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new OrderSubmitTask();
    }

    private class OrderSubmitTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getCheckoutSubmit());
            JSONObject json;
            try {
                json = new JSONObject(mJsonData);
                request.addParam(Tags.OrderSubmit.ADDRESS_ID, json.optString(Tags.OrderSubmit.ADDRESS_ID));
                request.addParam(Tags.OrderSubmit.PAY_ID, json.optString(Tags.OrderSubmit.PAY_ID));
                request.addParam(Tags.OrderSubmit.PICKUP_ID, json.optString(Tags.OrderSubmit.PICKUP_ID));
                request.addParam(Tags.OrderSubmit.SHIPMENT_ID, json.optString(Tags.OrderSubmit.SHIPMENT_ID));
                request.addParam(Tags.OrderSubmit.BEST_TIME, json.optString(Tags.OrderSubmit.BEST_TIME));
                request.addParam(Tags.OrderSubmit.INVOICE_TYPE, json.optString(Tags.OrderSubmit.INVOICE_TYPE));
                request.addParam(Tags.OrderSubmit.INVOICE_TITLE, json.optString(Tags.OrderSubmit.INVOICE_TITLE));
                request.addParam(Tags.OrderSubmit.COUPON_TYPE, json.optString(Tags.OrderSubmit.COUPON_TYPE));
                request.addParam(Tags.OrderSubmit.COUPON_CODE, json.optString(Tags.OrderSubmit.COUPON_CODE));
                request.addParam(Tags.OrderSubmit.MIHOME_BUY_ID, json.optString(Tags.OrderSubmit.MIHOME_BUY_ID));
                request.addParam(Tags.OrderSubmit.EXTEND_FIELD, json.optString(Tags.OrderSubmit.EXTEND_FIELD));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return request;
        }

    }

    public void setData(String jsonData) {
        mJsonData = jsonData;
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.info = OrderSubmitInfo.valueOf(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }

}
