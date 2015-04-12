
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecommendProductLoader extends BaseLoader<RecommendProductLoader.Result> {

    private final String CACHE_KEY = "recommend_product";
    private String mProductId;

    public RecommendProductLoader(Context context, String productId) {
        super(context);
        mProductId = productId;
    }

    public static final class Result extends BaseResult {
        public ArrayList<ProductInfo> mRecommandProducts;

        public Result() {
            mRecommandProducts = new ArrayList<ProductInfo>();
        }

        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mRecommandProducts = mRecommandProducts;
            return newResult;
        }

        @Override
        public int getCount() {
            return mRecommandProducts == null ? 0 : mRecommandProducts.size();
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new RecommandProductUpdateTask();
    }

    private class RecommandProductUpdateTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getRecommendProduct());
            request.addParam(Parameters.Keys.PRODUCT_ID, mProductId);
            return request;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        if (Tags.isJSONResultOK(json)) {
            JSONArray jsonArray = json.getJSONArray(Tags.DATA);
            int number = jsonArray.length();
            for (int i = 0; i < number; ++i) {
                JSONObject one = jsonArray.optJSONObject(i);
                if (one != null) {
                    /** only get the fields that are needed by recommendation */
                    String productId = one.optString(Tags.Product.PRODUCT_ID);
                    String productName = one.optString(Tags.Product.PRODUCT_NAME);
                    String price = one.optString(Tags.Product.PRICE);
                    String imageUrl = one.optString(Tags.Product.IMAGE_URL);
                    String url = one.optString(Tags.Product.URL, "");
                    result.mRecommandProducts.add(new ProductInfo(productId, productName, price, null,
                            null, false, new Image(imageUrl), url, null));
                }
            }
        }
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }
}
