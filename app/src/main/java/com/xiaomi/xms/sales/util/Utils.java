
package com.xiaomi.xms.sales.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.xiaomi.xms.sales.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final class Network {
        public static boolean isNetWorkConnected(Context context) {
            ConnectivityManager connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }

        public static int getActiveNetworkType(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null) {
                    return info.getType();
                }
            }
            return -1;
        }

        public static boolean isWifiConnected(Context context) {
            ConnectivityManager connManager = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            return networkInfo != null &&
                    networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }

        public static String getWifiSSID(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getSSID();
        }

        public static boolean isMobileConnected(Context context) {
            ConnectivityManager connManager = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            return networkInfo != null &&
                    networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
    }

    public static final class Preference {
        public static void setLongPref(Context context, String key, Long value) {
            if (context == null) {
                return;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                Editor editor = pref.edit();
                if (editor != null) {
                    editor.putLong(key, value);
                    editor.commit();
                }
            }
        }

        public static long getLongPref(Context context, String key, long defaultValue) {
            if (context == null) {
                return defaultValue;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                return pref.getLong(key, defaultValue);
            }
            return defaultValue;
        }

        public static void setStringPref(Context context, String key, String value) {
            if (context == null) {
                return;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                Editor editor = pref.edit();
                if (editor != null) {
                    editor.putString(key, value);
                    editor.commit();
                }
            }
        }

        public static String getStringPref(Context context, String key, String defaultValue) {
            if (context == null) {
                return defaultValue;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                return pref.getString(key, defaultValue);
            }
            return defaultValue;
        }

        public static boolean getBooleanPref(Context context, String key, boolean defaultValue) {
            if (context == null) {
                return defaultValue;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                return pref.getBoolean(key, defaultValue);
            }
            return defaultValue;
        }

        public static void setBooleanPref(Context context, String key, boolean value) {
            if (context == null) {
                return;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                Editor editor = pref.edit();
                if (editor != null) {
                    editor.putBoolean(key, value);
                    editor.commit();
                }
            }
        }

        public static void removePref(Context context, String key) {
            if (context == null) {
                return;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                Editor editor = pref.edit();
                if (editor != null) {
                    editor.remove(key);
                    editor.commit();
                }
            }
        }

        public static ArrayList<String> getAllPreferenceKey(Context context) {
            if (context == null) {
                return null;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (pref != null) {
                Map<String, ?> map = pref.getAll();
                Iterator it = map.keySet().iterator();
                ArrayList<String> list = new ArrayList<String>();
                while (it.hasNext()) {
                    list.add(it.next().toString());
                }
                return list;
            }
            return null;
        }
    }

    public static final class SoftInput {
        public static void hide(Context context, IBinder windowToken) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(windowToken, 0);
            }
        }

        public static void show(Context context, View view) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, 0);
            }
        }
    }

    public static final class DateTime {
        private static final int MILLSECONDS = 1000;

        public static String formatTime(Context context, String timeSeconds) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    context.getString(R.string.repair_progress_date_format_deafault));
            return sdf.format(new Date(Long.parseLong(timeSeconds) * MILLSECONDS));
        }

        /**
         * return a day string of today, using functin "formatDate"
         * 
         * @param context
         * @return
         */
        public static String formatToday(Context context) {
            return formatDate(context, String.valueOf((new Date()).getTime() / MILLSECONDS));
        }

        /**
         * return a string of timeSeconds, as the format "YYYY-MM-DD"
         * 
         * @param context
         * @param timeSeconds
         * @return string of date
         */
        public static String formatDate(Context context, String timeSeconds) {
            Time time = new Time();
            time.set(Long.parseLong(timeSeconds) * MILLSECONDS);
            return time.format(context.getString(R.string.order_date_format));
        }

        public static String getMonth(String timeSeconds) {
            Time time = new Time();
            time.set(Long.parseLong(timeSeconds) * MILLSECONDS);
            return String.valueOf(time.month + 1);
        }

        public static String getDayOfMonth(String timeSeconds) {
            Time time = new Time();
            time.set(Long.parseLong(timeSeconds) * MILLSECONDS);
            return String.valueOf(time.monthDay);
        }

        /**
         * 把yyyy-MM-dd HH:mm:ss 格式的日期转化为 yyyy年MM月dd日
         * 
         * @param dateStr
         * @return
         */
        public static String formatDateString(Context context, String dateStr) {
            Date date = formatStringToDate(
                    context.getString(R.string.repair_progress_date_format_deafault), dateStr);
            String newDateStr = dateStr;
            if (date != null) {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        context.getString(R.string.repair_progress_date_format));
                newDateStr = formatter.format(date);
            }

            return newDateStr;
        }

        /**
         * 将yyyy-MM-dd HH:mm:ss 时间格式字符串转化成时间
         * 
         * @param dateStr
         * @return
         * @throws ParseException
         */
        public static Date formatStringToDate(String formatStr, String dateStr) {
            SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
            Date newDateStr = null;
            try {
                newDateStr = formatter.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return newDateStr;
        }

    }

    public static final class Money {
        public static String valueOf(double value) {
            int valueInt = (int) value;
            if (valueInt == value) {
                return String.valueOf(valueInt);
            } else {
                return String.valueOf(value);
            }
        }
    }

    public static final class PhoneFormat {

        public static String valueOf(String phone) {
            if (!TextUtils.isEmpty(phone)) {
                Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
                Matcher m = p.matcher(phone);
                if (m.matches()) {
                    phone = phone.substring(0, 3) + "****" + phone.substring(7);
                }
                return phone;
            }
            return phone;
        }
    }

    public static final class Video {

        /**
         * Invoke player to play video.
         * 
         * @param context context used to start player activity
         * @param uriString video uri, like
         *            "http://forum.ea3w.com/coll_ea3w/attach/2008_10/12237832415.3gp"
         * @return true if success, false otherwise
         */
        public static boolean playVideo(Context context, String uriString) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = "video/*";
            try {
                Uri uri = Uri.parse(uriString);
                intent.setDataAndType(uri, type);
                context.startActivity(intent);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }
}
