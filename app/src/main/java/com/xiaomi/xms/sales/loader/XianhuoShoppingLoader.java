
package com.xiaomi.xms.sales.loader;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.JsonUtil;

public class XianhuoShoppingLoader extends BaseLoader<XianhuoShoppingLoader.Result> {

    private final String CACHE_KEY = "xianhuoProductdetails";
    private String mMihomeId;
    private String goodsId;
    private String sn;
    public XianhuoShoppingLoader(Context context,String goodsId,String mMihomeId,String mContainId,String sn) {
        super(context);
        this.goodsId = goodsId;
        this.mMihomeId = mMihomeId;
        this.sn = sn;
    }

    public static final class Result extends BaseResult {
        public ProductInfo p;
        public String resultInfo;
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.p = p;
            newResult.resultInfo = resultInfo;
            return newResult;
        }

        @Override
        public int getCount() {
            return p == null ? 0 : 1;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + goodsId;
    }

    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        tasks.add(new ProductDetailsUpdateTask(false));
    }

   

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public void setMihomeId(String mihomeId) {
        mMihomeId = mihomeId;
    }


	public void setSn(String sn) {
		this.sn = sn;
	}
	
    private class ProductDetailsUpdateTask extends BaseLoader<Result>.UpdateTask {
    	public ProductDetailsUpdateTask(boolean needSaveToDb) {
            super(needSaveToDb);
        }
    	 @Override
         protected Request getRequest() {
             Request request = new Request(HostManager.URL_XMS_SALE_API);
             JSONObject jsonObject = new JSONObject();
             try {
             	jsonObject.put("goodsId", goodsId);
             	jsonObject.put("orgId", mMihomeId);
             	jsonObject.put("sn", sn);
                 String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETPRODUCTINFO, jsonObject);
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
                    String bodyStr = json.optString(Tags.BODY);
                    if (!TextUtils.isEmpty(bodyStr)) {
                        JSONObject body = new JSONObject(bodyStr);
                        if (body != null) {
                            String productStr = body.optString("data");
                            if(!TextUtils.isEmpty(productStr)){
                            	JSONObject pJSON = new JSONObject(productStr);
                            	if(pJSON != null){
                            		result.p = ProductInfo.valueof(pJSON);
                            		result.resultInfo = "OK";
                            	}
                            }
                        }
                    }
                }
                else{
                	String header = json.optString(Tags.HEADER);
                	if(!TextUtils.isEmpty(header)){
                		JSONObject h = new JSONObject(header);
                		if(h != null ){
                			String code = h.optString(Tags.CODE);
                        	if(code != null && code.equalsIgnoreCase("208")){
                        		result.resultInfo = "sn错误或此商品已经被卖过";
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
