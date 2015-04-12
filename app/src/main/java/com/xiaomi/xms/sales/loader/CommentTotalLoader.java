
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONObject;

public class CommentTotalLoader extends BaseLoader<CommentTotalLoader.Result> {

    private final String CACHE_KEY = "CommentTotalLoader";
    private String mProductId;

    public CommentTotalLoader(Context context, String productId) {
        super(context);
        mProductId = productId;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public int mTotal;

        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mTotal = mTotal;
            return newResult;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new CommentTotalUpdateTask();
    }

    private class CommentTotalUpdateTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getCommentTotal());
            request.addParam(Parameters.Keys.GOODS_ID, mProductId);
            return request;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) {
        result.mTotal = json.optInt(Tags.DATA);
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }
}
