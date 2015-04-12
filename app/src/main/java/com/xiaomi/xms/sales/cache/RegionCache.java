
package com.xiaomi.xms.sales.cache;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;

import com.xiaomi.xms.sales.db.DBContract.Region;
import com.xiaomi.xms.sales.model.Tags;

import java.util.concurrent.CopyOnWriteArrayList;

public class RegionCache extends AsyncQueryHandler {

    private static RegionCache mInstance;
    private static int mTokenId = -1;

    private Context mContext;

    public static synchronized RegionCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RegionCache(context);
        }
        return mInstance;
    }

    public RegionCache(Context context) {
        super(context.getContentResolver());
        mContext = context;
    }

    public interface QueryCallback {
        public void queryComplete(Cursor cursor);
    }

    private CopyOnWriteArrayList<QueryCallback> mCallbackList = new CopyOnWriteArrayList<QueryCallback>();

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (mCallbackList.size() <= token) {
            return;
        }
        QueryCallback callback = mCallbackList.get(token);
        if (callback != null) {
            callback.queryComplete(cursor);
        }
    }

    public void getProvince(QueryCallback callback) {
        mCallbackList.add(callback);
        startQuery(mCallbackList.size() - 1, null, Region.CONTENT_URI, null, Region.PARENT
                + "=" + Tags.AddressInfo.CHINA_ID + " and " + Region.NAME
                + " not in ('香港','澳门','台湾')", null, null);// TODO:暂时代码屏蔽，待后续改为通过接口更新
    }

    public Cursor getProvince() {
        return mContext.getContentResolver().query(Region.CONTENT_URI, null,
                Region.PARENT + "=" + Tags.AddressInfo.CHINA_ID,
                null, null);
    }

    public void getCityByProvinceId(long id, QueryCallback callback) {
        mCallbackList.add(callback);
        startQuery(mCallbackList.size() - 1, null, Region.CONTENT_URI, null, Region.PARENT
                + "=" + id, null, null);
    }

    public Cursor getCityByProvinceId(long id) {
        return mContext.getContentResolver().query(Region.CONTENT_URI, null,
                Region.PARENT + "=" + id, null, null);
    }

    public void getDistrictByCityId(long id, QueryCallback callback) {
        mCallbackList.add(callback);
        startQuery(mCallbackList.size() - 1, null, Region.CONTENT_URI, null, Region.PARENT
                + "=" + id, null, null);
    }

    public Cursor getDistrictByCityId(long id) {
        return mContext.getContentResolver().query(Region.CONTENT_URI, null,
                Region.PARENT + "=" + id, null, null);
    }

    public String getNameById(int id) {
        Cursor cursor = mContext.getContentResolver().query(Region.CONTENT_URI, null,
                Region._ID + "=" + id, null, null);
        return cursor.getString(cursor.getColumnIndex(Region.NAME));
    }

    public String getZipCodeById(int id) {
        Cursor cursor = mContext.getContentResolver().query(Region.CONTENT_URI, null,
                Region._ID + "=" + id, null, null);
        return cursor.getString(cursor.getColumnIndex(Region.ZIPCODE));
    }

    public void getZipCodeById(long id, QueryCallback callback) {
        mCallbackList.add(callback);
        startQuery(mCallbackList.size() - 1, null, Region.CONTENT_URI, null,
                Region._ID + "=" + id, null, null);
    }

    public void updateFromResource(QueryCallback callback) {
        mCallbackList.add(callback);
        startUpdate(mCallbackList.size() - 1, null, Region.UPDATE_URI, null, null, null);
    }
}
