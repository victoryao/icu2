
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.view.View;

import com.xiaomi.xms.sales.model.CommentItemInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.HostManager.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommentListLoader extends BasePageLoader<CommentListLoader.Result> {

    private final String CACHE_KEY = "CommentListLoader";
    private String mProductId;

    public CommentListLoader(Context context, String productId) {
        super(context);
        mProductId = productId;
    }

    public static final class Result extends BaseResult {
        public ArrayList<CommentItemInfo> mCommentInfoList;
        public int mTotalCount;

        public Result() {
            super();
            mCommentInfoList = new ArrayList<CommentItemInfo>();
            mTotalCount = 0;
        }

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mCommentInfoList = mCommentInfoList;
            newResult.mTotalCount = mTotalCount;
            return newResult;
        }

        @Override
        public int getCount() {
            return mCommentInfoList == null ? 0 : mCommentInfoList.size();
        }
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY + mProductId;
    }

    @Override
    protected UpdateTask getUpdateTask() {
        return new ReviewListUpdateTask();
    }

    private class ReviewListUpdateTask extends PageUpdateLTask {

        @Override
        protected int getPageSizeValue() {
            return 10;
        }

        @Override
        protected Request getRequest(int page) {
            Request request = new Request(HostManager.getCommentList());
            request.addParam(Parameters.Keys.GOODS_ID, mProductId);
            request.addParam(Parameters.Keys.PAGE_INDEX, String.valueOf(page));
            request.addParam(Parameters.Keys.PAGE_SIZE, String.valueOf(getPageSizeValue()));
            return request;
        }

        @Override
        protected Result merge(Result oldResult, Result newResult) {
            Result result = new Result();
            if (newResult == null && oldResult == null)
                return result;
            if (newResult == null)
                return oldResult;
            if (oldResult == null)
                return newResult;
            result.mCommentInfoList.addAll(oldResult.mCommentInfoList);
            result.mCommentInfoList.addAll(newResult.mCommentInfoList);
            result.mTotalCount = newResult.mTotalCount;
            for (CommentItemInfo info : result.mCommentInfoList) {
                info.setBottomLineVisibility(View.VISIBLE);
            }
            return result;
        }
    }

    @Override
    protected Result getResultInstance() {
        return new Result();
    }

    @Override
    protected Result parseResult(JSONObject json, Result result) {
        ArrayList<CommentItemInfo> list = new ArrayList<CommentItemInfo>();
        int total = 0;
        if (Tags.isJSONResultOK(json)) {
            JSONObject dataJson = json.optJSONObject(Tags.DATA);
            if (dataJson != null) {
                total = dataJson.optInt(Tags.CommentInfo.TOTAL_COUNT);
                JSONArray listJson = dataJson.optJSONArray(Tags.CommentInfo.COMMENTS);
                if (listJson != null) {
                    int length = listJson.length();
                    for (int i = 0; i < length; ++i) {
                        JSONObject one = listJson.optJSONObject(i);
                        if (one == null)
                            continue;
                        list.add(CommentItemInfo.valueOf(one));
                    }
                }
            }
        }
        result.mCommentInfoList = list;
        result.mTotalCount = total;
        return result;
    }
}
