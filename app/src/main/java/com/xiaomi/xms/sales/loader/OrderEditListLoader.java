
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.model.OrderPreview;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderEditListLoader extends BasePageLoader<OrderEditListLoader.Result> {
    private static final String TAG = "OrderEditListLoader";
    private final String CACHE_KEY = "OrderEditList";

    public OrderEditListLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
        public ArrayList<OrderPreview> mOrderList;

        public Result() {
            super();
            mOrderList = new ArrayList<OrderPreview>();
        }

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mOrderList = mOrderList;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mOrderList == null ? 0 : mOrderList.size();
        }
    }

    @Override
    public Result getResultInstance() {
        return new Result();
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new OrderListUpdateTask();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    protected boolean isUserRelated() {
        return true;
    }

    private class OrderListUpdateTask extends PageUpdateLTask {

        @Override
        protected Request getRequest(int page) {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                jsonObject.put(Parameters.Keys.PAGE_INDEX, String.valueOf(page));
                jsonObject.put(Parameters.Keys.PAGE_SIZE,
                        String.valueOf(Parameters.Values.PAGESIZE_VALUE));
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETEDITORDERLIST, jsonObject);
                if (!TextUtils.isEmpty(data)) {
                    request.addParam(Tags.RequestKey.DATA, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return request;
        }

        @Override
        protected Result merge(Result oldResult, Result newResult) {
            Result result;
            if (oldResult == null) {
                result = new Result();
            } else {
                result = (Result) oldResult;
            }
            if (newResult != null) {
                result.mOrderList.addAll(((Result) newResult).mOrderList);
            }
            return result;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mOrderList = OrderPreview.valueOfOrderList(json);
        return result;
    }

}
