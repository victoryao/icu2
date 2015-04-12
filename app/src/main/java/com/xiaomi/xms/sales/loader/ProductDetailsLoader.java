
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.ProductDetailsInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProductDetailsLoader extends BaseLoader<ProductDetailsLoader.Result> {

    private final String CACHE_KEY = "productdetails";
    private String mProductId;
    private String mMihomeId;
    private String mContainId;

    public ProductDetailsLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
        public ProductDetailsInfo mProductDetailsInfos;
        public int mMihomeStorageCount;

        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mMihomeStorageCount = mMihomeStorageCount;
            newResult.mProductDetailsInfos = mProductDetailsInfos;
            return newResult;
        }

        @Override
        public int getCount() {
            return mProductDetailsInfos == null ? 0 : 1;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        if (mNeedDatabase) {
            DatabaseTask task = getDatabaseTask();
            if (task != null) {
                tasks.add(task);
            }
        }

        tasks.add(new ProductDetailsUpdateTask());
        if (!TextUtils.isEmpty(mMihomeId)) {
            tasks.add(new MihoneStorageTask(false));
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new ProductDetailsUpdateTask();
    }

    public void setProductId(String productId) {
        mProductId = productId;
    }

    public void setMihomeId(String mihomeId) {
        mMihomeId = mihomeId;
    }

    public void setContainId(String mContainId) {
        this.mContainId = mContainId;
    }

    private class ProductDetailsUpdateTask extends BaseLoader<Result>.UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getProductDetails());
            request.addParam(Parameters.Keys.PRODUCT_ID, mProductId);
            return request;
        }
    }

    private class MihoneStorageTask extends BaseLoader<Result>.UpdateTask {
        public MihoneStorageTask(boolean needSaveToDb) {
            super(needSaveToDb);
        }

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                if (TextUtils.isEmpty(mContainId)) {
                    jsonObject.put("goodsId", mProductId);
                } else {
                    jsonObject.put("goodsId", mContainId);
                }
                jsonObject.put("orgId", mMihomeId);
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETSTOCKNUM, jsonObject);
                if (!TextUtils.isEmpty(data)) {
                    request.addParam(Tags.RequestKey.DATA, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return request;
        }

        @Override
        protected Result parseTaskResult(JSONObject json) {
            Result result = getResultInstance();
            if (json != null) {
                try {
                    if (Tags.isJSONReturnedOK(json)) {
                        String bodyStr = json.optString(Tags.BODY);
                        if (!TextUtils.isEmpty(bodyStr)) {
                            JSONObject body = new JSONObject(bodyStr);
                            if (body != null) {
                                result.mMihomeStorageCount = body.optInt("stock");
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
        protected Result onDataLoaded(Result oldResult, Result newResult) {
            newResult.mProductDetailsInfos = oldResult.mProductDetailsInfos;
            return newResult;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        ProductDetailsInfo productDetailsInfos = ProductDetailsInfo.valueOf(json);
        if (productDetailsInfos != null) {
            int count = productDetailsInfos.getBuyNumber();
        }
        result.mProductDetailsInfos = productDetailsInfos;
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }
}
