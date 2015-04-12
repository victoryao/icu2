
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONObject;

public class RegionPaymentLoader extends BaseLoader<RegionPaymentLoader.Result> {

    private String mPayId;
    private String mRegionId;
    private String mMihomeBuyId;

    public RegionPaymentLoader(Context context, String payId, String regionId, String miHomeBuyId) {
        super(context);
        mPayId = payId;
        mRegionId = regionId;
        mMihomeBuyId = miHomeBuyId;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public JSONObject json;

        @Override
        public int getCount() {
            return json == null ? 0 : 1;
        }

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.json = json;
            return newResult;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new MyUpdateTask();
    }

    private class MyUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request r = new Request(HostManager.getRegionPayment());
            r.addParam("pay_id", mPayId);
            r.addParam("region_id", mRegionId);
            if (!TextUtils.isEmpty(mMihomeBuyId)) {
                r.addParam(Tags.CheckoutSubmit.MIHOME_BUY_ID, mMihomeBuyId);
            }
            return r;
        }

    }

    @Override
    protected Result parseResult(JSONObject json, Result baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.json = json;
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
