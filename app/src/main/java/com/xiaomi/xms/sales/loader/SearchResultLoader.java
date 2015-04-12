
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultLoader extends BasePageLoader<SearchResultLoader.Result> {

    private String mCategoryId;
    private String mKeyWord;

    public SearchResultLoader(Context context, String categoryId, String keyWord) {
        super(context);
        mCategoryId = categoryId;
        mKeyWord = keyWord;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public ArrayList<ProductInfo> mProductInfos;
        public String mTotalCount;

        public Result() {
            mProductInfos = new ArrayList<ProductInfo>();
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
            newResult.mTotalCount = mTotalCount;
            return newResult;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new SearchResultUpdateTask();
    }

    private class SearchResultUpdateTask extends PageUpdateLTask {
        @Override
        protected Request getRequest(int page) {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                jsonObject.put(Parameters.Keys.CATEGORY_ID, mCategoryId);
                jsonObject.put(Parameters.Keys.PAGE_INDEX, String.valueOf(page));
                jsonObject.put(Parameters.Keys.PAGE_SIZE,
                        String.valueOf(Parameters.Values.PAGESIZE_VALUE));
                jsonObject.put(Parameters.Keys.KEYWORD, mKeyWord);
                jsonObject.put("orgId",
                        Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_SEARCHGOODSLIST, jsonObject);
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
            if (!TextUtils.isEmpty(oldResult.mTotalCount)) {
                result.mTotalCount = oldResult.mTotalCount;
            }
            return result;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        result.mProductInfos = valueOf(json);
        result.mTotalCount = ProductInfo.getSearchResultCount(json);
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    public static ArrayList<ProductInfo> valueOf(JSONObject json) throws JSONException {
        ArrayList<ProductInfo> list = null;
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                JSONArray productJsonArray = body.optJSONArray(Tags.Product.PRODUCT);
                if (productJsonArray != null) {
                    list = new ArrayList<ProductInfo>();
                    for (int i = 0; i < productJsonArray.length(); i++) {
                        if (!productJsonArray.isNull(i)) {
                            JSONObject proJsonobject = (JSONObject) productJsonArray.get(i);
                            String productId = proJsonobject.optString(Tags.Product.PRODUCT_ID);
                            String productName = proJsonobject.optString(Tags.Product.PRODUCT_NAME);
                            String price = proJsonobject.optString(Tags.Product.PRICE);
                            String marketPrice = proJsonobject.optString(Tags.Product.MARKET_PRICE);
                            String styleName = proJsonobject.optString(Tags.Product.STYLE_NAME);
                            boolean hasProduct = proJsonobject.optBoolean(Tags.Product.IS_COS);
                            String imageUrl = proJsonobject.optString(Tags.Product.IMAGE_URL);
                            String url = proJsonobject.optString(Tags.Product.URL, "");
                            String displayType = proJsonobject.optString(Tags.Product.DISPLAY_TYPE,
                                    Tags.Product.DISPLAY_NATIVE);
                            String pid = proJsonobject.optString(Tags.Product.P_ID, productId);
                            boolean isBatched = proJsonobject.optBoolean(Tags.Product.IS_BATCHED, false);
                            String containId = proJsonobject.optString(Tags.Product.CONTAINID);
                            ProductInfo productInfo = new ProductInfo(productId, productName, price, marketPrice,
                                    styleName, !hasProduct, new Image(imageUrl), url, displayType);
                            productInfo.setPid(pid);
                            productInfo.setIsBatched(isBatched);
                            productInfo.setContainId(containId);
                            list.add(productInfo);
                        }
                    }
                }
            }
        }
        return list;
    }

}
