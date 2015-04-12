package com.xiaomi.xms.sales.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.xiaomi.xms.sales.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

public class Device {
    private static final String TAG = "device";

    /** 设备宽度（像素） */
    public static int DISPLAY_WIDTH;

    /** 设备高度（像素） */
    public static int DISPLAY_HEIGHT;

    /** 设备的高度*设备的宽度 */
    public static String DISPLAY_RESOLUTION;

    /** 设备的像素密度dpi */
    public static int DISPLAY_DENSITY;

    /** 用户可见的设备名，例如Nexus One */
    public static String MODEL;

    /** 内部设备名，例如Nexus One是passion */
    public static String DEVICE;

    /** 产品名 */
    public static String PRODUCT;

    /** 主板名 */
    public static String BOARD;

    /** 硬件名，从内核命令行获取，例如qcom */
    public static String HARDWARE;

    /** 生产商 */
    public static String MANUFACTURER;

    /** 软件定制品牌 */
    public static String BRAND;

    /** build类型，例如user/eng */
    public static String BUILD_TYPE;

    /** 系统的sdk版本号 */
    public static int SDK_VERSION;

    /** 系统的版本号（MIUI），例如2.7.13*/
    public static String SYSTEM_VERSION;

    /** 系统的版本号（Android），例如4.0.4 */
    public static String RELEASE;

    /** 是否MIUI系统 */
    public static boolean IS_MIUI;

    /** 商城的package name **/
    public static String PACKAGE;

    /** 商城的version code */
    public static int SHOP_VERSION;

    /** 商城的version name */
    public static String SHOP_VERSION_STRING;

    /** 商城是否系统应用 */
    public static boolean IS_SYSTEM_SHOP;

    /** 设置中的国家 */
    public static String COUNTRY;

    /** 设置中的语言 */
    public static String LANGUAGE;

    /** Sim卡的运营商 */
    public static String CARRIER;

    /** 设备号 */
    public static String UUID;

    /** IMEI号 */
    public static String IMEI;

    /** 渠道号 */
    public static String CHANNEL_ID;

    /** 是否为新安装用户 **/
    public static boolean IS_NEW_USER;

    private static final String KEY_INSTALL_TIME = "installTime";

    private static final long NEW_USER_TIME = 7 * 24 * 60 * 60 * 1000; // 默认安装7天以内为新用户

    public static void init(Context context) {
        acquireScreenAttr(context);
        acquireSystemInfo(context);
        acquireShopInfo(context);
        acquireUserInfo(context);
        acquireIdentity(context);
        acquireIsNewUser(context);
    }

    public static String getClientInfoHash() {
        return Coder.encodeMD5(getFullInfo());
    }

    public static String getFullInfo() {
        return DISPLAY_RESOLUTION + DISPLAY_WIDTH +
        DISPLAY_HEIGHT + DISPLAY_DENSITY + MODEL + DEVICE + PRODUCT +
        BOARD + HARDWARE + MANUFACTURER + BRAND + BUILD_TYPE +
        SDK_VERSION + SYSTEM_VERSION + RELEASE + IS_MIUI +
        SHOP_VERSION + SHOP_VERSION_STRING + IS_SYSTEM_SHOP +
        COUNTRY + LANGUAGE + CARRIER +
        UUID + IMEI + CHANNEL_ID;
    }

    private static void acquireScreenAttr(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        DISPLAY_HEIGHT = dm.heightPixels;
        DISPLAY_WIDTH = dm.widthPixels;
        DISPLAY_RESOLUTION = DISPLAY_HEIGHT + "*" + DISPLAY_WIDTH;
        DISPLAY_DENSITY = dm.densityDpi;
    }

    private static void acquireSystemInfo(Context context) {
        MODEL = Build.MODEL;
        DEVICE = Build.DEVICE;
        PRODUCT = Build.PRODUCT;
        BOARD = Build.BOARD;
        HARDWARE = Build.HARDWARE;
        MANUFACTURER = Build.MANUFACTURER;
        BRAND = Build.BRAND;
        BUILD_TYPE = Build.TYPE;

        RELEASE = Build.VERSION.RELEASE;
        SYSTEM_VERSION = Build.VERSION.INCREMENTAL;
        SDK_VERSION = Build.VERSION.SDK_INT;
        IS_MIUI = isMiui(context);
    }

    private static void acquireShopInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            PACKAGE = pkgInfo.packageName;
            SHOP_VERSION = pkgInfo.versionCode;
            SHOP_VERSION_STRING = pkgInfo.versionName;
            IS_SYSTEM_SHOP = (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (NameNotFoundException e) {
            SHOP_VERSION = 0;
            SHOP_VERSION_STRING = "";
            IS_SYSTEM_SHOP = false;
        }
    }

    private static void acquireUserInfo(Context context) {
        COUNTRY = Locale.getDefault().getCountry();
        LANGUAGE = Locale.getDefault().getLanguage();
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        CARRIER = telManager.getSimOperator();
    }

    private static void acquireIdentity(Context context) {
        // device id
        if (context != null) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = tm.getDeviceId();

            if (TextUtils.isEmpty(deviceId)) {
                deviceId = "";
            }
            IMEI = deviceId;

            // mac
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String mac = info.getMacAddress();

            StringBuffer uuid = new StringBuffer();
            if (!TextUtils.isEmpty(deviceId)) {
                uuid.append(deviceId);
            }
            if (!TextUtils.isEmpty(mac)) {
                uuid.append("_");
                uuid.append(mac);
            }

            UUID = Coder.encodeMD5(uuid.toString());
            CHANNEL_ID = context.getString(R.string.channel_id);
        }
    }

    private static void acquireIsNewUser(Context context) {
        long installTime = Utils.Preference.getLongPref(context, KEY_INSTALL_TIME, 0);
        long nowTime = System.currentTimeMillis();
        if (installTime <= 0 || nowTime - installTime < 0) {
            IS_NEW_USER = true;
            Utils.Preference.setLongPref(context, KEY_INSTALL_TIME, nowTime);
        } else if (nowTime - installTime < NEW_USER_TIME) {
            IS_NEW_USER = true;
        } else {
            IS_NEW_USER = false;
        }
    }
    /**
     * 通过判断CloudService是否存在来判断是否是miui，小米账户如果不存在的话认为不是miui
     */
    private static boolean isMiui(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo("com.miui.cloudservice", 0);
            return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LogUtil.e(TAG, e.toString());
        }
        return null;
    }
}
