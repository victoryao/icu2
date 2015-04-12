
package com.xiaomi.xms.sales.cache;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.xiaomi.xms.sales.db.DBContract;
import com.xiaomi.xms.sales.db.DBContract.Cache;
import com.xiaomi.xms.sales.util.LogUtil;

import java.util.ArrayList;

public class DbCache {

    private static final String TAG = "DbCache";
    private Context mContext;

    private static final int COLUMNS_KEY = 0;
    private static final int COLUMNS_CONTENT = 1;
    private static final int COLUMNS_ETAG = 2;

    public DbCache(Context context) {
        mContext = context;
    }

    // TODO 这个函数可以进一步优化
    /**
     * 依据key更新缓存数据，无论数据以前是否存在都先删除原有key下的数据，然后添加新行
     *
     * @param key
     * @param content
     * @param etag
     */
    public void setItem(String key, String content, String etag) {

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newDelete(Cache.CONTENT_URI)
                .withSelection("/?", new String[] {
                        key
                })
                .build());

        ContentValues values = new ContentValues();
        values.put(Cache.KEY, key);
        values.put(Cache.CONTENT, content);
        values.put(Cache.ETAG, etag);
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(Cache.CONTENT_URI);
        builder.withValues(values);
        operations.add(builder.build());

        try {
            mContext.getContentResolver().applyBatch(DBContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            LogUtil.e(TAG, "saveCacheToDb: failed");
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            LogUtil.e(TAG, "saveCacheToDb: failed");
            e.printStackTrace();
        }

    }

    public void deleteItem(String key) {
        mContext.getContentResolver().delete(Cache.CONTENT_URI, Cache.KEY + " = ?",
                new String[] {
                    key
                });
    }

    public void deleteUserRelatedItem() {
        mContext.getContentResolver().delete(Cache.CONTENT_URI, Cache.ACCOUNT_ID + " IS NOT NULL",
                null);
    }

    /**
     * 获取item
     *
     * @param key
     * @return
     */
    public DbCacheItem getItem(String key) {
        DbCacheItem item = null;
        Cursor cursor = mContext.getContentResolver().query(
                Cache.CONTENT_URI, null, Cache.KEY + "=?", new String[] {
                    key
                },
                null);
        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            item = new DbCacheItem();
            item.mKey = cursor.getString(COLUMNS_KEY);
            item.mContent = cursor.getString(COLUMNS_CONTENT);
            item.mEtag = cursor.getString(COLUMNS_ETAG);
        } finally {
            cursor.close();
        }
        return item;
    }

    public final class DbCacheItem {
        public String mKey;
        public String mContent;
        public String mEtag;
    }
}
