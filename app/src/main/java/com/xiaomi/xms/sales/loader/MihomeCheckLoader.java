package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.MiHomeCheckInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONObject;

public class MihomeCheckLoader extends BaseLoader{

    public static final String CACHE_KEY = "MihomeCheckinfo";
    private String mMihomeId;

    public MihomeCheckLoader(Context context, String mihomeId) {
        super(context);
        mMihomeId = mihomeId;
    }

    public static final class Result extends BaseResult {
        public MiHomeCheckInfo mMiHomeCheckInfo;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mMiHomeCheckInfo = mMiHomeCheckInfo;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mMiHomeCheckInfo == null ? 0 : 1;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new MiHomeCheckInfoUpdateTask();
    }

    private class MiHomeCheckInfoUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getMihomeSignInfo());
            request.addParam(Tags.MihomeCheckInfo.TDCODE, mMihomeId);
            return request;
        }
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mMiHomeCheckInfo = MiHomeCheckInfo.fromJSONObject(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

}
