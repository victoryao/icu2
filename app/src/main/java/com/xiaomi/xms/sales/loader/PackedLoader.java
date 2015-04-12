
package com.xiaomi.xms.sales.loader;

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
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class PackedLoader extends BaseLoader<PackedLoader.Result> {

    private final String CACHE_KEY = "PackedLoader";
    private String serviceNumber;
    public PackedLoader(Context context,String serviceNumber) {
        super(context);
        this.serviceNumber = serviceNumber;
    }

    public static final class Result extends BaseResult {
    	public String responseInfo;
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.responseInfo = responseInfo;
            return newResult;
        }

        @Override
        public int getCount() {
            return responseInfo == null ? 0 : 1;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + serviceNumber;
    }

    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        tasks.add(new PackedUpdateTask(false));
    }

	
	public void setServiceNumber(String serviceNumber) {
		this.serviceNumber = serviceNumber;
	}

	private class PackedUpdateTask extends BaseLoader<Result>.UpdateTask {
    	public PackedUpdateTask(boolean needSaveToDb) {
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
                 jsonObject.put("serviceNumber", serviceNumber);
                 String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_PACKED, jsonObject);
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
    	System.out.println("result:"+json);
    	if (json != null) {
            try {
                if (Tags.isJSONReturnedOK(json)) {
                    String headerStr = json.optString("header");
                    if (!TextUtils.isEmpty(headerStr)) {
                        JSONObject header = new JSONObject(headerStr);
                        if (header != null) {
                            String code = header.optString("code");
                            String desc = header.optString("desc");
                            if(code != null && code.equals("200")){
                            	result.responseInfo = "ok";
                            }
                            else{
                            	result.responseInfo = desc;  //错误描叙信息
                            }
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
