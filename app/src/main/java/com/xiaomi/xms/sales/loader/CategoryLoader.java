
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.CategoryInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryLoader extends BaseLoader {
    private static final String TAG = "CategoryLoader";

    private final String CACHE_KEY = "category";
    private String mRootId;

    public CategoryLoader(Context context, String rootId) {
        super(context);
        mRootId = rootId;
    }

    public static final class Result extends BaseResult {
        public ArrayList<CategoryInfo> mCategoryInfos;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mCategoryInfos = mCategoryInfos;
            return newResult;
        }

        @Override
        protected int getCount() {
            return mCategoryInfos == null ? 0 : mCategoryInfos.size();
        }
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY + mRootId;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new CategoryUpdateTask();
    }

    private class CategoryUpdateTask extends UpdateTask {

        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getCategoryTree());
            request.addParam(Parameters.Keys.ROOT_ID, mRootId);
            return request;
        }
    }

    @Override
    protected BaseResult parseResult(JSONObject json, BaseResult baseResult) throws Exception {
        Result result = (Result) baseResult;
        result.mCategoryInfos = CategoryInfo.valueOf(json);
        return result;
    }

    @Override
    protected BaseResult getResultInstance() {
        return new Result();
    }
}
