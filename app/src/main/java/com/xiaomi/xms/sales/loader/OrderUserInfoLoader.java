
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.model.OrderUserInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderUserInfoLoader extends BaseLoader<OrderUserInfoLoader.Result> {
    private final String CACHE_KEY = "OrderUserInfoLoader";
    private String mOrderId;

    public OrderUserInfoLoader(Context context, String orderId) {
        super(context);
        mOrderId = orderId;
    }

    public static final class Result extends BaseResult {
        public OrderUserInfo mOrderUserInfo;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mOrderUserInfo = mOrderUserInfo;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mOrderUserInfo == null ? 0 : 1;
        }

    }

    @Override
    protected Result parseResult(JSONObject json, Result baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mOrderUserInfo = OrderUserInfo.fromJSONObject(json);
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

    @Override
    protected UpdateTask getUpdateTask() {
        return new OrderUserInfoUpdateTask();
    }

    private class OrderUserInfoUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                jsonObject.put("serviceNumber", mOrderId);
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETCONSIGNEEINFO, jsonObject);
                if (!TextUtils.isEmpty(data)) {
                    request.addParam(Tags.RequestKey.DATA, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return request;
        }

    }
}
