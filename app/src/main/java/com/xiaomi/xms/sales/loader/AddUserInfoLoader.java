
package com.xiaomi.xms.sales.loader;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class AddUserInfoLoader extends BaseLoader<AddUserInfoLoader.Result> {

    private final String CACHE_KEY = "AddUserInfo";
    private String serviceNumber;
    private String mUserNameStr;
    private String mUserTelStr;
    private String email;
    private String invoiceType;
    private String mInvoiceTitleStr;
    
    public void setServiceNumber(String serviceNumber) {
		this.serviceNumber = serviceNumber;
	}

	public void setmUserNameStr(String mUserNameStr) {
		this.mUserNameStr = mUserNameStr;
	}

	public void setmUserTelStr(String mUserTelStr) {
		this.mUserTelStr = mUserTelStr;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public void setmInvoiceTitleStr(String mInvoiceTitleStr) {
		this.mInvoiceTitleStr = mInvoiceTitleStr;
	}

	public AddUserInfoLoader(Context context,String serviceNumber,String mUserNameStr,String mUserTelStr,
    		String email,String invoiceType,String mInvoiceTitleStr) {
        super(context);
        this.serviceNumber = serviceNumber;
        this.mUserNameStr = mUserNameStr;
        this.mUserTelStr = mUserTelStr;
        this.email = email;
        this.invoiceType = invoiceType;
        this.mInvoiceTitleStr = mInvoiceTitleStr;
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
        tasks.add(new AddUserInfoUpdateTask(false));
    }


	private class AddUserInfoUpdateTask extends BaseLoader<Result>.UpdateTask {
    	public AddUserInfoUpdateTask(boolean needSaveToDb) {
            super(needSaveToDb);
        }
    	 @Override
         protected Request getRequest() {
    		 Request request = new Request(HostManager.URL_XMS_SALE_API);
             JSONObject jsonObject = new JSONObject();
             try {
                 jsonObject.put("serviceNumber", serviceNumber);
                 jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                 jsonObject.put("consignee", mUserNameStr);
                 jsonObject.put("tel", mUserTelStr);
                
                 jsonObject.put("email", email);
                 jsonObject.put("invoiceType", invoiceType);
                 jsonObject.put("invoiceTitle", mInvoiceTitleStr);
                 String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_UPDATECONSIGNEEINF,
                         jsonObject);
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
                    String headerStr = json.optString("header");
                    if (!TextUtils.isEmpty(headerStr)) {
                        JSONObject header = new JSONObject(headerStr);
                        if (header != null) {
                            String code = header.optString("code");
                            if(code != null && code.equals("200")){
                            	result.responseInfo = "ok";
                            }
                            else{
                            	result.responseInfo = "error";
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
