
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONObject;

public class CheckoutLoader extends BaseLoader {

    //private static final String TAG = "CheckoutLoader";
    private String mAddressId;
    private String mMihomeBuyId;

    public CheckoutLoader(Context context, String mihomeBuyId) {
        super(context);
        this.mMihomeBuyId = mihomeBuyId;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public JSONObject json;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.json = json;
            return newResult;
        }

        @Override
        protected int getCount() {
            return json == null ? 0 : 1;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new MyUploadTask();
    }

    private class MyUploadTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getCheckout());
            request.addParam(Tags.CheckoutSubmit.MIHOME_BUY_ID, mMihomeBuyId);
            if (!TextUtils.isEmpty(mAddressId)) {
                request.addParam(Tags.CheckoutSubmit.ADDRESS_ID, mAddressId);
            }
            return request;
        }
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    public void setAddressId(String addressId) {
        mAddressId = addressId;
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.json = json;
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }

}
