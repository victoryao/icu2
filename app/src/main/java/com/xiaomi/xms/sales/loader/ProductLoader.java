
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.PhoneModelInfo;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductLoader extends BasePageLoader<ProductLoader.Result> {
    private static final String TAG = "ProductLoader";

    private final String CACHE_KEY = "product";
    private String mCategoryId;
    private String mPhoneModel = "";
    private boolean mNeedPhoneModel;
    private int mPageSIze;

    public static final String SORT_DEFAULT = "";
    public static final String SORT_TIME = "1";
    public static final String SORT_PRICE_DESC = "10";
    public static final String SORT_PRICE_ASC = "8";
    public static final String SORT_COMMENT = "3";
    private String mSortType = SORT_DEFAULT;

    public ProductLoader(Context context, String categoryId) {
        super(context);
        mCategoryId = categoryId;
        mNeedPhoneModel = true;
    }

    public static final class Result extends BaseResult {
        public ArrayList<ProductInfo> mProductInfos;
        public ArrayList<PhoneModelInfo> mPhoneModelInfos;
        public String mCateName;

        public Result() {
            mProductInfos = new ArrayList<ProductInfo>();
            mPhoneModelInfos = new ArrayList<PhoneModelInfo>();
        }

        @Override
        public int getCount() {
            return mProductInfos == null ? 0 : mProductInfos.size();
        }

        private void addList(ArrayList<ProductInfo> alist) {
            if (alist != null) {
                mProductInfos.addAll(alist);
            }
        }

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mProductInfos = mProductInfos;
            newResult.mPhoneModelInfos = mPhoneModelInfos;
            newResult.mCateName = mCateName;
            return newResult;
        }
    }

    public void setNeedPhoneModelTask(boolean need) {
        mNeedPhoneModel = need;
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY + mCategoryId + "," + mPhoneModel + "," + mSortType;
    }

    @Override
    protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
        if (mNeedDatabase) {
            DatabaseTask task = getDatabaseTask();
            if (task != null) {
                tasks.add(task);
            }
        }
        if (mNeedPhoneModel) {
            tasks.add(new PhoneModelUpdateTask());
        }
        tasks.add(new ProductUpdateTask());
    }

    private class ProductUpdateTask extends PageUpdateLTask {
        @Override
        protected Request getRequest(int page) {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                jsonObject.put("cateId", mCategoryId);
                jsonObject.put(Parameters.Keys.PAGE_INDEX, String.valueOf(page));
                jsonObject.put(Parameters.Keys.PAGE_SIZE,
                        String.valueOf(Parameters.Values.PAGESIZE_VALUE));
                jsonObject.put(Parameters.Keys.ADAPT_PHONE, mPhoneModel);
                jsonObject.put("sort_type", mSortType);
                jsonObject.put("type", "");
                jsonObject.put("on_sale", "");
                jsonObject.put("price_from", "");
                jsonObject.put("price_to", "");
                jsonObject.put("orgId",
                        Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GOODSLISTBYCATEID, jsonObject);
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
            Result result = new Result();
            if (oldResult.mProductInfos != null) {
                result.addList(oldResult.mProductInfos);
            }
            if (newResult.mProductInfos != null) {
                result.addList(newResult.mProductInfos);
            }
            return result;
        }

        @Override
        protected int getPageSizeValue() {
            return mPageSIze == 0 ? HostManager.Parameters.Values.PAGESIZE_VALUE : mPageSIze + 1;
        }
    }

    private class PhoneModelUpdateTask extends BaseLoader<Result>.UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getAdaptPhoneInfo());
            request.addParam(Parameters.Keys.CATEGORY_ID, mCategoryId);
            request.addParam(Parameters.Keys.ADAPT_SIMPLE, Parameters.Values.ADAPT_SIMPLE_VALUE);
            return request;
        }

        @Override
        protected Result parseTaskResult(JSONObject json) {
            Result result = getResultInstance();
            try {
                result.mPhoneModelInfos = PhoneModelInfo.valueOf(json);
            } catch (JSONException e) {
                result.setResultStatus(ResultStatus.DATA_ERROR);
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected Result onDataLoaded(Result oldResult, Result newResult) {
            newResult.mProductInfos = oldResult.mProductInfos;
            return newResult;
        }
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }

    public void setPhoneModel(String phoneModel) {
        mPhoneModel = phoneModel;
    }

    public void setSortType(String sortType) {
        mSortType = sortType;
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        result.mProductInfos = ProductInfo.valueOf(json);
        result.mCateName = ProductInfo.getCateName(json);
        mPageSIze = ProductInfo.getPageSize(json);
        return result;
    }
}
