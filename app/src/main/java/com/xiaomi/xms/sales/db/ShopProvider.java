
package com.xiaomi.xms.sales.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.xiaomi.xms.sales.db.DBContract.Cache;
import com.xiaomi.xms.sales.db.DBContract.Category;
import com.xiaomi.xms.sales.db.DBContract.DataStats;
import com.xiaomi.xms.sales.db.DBContract.Region;
import com.xiaomi.xms.sales.db.DatabaseHelper.Tables;
import com.xiaomi.xms.sales.util.LogUtil;

public class ShopProvider extends ContentProvider {
    private static final UriMatcher mMatcher;
    private DatabaseHelper mHelper;
    private static final String TAG = "ShopProvider";

    private static final int URI_CATEGORY = 1;
    private static final int URI_CATEGORY_ITEM = 2;
    private static final int URI_CACHE = 3;
    private static final int URI_CACHE_ITEM = 4;
    private static final int URI_REGION = 5;
    private static final int URI_DATASTATS = 6;
    private static final int URI_REGION_UPDATE = 7;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(DBContract.AUTHORITY, Category.DIRECTORY, URI_CATEGORY);
        mMatcher.addURI(DBContract.AUTHORITY, Category.DIRECTORY + "/#", URI_CATEGORY_ITEM);
        mMatcher.addURI(DBContract.AUTHORITY, Cache.DIRECTORY, URI_CACHE);
        mMatcher.addURI(DBContract.AUTHORITY, Cache.DIRECTORY + "/#", URI_CACHE_ITEM);
        mMatcher.addURI(DBContract.AUTHORITY, Region.DIRECTORY, URI_REGION);
        mMatcher.addURI(DBContract.AUTHORITY, Region.UPDATE_DIRECTORY, URI_REGION_UPDATE);
        mMatcher.addURI(DBContract.AUTHORITY, DataStats.DIRECTORY, URI_DATASTATS);
    }

    @Override
    public boolean onCreate() {
        mHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Cursor c = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        switch (mMatcher.match(uri)) {
            case URI_CATEGORY:
                c = db.query(Tables.CATEGORY, projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case URI_CACHE:
                c = db.query(Tables.CACHE, projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case URI_REGION:
                c = db.query(Tables.REGION, projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case URI_DATASTATS:
                c = db.query(Tables.DATASTATS, projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    private String parseSelection(String selection) {
        return (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        long insertedId = 0;
        switch (mMatcher.match(uri)) {
            case URI_CATEGORY:
                insertedId = db.insert(Tables.CATEGORY, null, values);
                break;
            case URI_CACHE:
                insertedId = db.insert(Tables.CACHE, null, values);
                break;
            case URI_REGION:
                insertedId = db.insert(Tables.REGION, null, values);
                break;
            case URI_DATASTATS:
                insertedId = db.insert(Tables.DATASTATS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (insertedId > 0) {
            getContext().getContentResolver().notifyChange(
                    ContentUris.withAppendedId(uri, insertedId), null);
            LogUtil.d(TAG, "Insert values with uri " + uri.toString());
        }
        return ContentUris.withAppendedId(uri, insertedId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (mMatcher.match(uri)) {
            case URI_CATEGORY:
                count = db.delete(Tables.CATEGORY, selection, selectionArgs);
                break;
            case URI_CATEGORY_ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.delete(Tables.CATEGORY,
                        Category._ID + "=" + id + parseSelection(selection), selectionArgs);
                break;
            case URI_CACHE:
                count = db.delete(Tables.CACHE, selection, selectionArgs);
                break;
            case URI_CACHE_ITEM:
                String key = uri.getPathSegments().get(1);
                count = db.delete(Tables.CACHE,
                        Cache.KEY + "=" + key + parseSelection(selection), selectionArgs);
                break;
            case URI_REGION:
                count = db.delete(Tables.REGION, selection, selectionArgs);
                break;
            case URI_DATASTATS:
                count = db.delete(Tables.DATASTATS, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            LogUtil.d(TAG, "Delete " + count + " rows with uri " + uri.toString());
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (mMatcher.match(uri)) {
            case URI_CATEGORY:
                count = db.update(Tables.CATEGORY, values, selection, selectionArgs);
                break;
            case URI_CATEGORY_ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.update(Tables.CATEGORY, values, Category._ID + "=" + id
                        + parseSelection(selection), selectionArgs);
                break;
            case URI_CACHE:
                count = db.update(Tables.CACHE, values, selection, selectionArgs);
                break;
            case URI_CACHE_ITEM:
                String key = uri.getPathSegments().get(1);
                count = db.update(Tables.CACHE, values, Cache.KEY + "=" + key
                        + parseSelection(selection), selectionArgs);
                break;
            case URI_DATASTATS:
                count = db.update(Tables.DATASTATS, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            LogUtil.d(TAG, "Update " + count + " rows with uri " + uri.toString());
        }
        return count;
    }
}
