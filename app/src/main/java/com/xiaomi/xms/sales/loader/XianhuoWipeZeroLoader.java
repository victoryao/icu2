
package com.xiaomi.xms.sales.loader;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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

public class XianhuoWipeZeroLoader extends BaseLoader<XianhuoWipeZeroLoader.Result> {

    private final String CACHE_KEY = "xianhuoWipeZero";
    private String mMihomeId;
    private BigDecimal totalPrice;
    public XianhuoWipeZeroLoader(Context context,String mMihomeId,BigDecimal totalPrice) {
        super(context);
        this.mMihomeId = mMihomeId;
        this.totalPrice = totalPrice;
    }

    public static final class Result extends BaseResult {
        public BigDecimal newTotalPrice;
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.newTotalPrice = newTotalPrice;
            return newResult;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + totalPrice;
    }

    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        if (!TextUtils.isEmpty(mMihomeId)) {
            tasks.add(new WipeZeroUpdateTask(false));
        }
    }

    
	public void settotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public void setMihomeId(String mihomeId) {
        mMihomeId = mihomeId;
    }

    

    private class WipeZeroUpdateTask extends BaseLoader<Result>.UpdateTask {
    	public WipeZeroUpdateTask(boolean needSaveToDb) {
            super(needSaveToDb);
        }
    	 @Override
         protected Request getRequest() {
             Request request = new Request(HostManager.URL_XMS_SALE_API);
             JSONObject jsonObject = new JSONObject();
             try {
            	 System.out.println("old price:"+totalPrice);
             	jsonObject.put("orgId", mMihomeId);
             	jsonObject.put("totalPrice", totalPrice.multiply(new BigDecimal(100)));
             	System.out.println("old price 2:"+ totalPrice.multiply(new BigDecimal(100)));
                 String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_WIPEZERO, jsonObject);
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
                            int newPrice = body.optInt("total_price");
                            if(newPrice == 0 ){
                            	result.newTotalPrice = totalPrice;
                            }
                            else{
                            	float num = (float)newPrice/100;
                                DecimalFormat df = new DecimalFormat("0.00");//格式化小数，不足的补0
                                result.newTotalPrice = new BigDecimal(df.format(num));
                            }
                            
                        }
                    }
                }
                else{
                	result.newTotalPrice = totalPrice;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                result.newTotalPrice = totalPrice;
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
