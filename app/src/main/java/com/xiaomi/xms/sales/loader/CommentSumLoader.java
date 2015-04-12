
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.model.CommentSumInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONObject;

public class CommentSumLoader extends BaseLoader<CommentSumLoader.Result> {

    private final String CACHE_KEY = "CommentSumLoader";
    private String mProductId;

    public CommentSumLoader(Context context, String productId) {
        super(context);
        mProductId = productId;
        setNeedDatabase(false);
    }

    public static final class Result extends BaseResult {
        public CommentSumInfo mSummaryInfo;

        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mSummaryInfo = mSummaryInfo;
            return newResult;
        }

        @Override
        public int getCount() {
            return mSummaryInfo == null ? 0 : 1;
        }
    }

    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new CommentSumUpdateTask();
    }

    private class CommentSumUpdateTask extends UpdateTask {
        @Override
        protected Request getRequest() {
            Request request = new Request(HostManager.getCommentDetail());
            request.addParam(Parameters.Keys.GOODS_ID, mProductId);
            return request;
        }
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) {
        result.mSummaryInfo = CommentSumInfo.valueOf(json);
        return result;
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }
}
