
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.UserInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfoLoader extends BaseLoader {
    private static final String TAG = "UserInfoLoader";
    private static final String CACHE_KEY = "userInfo";

    public UserInfoLoader(Context context) {
        super(context);
    }

    public static final class Result extends BaseResult {
        public UserInfo mUserInfo;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mUserInfo = mUserInfo;
            return newResult;
        }

        @Override
        public int getCount() {
            return mUserInfo == null ? 0 : 1;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new UserInfoUpdateTask();
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    protected boolean isUserRelated() {
        return true;
    }

    public void deleteCache() {
        mCache.deleteItem(getCacheKey());
    }

    private class UserInfoUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETUSERINFO,jsonObject);
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
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mUserInfo = UserInfo.fromJSONObject(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }
}
