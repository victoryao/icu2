
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.model.ComboInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ComboLoader extends BaseLoader {
    private static final String TAG = "ComboLoader";

    private final String CACHE_KEY = "combo";
    private String mProductId;

    public ComboLoader(Context context) {
        super(context);
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public ComboInfo mComboInfos;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mComboInfos = mComboInfos;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mComboInfos == null ? 0 : 1;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new ComboUpdateTask();
    }

    private class ComboUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.URL_XMS_SALE_API);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("commodityId", mProductId);
                jsonObject.put("orgId",
                        Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
                jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
                String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETBATCHEDVIEW, jsonObject);
                if (!TextUtils.isEmpty(data)) {
                    request.addParam(Tags.RequestKey.DATA, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return request;
        }
    }

    public void setProductId(String productId) {
        mProductId = productId;
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mComboInfos = ComboInfo.valueOf(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }
}
