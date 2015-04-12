
package com.xiaomi.xms.sales.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.db.DBContract.DataMimeType;
import com.xiaomi.xms.sales.db.DBContract.DataStats;
import com.xiaomi.xms.sales.db.DBContract.Region;
import com.xiaomi.xms.sales.db.DatabaseHelper.Tables;
import com.xiaomi.xms.sales.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegionDBHelper {

    private static final String TAG = "RegionDBHelper";
    private static final String CONTENTS_SEP = "\t";
    private static final String CONFIG_SEP = ":";

    public final class HEADER {
        public static final String VERSION = "VERSION";
    }

    private static RegionDBHelper mInstance = null;
    private Context mContext;

    public static synchronized RegionDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RegionDBHelper(context);
        }
        return mInstance;
    }

    private RegionDBHelper(Context context) {
        mContext = context;
    }

    private Map<String, String> readConfig(BufferedReader br) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        String line;
        while (null != (line = br.readLine())) {
            if (TextUtils.equals(line, "")) {
                break;
            }
            String[] fields = line.split(CONFIG_SEP);
            if (fields.length != 2) {
                continue;
            }
            map.put(fields[0], fields[1]);
        }
        return map;
    }

    private ArrayList<ContentValues> readContents(BufferedReader br) throws IOException {
        ArrayList<ContentValues> contents = new ArrayList<ContentValues>();
        String line;
        while (null != (line = br.readLine())) {
            String[] fields = line.split(CONTENTS_SEP);
            if (fields.length != 5) {
                continue;
            }
            ContentValues content = new ContentValues();
            content.put(Region.TYPE, fields[3]);
            content.put(Region.PARENT, fields[1]);
            content.put(Region._ID, fields[0]);
            content.put(Region.NAME, fields[2]);
            content.put(Region.ZIPCODE, fields[4]);
            contents.add(content);
        }
        return contents;
    }

    public RegionInfo readFromResource() {
        RegionInfo region = new RegionInfo();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(mContext.getResources()
                    .openRawResource(R.raw.region), "UTF8"));
            region.configs = readConfig(br);
            region.contents = readContents(br);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return region;
    }

    public boolean initialDB(SQLiteDatabase db) {
        LogUtil.d(TAG, "init region db.");
        RegionInfo regions = readFromResource();
        for (ContentValues content : regions.contents) {
            db.insert(Tables.REGION, null, content);
        }
        // update etag database
        ContentValues first = new ContentValues();
        first.put(DataStats.TYPE, DataMimeType.REGION);
        first.put(DataStats.STATS, regions.configs.get(HEADER.VERSION));
        db.insert(Tables.DATASTATS, null, first);
        return true;
    }

    public class RegionInfo {
        public ArrayList<ContentValues> contents = null;
        public Map<String, String> configs = null;
    }

    public boolean updateRegionDB(RegionInfo regions) {
        SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getWritableDatabase();
        mContext.getContentResolver().delete(Region.CONTENT_URI, null, null);
        db.beginTransaction();
        for (ContentValues content : regions.contents) {
            db.insert(Tables.REGION, null, content);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        // update etag database
        ContentValues first = new ContentValues();
        first.put(DataStats.TYPE, DataMimeType.REGION);
        first.put(DataStats.STATS, regions.configs.get(HEADER.VERSION));
        db.update(Tables.DATASTATS, first,
                DataStats.TYPE + "='" + DataMimeType.REGION + "'", null);
        return true;
    }
}
