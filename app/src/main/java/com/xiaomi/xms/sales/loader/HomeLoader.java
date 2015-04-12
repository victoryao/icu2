
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.HomeInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Device;

import org.json.JSONObject;

import java.util.ArrayList;

public class HomeLoader extends BaseLoader {

    private static final String TAG = "HomeLoader";

    private final String CACHE_KEY = "home";

    public HomeLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
        public ArrayList<HomeInfo> mHomeInfos;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mHomeInfos = mHomeInfos;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mHomeInfos == null ? 0 : mHomeInfos.size();
        }
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new HomeTask();
    }

    private class HomeTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getHome());
            request.addParam(HostManager.Parameters.Keys.PHONE_MODEL, Device.MODEL);
            request.addParam(HostManager.Parameters.Keys.PHONE_DEVICE, Device.DEVICE);
            return request;
        }
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mHomeInfos = HomeInfo.valueOf(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }
}
