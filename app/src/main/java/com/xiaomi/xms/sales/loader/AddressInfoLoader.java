
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.AddressInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONObject;

import java.util.ArrayList;

public class AddressInfoLoader extends BaseLoader {

    private static final String TAG = "AddressInfoLoader";
    private final String CACHE_KEY = "addressInfo";

    public AddressInfoLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
        public ArrayList<AddressInfo> mAddressInfos;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mAddressInfos = mAddressInfos;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mAddressInfos == null ? 0 : mAddressInfos.size();
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new AddressInfoUpdateTask();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

    private class AddressInfoUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            return new Request(HostManager.getAddressInfo());
        }
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mAddressInfos = AddressInfo.fromJSONObject(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }

    @Override
    protected boolean isUserRelated() {
        return true;
    }
}
