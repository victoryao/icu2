
package com.xiaomi.xms.sales.loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.cache.DbCache;
import com.xiaomi.xms.sales.cache.DbCache.DbCacheItem;
import com.xiaomi.xms.sales.db.DBContract;
import com.xiaomi.xms.sales.db.DBContract.Cache;
import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ThreadPool;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public abstract class BaseLoader<GenericResult extends BaseResult> extends Loader<GenericResult> {
    private static final String TAG = "BaseLoader";

    // Loader 加载结果
    protected GenericResult mResult;
    // 是否正在加载，如果正在加载则取消加载请求
    protected volatile boolean mIsLoading;
    // 是否需要读取数据库中的缓存数据
    protected boolean mNeedDatabase;
    // 是否需要从远程获取数据
    protected boolean mNeedServer;
    // 是否需要将结果传递给界面进行处理
    protected boolean mNeedDeliverResult;
    // 结果是否已经发送给过界面
    private boolean mHasDeliverdResult;
    // 任务队列，当界面是分布加载的时候，只需要按照顺序初始化Task即可
    protected List<AsyncTask<Void, Void, GenericResult>> mTaskList;
    private int mNextExecuteTask;
    // 加载任务时候是否有进度通知
    private ProgressNotifiable mProgressNotifiable;

    protected DbCache mCache;
    protected String mEtag;

    private WeakReference<BaseActivity> mBaseActivityRef;

    public BaseLoader(Context context) {
        super(context);
        if (context instanceof BaseActivity) {
            mBaseActivityRef = new WeakReference<BaseActivity>((BaseActivity) context);
        }
        mIsLoading = false;
        mNeedDatabase = true;
        mNeedServer = true;
        mNeedDeliverResult = true;
        mHasDeliverdResult = false;

        mTaskList = new ArrayList<AsyncTask<Void, Void, GenericResult>>();
        mNextExecuteTask = 0;

        mCache = new DbCache(context);
        mResult = getResultInstance();
    }

    /**
     * 重新加载数据
     */
    public void reload() {
        if (!this.isLoading()) {
            this.mNeedDatabase = false;
            this.forceLoad();
        }
    }

    public void setNeedDatabase(boolean needDatabase) {
        this.mNeedDatabase = needDatabase;
    }

    public void setNeedServer(boolean needServer) {
        this.mNeedServer = needServer;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setProgressNotifiable(ProgressNotifiable progressNotifiable) {
        this.mProgressNotifiable = progressNotifiable;
        // 注册监听器时直接告知当前状态
        if (progressNotifiable != null) {
            progressNotifiable.init(dataExists(), mIsLoading);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStartLoading() {
        if (mResult.getCount() > 0) {
            // 这里浅克隆结果，避免由于结果完全一致，不会刷新界面
            deliverResult((GenericResult) mResult.shallowClone());
        }

        if (!mIsLoading && (mResult.getCount() == 0 || takeContentChanged())) {
            forceLoad();
        }
    }

    @Override
    protected void onForceLoad() {
        mTaskList.clear();
        mNextExecuteTask = 0;
        initTaskList(mTaskList);
        executeNextTask();
    }

    /**
     * 子类可以继承该方法自定义任务列表，此时默认列表不起作用（一个数据库任务和一个网络任务）
     */
    protected void initTaskList(List<AsyncTask<Void, Void, GenericResult>> tasks) {
        if (mNeedDatabase) {
            DatabaseTask task = getDatabaseTask();
            if (task != null) {
                tasks.add(task);
            }
        }
        if (mNeedServer) {
            UpdateTask task = getUpdateTask();
            if (task != null) {
                tasks.add(task);
            }
        }
    }

    /**
     * 调用该方法将会执行下一个任务
     */
    protected void executeNextTask() {
        if (hasNextTask()) {
            AsyncTask<Void, Void, GenericResult> task = null;
            while (task == null && hasNextTask()) {
                task = mTaskList.get(mNextExecuteTask);
                mNextExecuteTask++;
            }
            if (task != null) {
                task.execute();
            }
        }
    }

    protected boolean hasNextTask() {
        return mNextExecuteTask < mTaskList.size();
    }

    protected DatabaseTask getDatabaseTask() {
        return new DatabaseTask();
    }

    protected UpdateTask getUpdateTask() {
        return null;
    }

    protected boolean dataExists() {
        // 数据存在并且已经发送给界面过
        return mResult.getCount() > 0 && mHasDeliverdResult;
    }

    protected abstract GenericResult parseResult(JSONObject json, GenericResult baseResult)
            throws Exception;

    protected abstract GenericResult getResultInstance();

    /**
     * 子类的任务都需要继承该类以获得任务管理和进度通知特性
     */
    protected abstract class BaseTask extends AsyncTask<Void, Void, GenericResult> {
        @Override
        protected void onPreExecute() {
        	/*ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE); 
        	HttpClient httpclient = new DefaultHttpClient();
            HttpParams params = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(params , 1);
            HttpConnectionParams.setSoTimeout(params ,1); */
//        	// 设置网络超时时间（请求和响应）
//            NetWorkConnection.getHttpClient().getParams()
//                            .setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
//                                            35000);
//            NetWorkConnection.getHttpClient().getParams()
//                            .setParameter(CoreConnectionPNames.SO_TIMEOUT, 35000);
            mIsLoading = true;
            if (mProgressNotifiable != null) {
                mProgressNotifiable.startLoading(dataExists());
            }
        }

        @Override
        protected void onPostExecute(GenericResult result) {
            mIsLoading = false;
            final ResultStatus status = result.getResultStatus();
            // 如果出错
            if (status != ResultStatus.OK) {
                if (mProgressNotifiable != null) {
                    mProgressNotifiable.onError(dataExists(), status,
                            new Callback() {
                                @Override
                                public boolean handleMessage(Message message) {
                                    if (status == BaseResult.ResultStatus.NETWROK_ERROR) {
                                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getContext().startActivity(intent);
                                    } else if (status == BaseResult.ResultStatus.AUTH_ERROR) {
                                        if (mBaseActivityRef != null) {
                                            BaseActivity ba = mBaseActivityRef.get();
                                            if (ba != null) {
                                                LoginManager.getInstance().setSystemLogin(false);
                                                ba.gotoLogin();
                                            }
                                        }
                                    } else {
                                        reload();
                                    }
                                    return true;
                                }
                            });
                }
            } else {
                mResult = result;
                if (mNeedDeliverResult) {
                    deliverResult(result);
                    mHasDeliverdResult = true;
                } else {
                    mNeedDeliverResult = true;
                }
                // 如果数据正常，那么停止停止加载进度，显示结果
                if (mProgressNotifiable != null && !hasNextTask()) {
                    mProgressNotifiable.stopLoading(dataExists());
                }
            }
            executeNextTask();
        }

        protected GenericResult parseTaskResult(JSONObject json) {
            GenericResult result = getResultInstance();
            if (result == null) {
                throw new IllegalStateException(
                        "The parsed result should not be null, you must construct" +
                                "a result to indicate the task state");
            }
            try {
            	if(json.has(Tags.HEADER)){
            		if(json.optJSONObject(Tags.HEADER).has(Tags.CODE)){
            			if(json.optJSONObject(Tags.HEADER).optInt(Tags.CODE) == 205){
                    		result.setResultStatus(ResultStatus.IP_ERROR);
                    		return result;
                    	}
            		}
            	}
            	result = parseResult(json, result);
            } catch (Exception e) {
                result.setResultStatus(ResultStatus.DATA_ERROR);
                e.printStackTrace();
            }
            return result;
        }
    }

    /**
     * 获取缓存的key
     */
    protected abstract String getCacheKey();

    /**
     * 缓存信息是否与用户相关，如果相关，那么在切换帐号的时候需要清除缓存
     */
    protected boolean isUserRelated() {
        return false;
    }

    public void saveCacheToDB(String key, String content, String etag) {

        if (TextUtils.isEmpty(content)) {
            return;
        }

        ContentValues values = new ContentValues();
        String cacheKey = getCacheKey();

        values.put(Cache.KEY, cacheKey);
        values.put(Cache.CONTENT, content);
        values.put(Cache.ETAG, etag);
        if (isUserRelated() && LoginManager.getInstance().hasLogin()) {
            values.put(Cache.ACCOUNT_ID, LoginManager.getInstance().getUserId());
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newDelete(Cache.CONTENT_URI);
        builder.withSelection(Cache.KEY + "=?", new String[] {
                key
        });
        operations.add(builder.build());

        builder = ContentProviderOperation.newInsert(Cache.CONTENT_URI);
        builder.withValues(values);
        operations.add(builder.build());

        try {
            getContext().getContentResolver().applyBatch(DBContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            LogUtil.e(TAG, "saveCategoryToDB: failed");
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            LogUtil.e(TAG, "saveCategoryToDB: failed");
            e.printStackTrace();
        }
    }

    /**
     * 从数据库将数据载入缓存
     */

    protected class DatabaseTask extends BaseTask {

        @Override
        protected GenericResult doInBackground(Void... params) {
            GenericResult result = getResultInstance();
            DbCacheItem item = mCache.getItem(getCacheKey());
            if (item != null && item.mEtag != null) {
                mEtag = item.mEtag;
            }
            if (item != null) {
                try {
                    JSONObject json = new JSONObject(item.mContent);
                    GenericResult newResult = parseTaskResult(json);
                    result = onDataLoaded(mResult, newResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        /**
         * 子类可以对获取的数据进行处理，如果该方法返回null，则数据和界面不会更新
         */
        protected GenericResult onDataLoaded(GenericResult oldResult, GenericResult newResult) {
            return newResult;
        }
    }

    /**
     * 从服务器将数据载入缓存和数据库
     */
    protected abstract class UpdateTask extends BaseTask {

        private boolean needSaveToDb;

        public UpdateTask() {
            super();
            this.needSaveToDb = true;
        }

        public UpdateTask(boolean needSaveToDb) {
            super();
            this.needSaveToDb = needSaveToDb;
        }

        /**
         * @return the update task result. Null to indicate our service error.
         */
        @Override
        protected GenericResult doInBackground(Void... params) {
            Request request = getRequest();
            if (mEtag != null) {
                request.addHeader("If-None-Match", mEtag);
            }
            int status = request.getStatus();
            GenericResult result = getResultInstance();
            if (status == Request.STATUS_OK) {
                final String etag = request.getEtag();
                JSONObject mainObject = request.requestJSON();
                GenericResult newResult = parseTaskResult(mainObject);
                result = onDataLoaded(mResult, newResult);
                final String jonsString = mainObject.toString();
                if (needSaveToDb && mNeedDatabase && !TextUtils.isEmpty(getCacheKey())) {
                    ThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            saveCacheToDB(getCacheKey(), jonsString, etag);
                        }
                    });
                }
                return result;
            } else if (status == Request.STATUS_NOT_MODIFIED) {
                LogUtil.d(TAG, "url: " + request.getRequestUrl() + " is NOT MODIFIED");
                mNeedDeliverResult = false;
                result = mResult;
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
         * 获得请求网络数据的Request
         */
        protected abstract Request getRequest();

        /**
         * 子类可以对获取的数据进行处理，如果该方法返回null，则数据和界面不会更新
         */
        protected GenericResult onDataLoaded(GenericResult oldResult, GenericResult newResult) {
            return newResult;
        }
    }
}
