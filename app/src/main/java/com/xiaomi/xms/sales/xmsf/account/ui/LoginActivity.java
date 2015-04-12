
package com.xiaomi.xms.sales.xmsf.account.ui;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ICUMainActivity;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.UserInfo;
import com.xiaomi.xms.sales.request.ExtendedAuthToken;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.xmsf.account.data.AccountInfo;
import com.xiaomi.xms.sales.xmsf.account.exception.AccessDeniedException;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidCredentialException;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidResponseException;
import com.xiaomi.xms.sales.xmsf.account.utils.CloudHelper;

public class LoginActivity extends BaseActivity implements
        LoginInputFragment.OnLoginInterface,
        LoginProgressFragment.OnLoginProgressInterface,
        LoginFailureFragment.OnLoginFailureInterface {

    private static final String TAG = "LoginActivity";

    private static final int ERROR_PASSWORD = 1;

    private static final int ERROR_NETWORK = 2;

    private static final int ERROR_SERVER = 3;

    private static final int ERROR_FORBIDDEN = 4;

    private static final int ERROR_MIHOME_FORBIDDEN = 5;

    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_SERVICE_TOKEN = "key_service_token";
    public static final String KEY_SECURITY = "key_security";

    private LoginTask mLoginTask;

    private String mServiceUrl;

    private boolean mActivityStatusSaved;

    private LoginResult mLoginResult;

    private FragmentManager mFragmentManager;

    private LoginInputFragment mLoginInputFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.login_activity);
        
        setTitle(getString(R.string.app_name));

        Intent intent = getIntent();
        final String serviceUrl = intent.getStringExtra(Constants.Account.EXTRA_SERVICE_URL);
        if (TextUtils.isEmpty(serviceUrl)) {
            throw new IllegalStateException("empty service id or service url");
        }
        mServiceUrl = serviceUrl;

        mFragmentManager = getSupportFragmentManager();

        mLoginInputFragment = new LoginInputFragment();
        mLoginInputFragment.setOnLoginInterface(this);

        mFragmentManager.beginTransaction().add(R.id.fragment_container, mLoginInputFragment).commit();
        setShoppingBarEnable(false);
        setHomeButtonEnable(false);
    }


    @Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return false;// 返回为true 则显示系统menu
	}
    
	
    @Override
    protected void onResume() {
        super.onResume();
        mActivityStatusSaved = false;
        handleLoginResult(mLoginResult, true);
        displayOrHideSoftInput();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mActivityStatusSaved = true;
    }

    @Override
    protected void onDestroy() {
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
            mLoginTask = null;
        }
        handleLoginResult(mLoginResult, false);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void displayOrHideSoftInput() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            hideSoftInput();
        }
    }

    private void hideSoftInput() {
        // hide soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void tryLogin(String username, String pwd) {
        // begin login
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
        if(username.equals("icu") && pwd.equals("123456")){
        	Intent intent = new Intent(this, ICUMainActivity.class);
			startActivity(intent);
        }else{
        	ToastUtil.show(getApplicationContext(), R.string.login_err);
        }
        finish();
       
//        mLoginTask = new LoginTask(username, pwd, mServiceUrl);
//        mLoginTask.execute();
    }

    @Override
    public void onStartLogin(String userName, String pwd) {
        tryLogin(userName, pwd);
    }

    @Override
    public void onLoginCanceled() {
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
            mLoginTask = null;
        }
        popupFragment();
    }

    @Override
    public void onCancelLoginAfterFailure() {
        popupFragment();
    }

    @Override
    public void onRetryLoginAfterFailure(String username, String pwd) {
        popupFragment();
        tryLogin(username, pwd);
    }

    public void onLoginSuccess(AccountInfo accountInfo) {
        String userId = accountInfo.getUserId();
        String serviceToken = accountInfo.getServiceToken();
        String passToken = accountInfo.getPassToken();
        String security = accountInfo.getSecurity();

        Utils.Preference.setStringPref(this, Constants.Account.PREF_UID, userId);
        Utils.Preference.setStringPref(this, Constants.Account.PREF_PASS_TOKEN, passToken);
        Utils.Preference.setStringPref(this, Constants.Account.PREF_EXTENDED_TOKEN, ExtendedAuthToken
                .build(serviceToken, security).toPlain());
    }

    private void popupFragment() {
        mFragmentManager.popBackStack();
    }

    private void replaceFragment(Fragment f, boolean popupTop, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (popupTop) {
            popupFragment();
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(android.R.id.content, f, f.getClass().getSimpleName());
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * handle login result
     * 
     * @param result contains login and result info
     * @param updateUI whether display the successful or failure info
     */
    private void handleLoginResult(LoginResult result, boolean updateUI) {
        if (result != null && (!mActivityStatusSaved || !updateUI)) {
            // getFragmentManager().popBackStack cannot be called after
            // onSaveInstanceState
            AccountInfo accountInfo = result.accountInfo;
            if (accountInfo != null) {
                String userId = accountInfo.getUserId();
                String serviceToken = accountInfo.getServiceToken();
                String security = accountInfo.getSecurity();

                onLoginSuccess(accountInfo);

                LoginManager.getInstance().onAccountLoginSucceed(userId, serviceToken, security);
                MainActivity.launchMain(LoginActivity.this, MainActivity.FRAGMENT_TAG_HOME);
                finish();
            } else if (updateUI) {
                LogUtil.d(TAG, "login failure");
                String reason;
                switch (result.errorCode) {
                    case ERROR_NETWORK: {
                        reason = getString(R.string.error_network);
                        break;
                    }
                    case ERROR_PASSWORD: {
                        reason = getString(R.string.bad_authentication);
                        break;
                    }
                    case ERROR_SERVER: {
                        reason = getString(R.string.error_server);
                        break;
                    }
                    case ERROR_FORBIDDEN: {
                        reason = getString(R.string.access_denied);
                        break;
                    }
                    case ERROR_MIHOME_FORBIDDEN: {
                        reason = result.errorInfo;
                        break;
                    }
                    default: {
                        reason = getString(R.string.error_unknown);
                        break;
                    }
                }
                LoginFailureFragment f = new LoginFailureFragment();
                f.setOnLoginFailureInterface(LoginActivity.this);
                f.setGetUserInputInterface(mLoginInputFragment);
                Bundle args = new Bundle();
                args.putString("reason", reason);
                args.putString("username", result.username);
                args.putString("pwd", result.password);
                f.setArguments(args);
                replaceFragment(f, true, true);
            }
            mLoginResult = null;
        }
    }

    private class LoginTask extends AsyncTask<String, Void, LoginResult> {
        private String username;
        private String password;
        private String serviceId;

        private LoginProgressFragment mLoginProgressFragment;

        private LoginTask(String username, String password, String serviceId) {
            this.username = username;
            this.password = password;
            this.serviceId = serviceId;
        }

        @Override
        protected void onPreExecute() {
            hideSoftInput();

            LoginProgressFragment f = new LoginProgressFragment();
            f.setOnLoginProgressInterface(LoginActivity.this);
            replaceFragment(f, false, true);
            mLoginProgressFragment = f;
        }

        @Override
        protected void onPostExecute(LoginResult result) {
            mLoginResult = result;
            handleLoginResult(result, true);
        }

        @Override
        protected LoginResult doInBackground(String... args) {
            final String name = this.username;
            final String pwd = this.password;

            // perform login
            AccountInfo accountInfo;
            try {
                accountInfo = CloudHelper
                        .getServiceTokenByPassword(name, pwd, serviceId);
            } catch (InvalidCredentialException e) {
                e.printStackTrace();
                return new LoginResult(null, ERROR_PASSWORD, "", serviceId,
                        username, pwd);
            } catch (InvalidResponseException e) {
                e.printStackTrace();
                return new LoginResult(null, ERROR_SERVER, "", serviceId,
                        username, pwd);
            } catch (IOException e) {
                e.printStackTrace();
                return new LoginResult(null, ERROR_NETWORK, "", serviceId,
                        username, pwd);
            } catch (AccessDeniedException e) {
                e.printStackTrace();
                return new LoginResult(null, ERROR_FORBIDDEN, "", serviceId,
                        username, pwd);
            }
            if (accountInfo != null && !isCancelled()) {
                LogUtil.d(TAG, "login success, service token:" + accountInfo.getServiceToken());
                // 判断该用户是否有米家的权限
                Request request = new Request(HostManager.URL_XMS_SALE_API);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Tags.XMSAPI.USERID, accountInfo.getUserId());
                    jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
                    String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_GETUSERINFO, jsonObject);
                    if (!TextUtils.isEmpty(data)) {
                        request.addParam(Tags.RequestKey.DATA, data);
                        if (request.getStatus() == Request.STATUS_OK) {
                            JSONObject json = request.requestJSON();
                            if (Tags.isJSONReturnedOK(json)) {
                                UserInfo userInfo = UserInfo.fromJSONObject(json);
                                if (userInfo != null) {
                                    // @HACKME XM_[userId]_UN
                                    // 这个cookie是用来记录用户的用户名，给Web中使用
                                    HostManager.setCookie(LoginActivity.this, "XM_" + userInfo.getUserId()
                                            + "_UN", URLEncoder.encode(userInfo.getUserName()),
                                            HostManager.DOMAIN_BASE);
                                }
                                saveUserName(username);
                                return new LoginResult(accountInfo, Activity.RESULT_OK, "",
                                        serviceId, username, password);
                            } else {
                                return new LoginResult(null, ERROR_MIHOME_FORBIDDEN, json.optJSONObject(Tags.HEADER)
                                        .optString(Tags.DESC), serviceId, username, pwd);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "login failure");
            }
            Log.w(TAG, "failed to get service token");
            return new LoginResult(null, ERROR_SERVER, "", serviceId,
                    username, pwd);
        }
    }

    private class LoginResult {
        final AccountInfo accountInfo;
        final int errorCode;
        final String errorInfo;
        final String serviceId;
        final String username;
        final String password;

        private LoginResult(AccountInfo accountInfo, int errorCode, String errorInfo,
                String serviceId, String username, String pwd) {
            this.accountInfo = accountInfo;
            this.errorCode = errorCode;
            this.errorInfo = errorInfo;
            this.serviceId = serviceId;
            this.username = username;
            this.password = pwd;
        }
    }

    private void saveUserName(String username) {
        String userNames = Utils.Preference.getStringPref(LoginActivity.this, Constants.Account.PREF_USER_NAMES, "");
        StringBuilder newUserNames = new StringBuilder(userNames);
        if (!TextUtils.isEmpty(userNames)) {
            // 防止同一账户重复添加
            if (newUserNames.indexOf(username) == -1) {
                String[] userNamesArray = userNames.split(Constants.Account.USER_NAME_SEPARATOR);
                // 使保存的帐号不多于5个
                if (userNamesArray.length > 4) {
                    newUserNames.delete(0, newUserNames.indexOf(Constants.Account.USER_NAME_SEPARATOR) + 1);
                }
                newUserNames.append(Constants.Account.USER_NAME_SEPARATOR);
                newUserNames.append(username);
            }
        } else {
            newUserNames.append(username);
        }
        Utils.Preference.setStringPref(LoginActivity.this, Constants.Account.PREF_USER_NAMES, newUserNames.toString());
    }
}
