
package com.xiaomi.xms.sales.loader;

import java.text.DecimalFormat;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;


public class GetServiceNumberLoader extends BaseLoader<GetServiceNumberLoader.Result> {

    private final String CACHE_KEY = "GetServiceNumberLoader";
    public GetServiceNumberLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
    	public String serviceNumber;
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.serviceNumber = serviceNumber;
            return newResult;
        }

        @Override
        public int getCount() {
            return serviceNumber == null ? 0 : 1;
        }
    }

    protected String getCacheKey() {
        return "";
    }

    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        tasks.add(new GetServiceNumberUpdateTask(false));
    }



	private class GetServiceNumberUpdateTask extends BaseLoader<Result>.UpdateTask {
    	public GetServiceNumberUpdateTask(boolean needSaveToDb) {
            super(needSaveToDb);
        }
    	 @Override
         protected Request getRequest() {
             Request request = new Request(HostManager.URL_XMS_SALE_API);
             JSONObject jsonObject = new JSONObject();
             try {
            	 jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                 jsonObject.put(Tags.XMSAPI.ORGID,
                         Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
                 jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
                 String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GET_SERVICENUMBER, jsonObject);
                 if (!TextUtils.isEmpty(data)) {
                     request.addParam(Tags.RequestKey.DATA, data);
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return request;
         }
    }


    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
    	System.out.println("json:"+json);
    	if (json != null) {
            try {
                if (Tags.isJSONReturnedOK(json)) {
                    String bodyStr = json.optString(Tags.BODY);
                    if (!TextUtils.isEmpty(bodyStr)) {
                        JSONObject body = new JSONObject(bodyStr);
                        if (body != null) {
                           result.serviceNumber = body.optString("service_number");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            result.setResultStatus(ResultStatus.DATA_ERROR);
        }
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }
    
    
}
