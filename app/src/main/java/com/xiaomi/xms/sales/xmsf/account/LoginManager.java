
package com.xiaomi.xms.sales.xmsf.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.xiaomi.xms.sales.UploadLogService;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.db.DBContract.DataMimeType;
import com.xiaomi.xms.sales.db.DBContract.DataStats;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.ExtendedAuthToken;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.data.AccountInfo;
import com.xiaomi.xms.sales.xmsf.account.exception.AccessDeniedException;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidCredentialException;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidResponseException;
import com.xiaomi.xms.sales.xmsf.account.utils.CloudHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class LoginManager {
    private static final String TAG = "ShopLoginManager";

    private Context mContext;
    private AccountManager mAccountManager;

    private LoginCallback mLoginCallback;
    private LogoutCallback mLogoutCallback;
    private HashSet<AccountListener> mAccountLsteners;

    private static LoginManager sLoginManager;

    public static final int STATUS_SYSTEM_LOGIN = 1;
    public static final int STATUS_SYSTEM_LOGOUT = 2;
    public static final int STATUS_LOCAL_LOGIN = 3;
    public static final int STATUS_LOCAL_LOGOUT = 4;
    public static final int STATUS_SYSTEM_UNACTIVE = 5;

    public static final int ERROR_LOGIN_COMMON = 0;
    public static final int ERROR_LOGIN_USER_CANCELED = 1;
    public static final int ERROR_LOGIN_NO_ACCOUNT = 2;
    public static final int ERROR_LOGIN_NETWORK = 3;
    public static final int ERROR_LOGIN_SERVER = 4;
    public static final int ERROR_LOGIN_UNACTIVE = 5;

    private LoginManager(Context context) {
        mContext = context;
        mAccountManager = AccountManager.get(context);
        mAccountLsteners = new HashSet<AccountListener>();

        // 注册账户切换的监听器
        context.registerReceiver(mAccountChangedReceiver, new IntentFilter(
                Constants.Account.ACTION_LOGIN_ACCOUNTS_PRE_CHANGED));
    }

    @Override
    protected void finalize() throws Throwable {
        mContext.unregisterReceiver(mAccountChangedReceiver);
    }

    public static void init(Context context) {
        if (sLoginManager == null) {
            sLoginManager = new LoginManager(context);
        }
    }

    public static LoginManager getInstance() {
        return sLoginManager;
    }

    public String getUserId() {
        String userId = null;
        if (Utils.Preference.getBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM, false)) {
            userId = Utils.Preference.getStringPref(mContext, Constants.Account.PREF_SYSTEM_UID, null);
        } else {
            userId = Utils.Preference.getStringPref(mContext, Constants.Account.PREF_UID, null);
        }
        return userId;
    }

    public ExtendedAuthToken getExtendedAuthToken(String sid) {
        String extendedAuthToken = null;
        // 如果使用的是系统登录
        if (Utils.Preference.getBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM, false)) {
            if (TextUtils.equals(sid, Constants.Account.DEFAULT_SERVICE_ID)) {
                extendedAuthToken = Utils.Preference.getStringPref(mContext,
                        Constants.Account.PREF_SYSTEM_EXTENDED_TOKEN, "");
                if (TextUtils.isEmpty(extendedAuthToken)) {
                    extendedAuthToken = getSystemAccountAuthToken(sid);
                }
            } else {
                extendedAuthToken = getSystemAccountAuthToken(sid);
            }

        } else {
            if (TextUtils.equals(sid, Constants.Account.DEFAULT_SERVICE_ID)) {
                extendedAuthToken = Utils.Preference.getStringPref(mContext,
                        Constants.Account.PREF_EXTENDED_TOKEN, "");
                if (TextUtils.isEmpty(extendedAuthToken)) {
                    extendedAuthToken = getLocalAccountExtendedAuthToken(sid);
                }
            } else {
                extendedAuthToken = getLocalAccountExtendedAuthToken(sid);
            }
        }

        return TextUtils.isEmpty(extendedAuthToken) ? null : ExtendedAuthToken
                .parse(extendedAuthToken);
    }

    public void setSystemLogin(boolean isLoginSystem) {
        Utils.Preference.setBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM, isLoginSystem);
    }

    public synchronized void addLoginListener(AccountListener listener) {
        if (listener == null) {
            return;
        }
        if (mAccountLsteners == null) {
            mAccountLsteners = new HashSet<AccountListener>();
        }
        if (!mAccountLsteners.contains(listener)) {
            mAccountLsteners.add(listener);
        }
    }

    public synchronized void removeLoginListener(AccountListener listener) {
        if (listener == null) {
            return;
        }
        if (mAccountLsteners != null) {
            mAccountLsteners.remove(listener);
        }
    }

    /**
     * for internal use
     */
    public void onAccountLoginSucceed(String userId, String authToken, String security) {
        if (mLoginCallback != null) {
            mLoginCallback.onLoginSucceed(userId, authToken, security);
        }
        if (mAccountLsteners != null && !mAccountLsteners.isEmpty()) {
            for (AccountListener listener : mAccountLsteners) {
                listener.onLogin(userId, authToken, security);
            }
        }
        Log.d(TAG, "account has login:" + userId);
    }

    /**
     * for internal use
     */
    public void onAccountLoginFailed(int error) {
        if (mLoginCallback != null) {
            mLoginCallback.onLoginFailed(error);
        }
        Log.d(TAG, "account login failed: " + error);
    }

    /**
     * for internal use
     */
    private void onAccountLogout() {
        if (mLogoutCallback != null) {
            mLogoutCallback.onLogout();
        }
        if (mAccountLsteners != null && !mAccountLsteners.isEmpty()) {
            for (AccountListener listener : mAccountLsteners) {
                listener.onLogout();
            }
        }
        Log.d(TAG, "account has logout");
    }

    /**
     * 是否登录
     * 
     * @return boolean
     */
    public boolean hasLogin() {
        String userId;
        String extendedAuthToken;
        if (Utils.Preference.getBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM, false)) {
            userId = getSystemAccountId();
            if (!TextUtils.isEmpty(userId)) {
                return true;
            }
            return false;
        } else {
            userId = Utils.Preference.getStringPref(mContext, Constants.Account.PREF_UID, "");
            extendedAuthToken = Utils.Preference.getStringPref(mContext,
                    Constants.Account.PREF_EXTENDED_TOKEN, "");
        }
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(extendedAuthToken)) {
            return true;
        }

        return false;
    }

    public boolean hasSystemAccount() {
        return !TextUtils.isEmpty(getSystemAccountId());
    }

    public String getSystemAccountId() {
        Account[] account = mAccountManager.getAccountsByType(Constants.Account.ACCOUNT_TYPE);
        return account.length > 0 ? account[0].name : null;
    }

    public String getPassToken() {
        if (hasLogin()
                && !Utils.Preference.getBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM,
                        false)) {
            return Utils.Preference.getStringPref(mContext, Constants.Account.PREF_PASS_TOKEN, "");
        }
        return null;
    }

    public void invalidAuthToken() {
        if (Utils.Preference.getBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM, false)) {
            Utils.Preference.removePref(mContext, Constants.Account.PREF_EXTENDED_TOKEN);
        } else {
            Utils.Preference.removePref(mContext, Constants.Account.PREF_SYSTEM_EXTENDED_TOKEN);
        }
        if (mAccountLsteners != null && !mAccountLsteners.isEmpty()) {
            for (AccountListener listener : mAccountLsteners) {
                listener.onInvalidAuthonToken();
            }
        }
    }

    private String getLocalAccountExtendedAuthToken(String sid) {
        String userId = Utils.Preference.getStringPref(mContext, Constants.Account.PREF_UID, "");
        String passToken = Utils.Preference.getStringPref(mContext, Constants.Account.PREF_PASS_TOKEN, "");
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(passToken)) {
            return null;
        }

        try {
            AccountInfo account = CloudHelper.getServiceTokenByPassToken(userId, passToken, sid);
            String extendedAuthToken = ExtendedAuthToken.build(account.getSecurity(),
                    account.getSecurity()).toPlain();
            Utils.Preference.setStringPref(mContext, Constants.Account.PREF_EXTENDED_TOKEN,
                    extendedAuthToken);
            return extendedAuthToken;
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (InvalidCredentialException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSystemAccountAuthToken(String sid) {
        Account[] account = mAccountManager.getAccountsByType(Constants.Account.ACCOUNT_TYPE);
        if (account.length > 0) {
            AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account[0], sid, null,
                    null, null, null);
            try {
                if (future != null) {
                    Bundle result = future.getResult();
                    if (result != null) {
                        String extendedAuthToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                        if (!TextUtils.isEmpty(extendedAuthToken)) {
                            return extendedAuthToken;
                        }
                    }
                }
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void loginSystem(String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            LogUtil.d(TAG, "system login");
            setSystemLogin(true);
            String uid = getSystemAccountId();
            ExtendedAuthToken token = ExtendedAuthToken.parse(authToken);
            Utils.Preference.setStringPref(mContext, Constants.Account.PREF_SYSTEM_UID, uid);
            Utils.Preference.setStringPref(mContext, Constants.Account.PREF_SYSTEM_EXTENDED_TOKEN,
                    authToken);
            onAccountLoginSucceed(uid, token.authToken, token.security);
        }
    }

    public void removeLoginCallback() {
        this.mLoginCallback = null;
    }

    public void logout() {
        logout(null);
    }

    /**
     * 登出本地账户，该方法会刷新LoginManager中的缓存数据
     */
    public void logout(LogoutCallback logoutCallback) {
        this.mLogoutCallback = logoutCallback;

        // 删除所有的登录信息
        Utils.Preference.removePref(mContext, Constants.Account.PREF_UID);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_EXTENDED_TOKEN);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_PASS_TOKEN);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_SYSTEM_UID);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_SYSTEM_EXTENDED_TOKEN);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_LOGIN_SYSTEM);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_USER_ORGID);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_USER_NAME);
        Utils.Preference.removePref(mContext, Constants.Account.PREF_USER_ORGNAME);
        Intent intent = new Intent(mContext, UploadLogService.class);
        mContext.stopService(intent);
        onAccountLogout();
    }

    private BroadcastReceiver mAccountChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, Constants.Account.ACTION_LOGIN_ACCOUNTS_PRE_CHANGED)) {
                int type = intent.getIntExtra(Constants.Account.EXTRA_UPDATE_TYPE, -1);
                Account account = intent.getParcelableExtra(Constants.Account.EXTRA_ACCOUNT);
                if (!TextUtils.equals(account.type, Constants.Account.ACCOUNT_TYPE)) {
                    return;
                }
                if (type == Constants.Account.TYPE_ADD) {
                    // TODO:系统账户增加时，可以问用户是否登录
                } else if (type == Constants.Account.TYPE_REMOVE) {
                    // 系统账户删除时自动登出
                    Utils.Preference.removePref(mContext, Constants.Account.PREF_SYSTEM_UID);
                    Utils.Preference.removePref(mContext, Constants.Account.PREF_SYSTEM_EXTENDED_TOKEN);
                    if (Utils.Preference.getBooleanPref(mContext, Constants.Account.PREF_LOGIN_SYSTEM,
                            false)) {
                        // 如果正在使用系统帐号，则登出事件
                        setSystemLogin(false);
                        onAccountLogout();
                    }
                }
            }
        }
    };

    public Map<String, ExtendedAuthToken> getWebRequiredCachedServiceTokens() {
        Map<String, ExtendedAuthToken> map = null;
        Cursor c = mContext.getContentResolver().query(DataStats.CONTENT_URI,
                new String[] {
                    DataStats.STATS
                },
                DataStats.TYPE + "='" + DataMimeType.SERVICE_TOKEN + "'", null, null);

        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        String stats = c.getString(0);
                        if (!TextUtils.isEmpty(stats)) {
                            Pair<String, String> pair = DataMimeType.parseServiceToken(stats);
                            if (map == null) {
                                map = new HashMap<String, ExtendedAuthToken>();
                            }
                            map.put(pair.first, ExtendedAuthToken.parse(pair.second));
                            LogUtil.d(TAG, "The cached serviceToken is:" + pair.second);
                        }
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        }
        return map;
    }

    public Map<String, ExtendedAuthToken> getWebRequiredServiceTokens() {
        Map<String, ExtendedAuthToken> map = getWebRequiredCachedServiceTokens();
        // 重置系统帐号中所有的serviceToken
        if (map != null) {
            Iterator<Entry<String, ExtendedAuthToken>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, ExtendedAuthToken> entry = iterator.next();
                LogUtil.d(TAG, "Invalide serviceToken:" + entry.getValue());
                mAccountManager.invalidateAuthToken(Constants.Account.ACCOUNT_TYPE, entry.getValue().toPlain());
            }
            map.clear();
        }

        Map<String, String> sidsMap = getSidsMap();
        if (sidsMap != null) {
            Iterator<Entry<String, String>> iterator = sidsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                // Web上没有有效的serviceToken过期机制，现在每次都重新取serviceToken，防止web上Token过期，抢购
                // 活动时候没有登录的现象发生
                ExtendedAuthToken serviceToken = getExtendedAuthToken(entry.getValue());
                if (serviceToken != null) {
                    if (map == null) {
                        map = new HashMap<String, ExtendedAuthToken>();
                    }
                    map.put(entry.getKey(), serviceToken);
                    saveServiceToken(entry.getValue(), serviceToken.toPlain());
                }
                LogUtil.d(TAG, "The sid " + entry.getValue() + " 's serviceToken is "
                        + (serviceToken == null ? "null" : serviceToken.authToken));
            }
        }
        return map;
    }

    private void saveServiceToken(String sid, String serviceToken) {
        Cursor c = mContext.getContentResolver().query(
                DataStats.CONTENT_URI,
                new String[] {
                    BaseColumns._ID
                },
                DataStats.TYPE + "='" + DataMimeType.SERVICE_TOKEN + "' AND " + DataStats.STATS
                        + " LIKE '" + sid + "%'", null, null);

        ContentValues values = new ContentValues();
        values.put(DataStats.STATS, DataMimeType.formatServiceToken(sid, serviceToken));
        try {
            if (c != null && c.getCount() > 0) {
                // 如果有缓存，那么更新缓存
                if (c.moveToFirst()) {
                    long id = c.getLong(0);
                    mContext.getContentResolver().update(DataStats.CONTENT_URI, values,
                            BaseColumns._ID + "=" + id, null);
                }
            } else {
                // 如果缓存中没有token，那么直接插入数据
                values.put(DataStats.TYPE, DataMimeType.SERVICE_TOKEN);
                mContext.getContentResolver().insert(DataStats.CONTENT_URI, values);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * 获取商城需要的所有服务的serviceToken ids
     */
    private Map<String, String> getSidsMap() {
        Map<String, String> map = null;
        Request request = new Request(HostManager.getServiceTokens());
        if (request.getStatus() == Request.STATUS_OK) {
            JSONObject json = request.requestJSON();
            LogUtil.d(TAG, json.toString());

            if (json != null && json.optInt(Tags.CODE) == 0) {
                JSONObject sids = json.optJSONObject(Tags.DATA);
                if (sids != null) {
                    Iterator<String> iter = sids.keys();
                    while (iter.hasNext()) {
                        if (map == null) {
                            map = new HashMap<String, String>();
                        }
                        String key = iter.next();
                        // 默认的SID通过APP有有效的过期重置机制，无需每次重新获取
                        if (!TextUtils.equals(Constants.Account.DEFAULT_SERVICE_ID, key)) {
                            map.put(sids.optString(key), key);
                            LogUtil.d(TAG, "The sid is " + key + " and the value is "
                                    + sids.optString(key));
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * 登录监听
     */
    public interface AccountListener {
        public void onLogin(String userId, String authToken, String security);

        public void onInvalidAuthonToken();

        public void onLogout();
    }

    /**
     * 登录回调
     */
    public interface LoginCallback {
        public void onLoginSucceed(String userId, String authToken, String security);

        public void onLoginFailed(int error);
    }

    /**
     * 登出回调
     */
    public interface LogoutCallback {
        public void onLogout();
    }
}
