
package com.xiaomi.xms.sales.loader;

import android.content.Context;

import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.ThreadPool;

import org.json.JSONObject;

public abstract class BasePageLoader<GenericResult extends BaseResult> extends
        BaseLoader<GenericResult> {
    // private static final String TAG = "BasePageLoader";
    private int mPage;
    private boolean mNeedNextPage;

    public BasePageLoader(Context context) {
        super(context);
        mPage = 1;
        mNeedNextPage = false;
    }

    public boolean hasNextPage() {
        // 只要翻页了，就不需要从数据库取数据，只从服务器取数据
        if (mNeedNextPage) {
            mPage++;
        }
        setNeedDatabase(false);
        return mNeedNextPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public int getPage() {
        return mPage;
    }

    @Override
    public void reload() {
        if (!isLoading()) {
            mPage = 1;
            mNeedNextPage = false;
            super.reload();
        }
    }

    /**
     * 从服务器将app列表载入缓存和数据库
     */
    protected abstract class PageUpdateLTask extends BaseLoader<GenericResult>.UpdateTask {
        protected boolean mIsAppend;

        public PageUpdateLTask() {
            if (mPage == 1) {
                // 第一页时不是附加
                mIsAppend = false;
            } else {
                mIsAppend = true;
            }
        }

        /**
         * 获得请求网络数据的Request
         */
        protected abstract Request getRequest(int page);

        @Override
        protected final Request getRequest() {
            return getRequest(mPage);
        }

        protected GenericResult doInBackground(Void... params) {
            Request request = getRequest(mPage);
            int status = request.getStatus();
            GenericResult result = getResultInstance();
            if (status == Request.STATUS_OK) {
                final String etag = request.getEtag();
                JSONObject mainObject = request.requestJSON();
                GenericResult newResult = parseTaskResult(mainObject);
                result = onDataLoaded(mResult, newResult);
                final String jonsString = mainObject.toString();
                if (!mIsAppend) {
                    ThreadPool.execute(new Runnable() {
                        public void run() {
                            saveCacheToDB(getCacheKey(), jonsString, etag);
                        }
                    });
                }
                return result;
            } else if (status == Request.STATUS_NETWORK_UNAVAILABLE) {
                result.setResultStatus(ResultStatus.NETWROK_ERROR);
            } else if (status == Request.STATUS_AUTH_ERROR) {
                result.setResultStatus(ResultStatus.AUTH_ERROR);
            } else {
                result.setResultStatus(ResultStatus.SERVICE_ERROR);
            }
            return result;
        }

        /**
         * 子类可以对获取的数据进行处理，如果该方法返回null，则数据和界面不会更新
         */
        protected GenericResult onDataLoaded(GenericResult oldResult, GenericResult newResult) {
            mNeedNextPage = newResult.getCount() >= getPageSizeValue();
            GenericResult processed = newResult;
            if (mIsAppend) {
                // 如果是附加，则最终显示的结果是旧数据与新数据的整合
                processed = merge(oldResult, newResult);
            }
            // 返回整合后的数据
            return processed;
        }

        protected abstract GenericResult merge(GenericResult oldResult, GenericResult newResult);

        protected int getPageSizeValue() {
            return HostManager.Parameters.Values.PAGESIZE_VALUE;
        }
    }
}
