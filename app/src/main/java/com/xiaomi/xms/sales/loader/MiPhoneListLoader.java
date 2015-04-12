package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.MiPhoneInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONObject;

import java.util.ArrayList;

public class MiPhoneListLoader extends BasePageLoader<MiPhoneListLoader.Result> {

    private String mCategoryId;
    private String mKeyWord;

    public MiPhoneListLoader(Context context, String categoryId, String keyWord) {
        super(context);
        mCategoryId = categoryId;
        mKeyWord = keyWord;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public ArrayList<MiPhoneInfo> mMiPhoneInfos;

        public Result() {
            mMiPhoneInfos = new ArrayList<MiPhoneInfo>();
        }

        @Override
        public int getCount() {
            return mMiPhoneInfos == null ? 0 : mMiPhoneInfos.size();
        }

        private void addList(ArrayList<MiPhoneInfo> alist) {
            if (alist != null) {
                mMiPhoneInfos.addAll(alist);
            }
        }

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mMiPhoneInfos = mMiPhoneInfos;
            return newResult;
        }
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new MiPhoneListUpdateTask();
    }

    private class MiPhoneListUpdateTask extends PageUpdateLTask {
        @Override
        protected Request getRequest(int page) {
            Request request = new Request(HostManager.getProduct());
            request.addParam("cat_id", mCategoryId);
            request.addParam(Parameters.Keys.PAGE_INDEX, String.valueOf(page));
            request.addParam(Parameters.Keys.PAGE_SIZE,
                    String.valueOf(Parameters.Values.PAGESIZE_VALUE));
            return request;
        }

        @Override
        protected Result merge(Result oldResult, Result newResult) {
            Result result = new Result();
            if (oldResult.mMiPhoneInfos != null) {
                result.addList(oldResult.mMiPhoneInfos);
            }
            if (newResult.mMiPhoneInfos != null) {
                result.addList(newResult.mMiPhoneInfos);
            }
            return result;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) throws Exception {
        result.mMiPhoneInfos = MiPhoneInfo.valueOf(json);
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
