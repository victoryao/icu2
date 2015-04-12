
package com.xiaomi.xms.sales.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xiaomi.xms.sales.db.DBContract.Cache;
import com.xiaomi.xms.sales.db.DBContract.DataStats;
import com.xiaomi.xms.sales.db.DBContract.Region;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 6;
    private static final String DB_NAME = "xmssales.db";

    private static DatabaseHelper sInstance;
    private static Context mContext;

    public interface Tables {
        public static final String CACHE = "cache";
        public static final String CATEGORY = "category";
        public static final String REGION = "region";
        public static final String DATASTATS = "data_stats";
    }

    public final class Index {
        public static final String REGION_PARENT = "region_parent";
    }

    public final class DataStatsTypes {
        public static final String REGION = "region_cache";
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context);
            mContext = context;
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Tables.CACHE + "(" +
                Cache.KEY + " TEXT PRIMARY KEY," +
                Cache.CONTENT + " TEXT NOT NULL," +
                Cache.ETAG + " TEXT," +
                Cache.ACCOUNT_ID + " TEXT);"
                );

        db.execSQL("CREATE TABLE " + Tables.REGION + "(" +
                Region._ID + " INTEGER PRIMARY KEY," +
                Region.TYPE + " INTEGER NOT NULL," +
                Region.PARENT + " INTEGER NOT NULL," +
                Region.NAME + " TEXT NOT NULL," +
                Region.ZIPCODE + " INTEGER);");

        db.execSQL("CREATE INDEX IF NOT EXISTS " + Index.REGION_PARENT + " ON " + Tables.REGION
                + " (" + Region.PARENT + ");");

        db.execSQL("CREATE TABLE " + Tables.DATASTATS + "(" +
                DataStats._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DataStats.TYPE + " TEXT NOT NULL," +
                DataStats.STATS + " TEXT NOT NULL);");

        // load region cache into database from raw file
        // must after Tables.DATASTATS created
        RegionDBHelper.getInstance(mContext).initialDB(db);

    }

    // 覆盖API level 11及以上系统默认实现(默认实现会抛出SQLiteException)
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
