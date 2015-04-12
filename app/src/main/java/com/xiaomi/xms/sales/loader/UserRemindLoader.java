
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.model.RemindInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

public class UserRemindLoader extends BaseLoader<UserRemindLoader.Result> {
    private static final String CACHE_KEY = "userRemind";

    public UserRemindLoader(Context context) {
        super(context);
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public RemindInfo mRemindInfo;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mRemindInfo = mRemindInfo;
            return newResult;
        }

        @Override
        public int getCount() {
            return mRemindInfo == null ? 0 : 1;
        }
    }

    private class UserRemindUpdateTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETSALESORDERCOUNT, jsonObject);
                if (!TextUtils.isEmpty(data)) {
                    request.addParam(Tags.RequestKey.DATA, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return request;
        }
    }

    @Override
    protected boolean isUserRelated() {
        return true;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new UserRemindUpdateTask();
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) {
        result.mRemindInfo = RemindInfo.fromJSONObject(json);
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

}
