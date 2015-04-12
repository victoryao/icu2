
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SaleOutRegisterLoader extends BaseLoader<SaleOutRegisterLoader.Result> {
    private String mProductId;

    public SaleOutRegisterLoader(Context context, String productId) {
        super(context);
        mProductId = productId;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public boolean isSuc;
        public ArrayList<ProductInfo> recommandProducts;

        public Result() {
            recommandProducts = new ArrayList<ProductInfo>();
        }

        @Override
        public int getCount() {
            return isSuc ? 1 : 0;
        }

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.isSuc = isSuc;
            newResult.recommandProducts = recommandProducts;
            return newResult;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new SaleOutRegisterUpdateTask();
    }

    private class SaleOutRegisterUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getSaleOutReg());
            request.addParam(Parameters.Keys.PRODUCT_ID, mProductId);
            return request;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        result.isSuc = Tags.isJSONResultOK(json);
        if (!result.isSuc) {
            result.setResultStatus(ResultStatus.DATA_ERROR);
        } else {
            JSONObject data = json.optJSONObject(Tags.DATA);
            if (data != null) {
                JSONArray jsonArray = data.optJSONArray(Tags.SaleOutRegister.MORE);
                int number = jsonArray == null ? 0 : jsonArray.length();
                for (int i = 0; i < number; ++i) {
                    JSONObject one = jsonArray.optJSONObject(i);
                    if (one != null) {
                        /**
                         * only get the fields that are needed by recommendation
                         */
                        String productId = one.optString(Tags.Product.PRODUCT_ID);
                        String productName = one.optString(Tags.Product.PRODUCT_NAME);
                        String price = one.optString(Tags.Product.PRICE);
                        String imageUrl = one.optString(Tags.Product.IMAGE_URL);
                        String url = one.optString(Tags.Product.URL, "");
                        result.recommandProducts.add(new ProductInfo(productId, productName, price,
                                null, null, false, new Image(imageUrl), url, null));
                    }
                }
            }
        }
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

}
