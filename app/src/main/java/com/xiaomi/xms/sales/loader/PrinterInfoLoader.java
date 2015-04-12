
package com.xiaomi.xms.sales.loader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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

public class PrinterInfoLoader extends BaseLoader<PrinterInfoLoader.Result> {
    private String deviceType;
    public PrinterInfoLoader(Context context,String deviceType) {
        super(context);
        this.deviceType = deviceType;
    }

    public static final class Result extends BaseResult {
        public ArrayList<PrinterMode> p;
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.p = p;
            return newResult;
        }

        @Override
        public int getCount() {
            return p == null ? 0 : 1;
        }
    }


    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        tasks.add(new DeviceUpdateTask(false));
    }
    
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}


    private class DeviceUpdateTask extends BaseLoader<Result>.UpdateTask {
    	public DeviceUpdateTask(boolean needSaveToDb) {
            super(needSaveToDb);
        }
    	 @Override
         protected Request getRequest() {
             Request request = new Request(HostManager.URL_XMS_SALE_API);
             JSONObject jsonObject = new JSONObject();
             try {
            	 jsonObject.put(Tags.XMSAPI.ORGID,
                         Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
             	jsonObject.put("deviceType", deviceType);
                 String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_DEVICE, jsonObject);
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
    	if (json != null) {
            try {
                if (Tags.isJSONReturnedOK(json)) {
                    String bodyStr = json.optString(Tags.BODY);
                    if (!TextUtils.isEmpty(bodyStr)) {
                        JSONArray body = new JSONArray(bodyStr);
                        ArrayList<PrinterMode> list = new ArrayList<PrinterMode>();
                        for (int i = 0; i < body.length(); i++) {
                            if (!body.isNull(i)) {
                                JSONObject printerJson = (JSONObject) body.get(i);
                                int key = printerJson.optInt("id");
                                String ip = printerJson.optString("addressInfo");
                                String value = printerJson.optString("deviceName");
                                PrinterMode printer = new PrinterMode(key, value,ip);
                                
                                list.add(printer);
                            }
                        }
                        result.p = list;
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
    
    public static class PrinterMode {
        public int mModeKey;
        public String mModeValue;
        public String mIpAdress;
        public PrinterMode(int modeKey, String modeValue,String ipAdress) {
            mModeKey = modeKey;
            mModeValue = modeValue;
            mIpAdress = ipAdress;
        }
    }


	@Override
	protected String getCacheKey() {
		// TODO Auto-generated method stub
		return null;
	}
    
}
