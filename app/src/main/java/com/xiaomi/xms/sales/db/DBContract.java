
package com.xiaomi.xms.sales.db;

import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Pair;

public class DBContract {
    public static final String AUTHORITY = "com.xiaomi.xms.sales";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Category implements BaseColumns {
        public static final String DIRECTORY = "category";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                AUTHORITY_URI, DIRECTORY);

        public static final String CATEGORY_ID = "category_id";
        public static final String NAME = "category_name";
        public static final String PHOTO_LOCAL_PATH = "photo_local_path";
        public static final String PHOTO_REMOTE_PATH = "photo_remote_path";
        public static final String TOTAL_COUNT = "total_count";

        private Category() {
        };
    }

    public static final class Cache implements BaseColumns {
        public static final String DIRECTORY = "cache";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                AUTHORITY_URI, DIRECTORY);

        public static final String KEY = "key";
        public static final String CONTENT = "content";
        public static final String ETAG = "etag";
        public static final String ACCOUNT_ID = "account_id";

        private Cache() {
        };
    }

    public static final class Region implements BaseColumns {
        public static final String DIRECTORY = "region";
        public static final String UPDATE_DIRECTORY = "update_region";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, DIRECTORY);
        public static final Uri UPDATE_URI = Uri.withAppendedPath(AUTHORITY_URI, UPDATE_DIRECTORY);

        public static final String TYPE = "type";
        public static final String PARENT = "parent";
        public static final String NAME = "name";
        public static final String ZIPCODE = "zipcode";
    }

    public static final class DataStats implements BaseColumns {
        public static final String DIRECTORY = "data_stats";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, DIRECTORY);

        public static final String TYPE = "type";
        public static final String STATS = "stats";
    }

    public static final class DataMimeType implements BaseColumns {
        public static final String REGION = "RegionCache";
        public static final String SERVICE_TOKEN = "service_token";

        public static String formatWaterMark(String name, String value) {
            return name + ":" + value;
        }

        public static String formatServiceToken(String sid, String serviceToken) {
            return sid + ":" + serviceToken;
        }

        public static String parseWaterMark(String value) {
            if (!TextUtils.isEmpty(value)) {
                String[] data = value.split(":");
                if (data != null && data.length == 2) {
                    return data[1];
                }
            }
            return null;
        }

        public static Pair<String, String> parseServiceToken(String value) {
            Pair pair = null;
            if (!TextUtils.isEmpty(value)) {
                String[] data = value.split(":");
                if (data != null && data.length == 2) {
                    pair = new Pair<String, String>(data[0], data[1]);
                }
            }
            return pair;
        }
    }
}
