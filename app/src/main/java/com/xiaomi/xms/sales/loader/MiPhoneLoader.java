
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.MiPhoneDetailInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONObject;

public class MiPhoneLoader extends BaseLoader {

    private final String CACHE_KEY = "miphonedetails";
    private String mProductId;

    public MiPhoneLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
        public MiPhoneDetailInfo mMiPhoneDetailInfos;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mMiPhoneDetailInfos = mMiPhoneDetailInfos;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mMiPhoneDetailInfos == null ? 0 : 1;
        }

    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new MiPhoneDetailUpdateTask();
    }

    private class MiPhoneDetailUpdateTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getMiPhoneDetail());
            request.addParam(Parameters.Keys.PRODUCT_ID, mProductId);
            return request;
        }
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mMiPhoneDetailInfos = MiPhoneDetailInfo.valueOf(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    public void setProductId(String productId) {
        mProductId = productId;
    }
}
