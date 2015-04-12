
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ShoppingLoader extends BaseLoader<ShoppingLoader.Result> {
    // private static final String TAG = "ShoppingLoader";
    private String mMihomeBuyId;

    public ShoppingLoader(Context context, String mihomeBuyId) {
        super(context);
        this.mMihomeBuyId = mihomeBuyId;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public ShoppingCartListInfo mInfo;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mInfo = mInfo;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mInfo == null ? 0 : 1;
        }
    }

    @Override
    protected String getCacheKey() {
        return null;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new ShoppingUpdateTask();
    }

    private class ShoppingUpdateTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                jsonObject.put("orgId",
                        Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETSALESCARTLIST,jsonObject);
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
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        result.mInfo = ShoppingCartListInfo.valueOf(json);
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }
}
