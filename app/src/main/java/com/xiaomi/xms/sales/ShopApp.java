
package com.xiaomi.xms.sales;

import android.app.Application;
import android.content.Context;

import com.xiaomi.xms.sales.cache.DbCache;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.ImageUtil;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ThreadPool;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.xmsf.account.LoginManager.AccountListener;

public class ShopApp extends Application implements AccountListener {
    private static final String TAG = "ShopApp";

    public static final String APP_ID = "1000276";
    public static final String APP_TOKEN = "480100055276";
    public static final String APP_PACKAGENAME = "com.xiaomi.xms.sales";
    public static boolean DEBUG = false;
    public static final String PREF_USER_DEBUG = "pref_user_debug";

    private static Context sContext;

    private static boolean sIsCookiesInited;
    
    private static ShopApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        ImageLoader.init(sContext);
        ImageUtil.initProcessor(sContext);
        Device.init(sContext);
        LoginManager.init(sContext);
        LoginManager.getInstance().addLoginListener(this);
        
        sInstance = this;
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);
    }

    public static ShopApp getInstance(){
		return sInstance;
	}
    
    public static Context getContext() {
        return sContext;
    }

    public static boolean isUserDebug() {
        return ShopApp.DEBUG && Utils.Preference.getBooleanPref(sContext,
                PREF_USER_DEBUG,
                true);
    }

    public static void setUserPrefDebug(boolean debug) {
        Utils.Preference.setBooleanPref(sContext, PREF_USER_DEBUG,
                debug);
    }

    public static void initCookiesIfHaveNot() {
        if (!sIsCookiesInited) {
            sIsCookiesInited = true;
            initCookies();
        }
    }

    /**
     * 为webview初始化全局cookie
     */
    private static void initCookies() {
        // 在cookie里加入标识，当webview访问m.xiaomi.com时返回特定页面
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                HostManager.initSettingCookies(sContext);
                HostManager.setLoginCookies(sContext);
            }
        });

    }

    @Override
    public void onLogin(String userId, String authToken, String security) {
        LogUtil.d(TAG, "onLogin");
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                HostManager.setLoginCookies(sContext);
            }
        });
    }

    @Override
    public void onInvalidAuthonToken() {
        LogUtil.d(TAG, "invalid authon token");
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                HostManager.removeLoginCookies(sContext);
                HostManager.setLoginCookies(sContext);
            }
        });
    }

    @Override
    public void onLogout() {
        LogUtil.d(TAG, "onLogout");
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                HostManager.removeLoginCookies(sContext);
                new DbCache(sContext).deleteUserRelatedItem();
                Utils.Preference.removePref(sContext, Constants.Prefence.PREF_NO_CHANCE);
            }
        });
    }
}
