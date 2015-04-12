
package com.xiaomi.xms.sales.xmsf.account.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.data.AccountInfo;
import com.xiaomi.xms.sales.xmsf.account.exception.AccessDeniedException;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidCredentialException;
import com.xiaomi.xms.sales.xmsf.account.exception.InvalidResponseException;
import com.xiaomi.xms.sales.xmsf.miui.utils.CarrierSelector;
import com.xiaomi.xms.sales.xmsf.miui.utils.CloudCoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper class to perform account related tasks.
 */
public final class CloudHelper {

    private static final String TAG = "CloudHelper";

    private static final String LOCATION = "Location";

    private static final Integer INT_0 = 0;

    /**
     * key: imsi, value: array[0] hashed device id, array[1] hashed imsi
     */
    private static final Map<String, String> mDeviceInfoMap = new HashMap<String, String>();

    private static final CarrierSelector<String> CARRIER_SELECTOR = new CarrierSelector<String>(
            CarrierSelector.CARRIER.CHINA_MOBILE);

    static {
        CARRIER_SELECTOR
                .register(CarrierSelector.CARRIER.CHINA_MOBILE, Constants.Account.SMS_GW_CM);
        CARRIER_SELECTOR
                .register(CarrierSelector.CARRIER.CHINA_UNICOM, Constants.Account.SMS_GW_CU);
        CARRIER_SELECTOR
                .register(CarrierSelector.CARRIER.CHINA_TELECOM, Constants.Account.SMS_GW_CT);
    }

    public static AccountInfo getServiceTokenByPassword(String userId,
            String password,
            String serviceId)
            throws IOException, InvalidResponseException,
            InvalidCredentialException, AccessDeniedException {
        if (userId == null || password == null || serviceId == null) {
            throw new NullPointerException("invalid params");
        }
        EasyMap<String, String> params = new EasyMap<String, String>()
                .easyPut("user", userId)
                .easyPut("pwd", password)
                .easyPut("sid", serviceId);
        SimpleRequest.StringContent loginContent = SimpleRequest
                .postAsString(Constants.Account.URL_LOGIN_AUTH, params, null, false);
        if (loginContent == null) {
            throw new IOException("failed to get response from server");
        }
        return processLoginContent(loginContent);
    }

    public static AccountInfo getServiceTokenByPassToken(String userId,
            String passToken,
            String serviceId) throws IOException,
            InvalidResponseException, InvalidCredentialException,
            AccessDeniedException {
        EasyMap<String, String> params = new EasyMap<String, String>()
                .easyPut("sid", serviceId);
        EasyMap<String, String> cookies = new EasyMap<String, String>()
                .easyPut("userId", userId)
                .easyPut("passToken", passToken);

        LogUtil.d(TAG, "The login url is:" + Constants.Account.URL_LOGIN);
        SimpleRequest.StringContent loginContent = SimpleRequest
                .getAsString(Constants.Account.URL_LOGIN, params, cookies, false);
        if (loginContent == null) {
            throw new IOException("failed to get response from service server");
        }
        return processLoginContent(loginContent);
    }

    protected static AccountInfo processLoginContent(
            SimpleRequest.StringContent loginContent)
            throws InvalidResponseException, InvalidCredentialException {
        // location where to get the service token
        String serviceTokenLocation = loginContent.getHeader(LOCATION);
        LogUtil.d(TAG, "The callback url is:" + serviceTokenLocation);
        String userId = loginContent.getHeader("userId");
        String passToken = loginContent.getHeader("passToken");
        String extParams = loginContent.getHeader("extension-pragma");
        if (TextUtils.isEmpty(serviceTokenLocation)) {
            throw new InvalidCredentialException(
                    "no get auth location, password mistakes");
        }
        if (TextUtils.isEmpty(userId)) {
            throw new InvalidResponseException("no user Id");
        }
        if (TextUtils.isEmpty(passToken)) {
            throw new InvalidResponseException(
                    "no passToken in login response");
        }
        if (TextUtils.isEmpty(extParams)) {
            throw new InvalidResponseException("empty extension-pragma");
        }
        String security = null;
        Long nonce = null;
        try {
            JSONObject jObj = new JSONObject(extParams);
            security = jObj.optString("ssecurity");
            nonce = jObj.optLong("nonce");
        } catch (JSONException e) {
        }

        if (security == null || nonce == null) {
            throw new InvalidResponseException("security or nonce is null");
        }
        String clientSign = getClientSign(nonce, security);
        if (clientSign == null) {
            Log.e(TAG, "failed to get client sign");
            throw new InvalidResponseException("sign parameters failure");
        }
        EasyMap<String, String> params = new EasyMap<String, String>("clientSign", clientSign);
        // params = params.easyPut("client_id", "180100031013");
        SimpleRequest.StringContent serviceTokenContent = null;
        try {
            serviceTokenContent = SimpleRequest
                    .getAsString(serviceTokenLocation, params, null, false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
        if (serviceTokenContent == null) {
            throw new InvalidResponseException(
                    "no response when get service token");
        }
        LogUtil.d(TAG, "The serviceTokenContent is " + serviceTokenContent);
        LogUtil.d("test", serviceTokenContent.getHeaders().toString());
        String serviceToken = serviceTokenContent
                .getHeader("serviceToken");
        if (TextUtils.isEmpty(serviceToken)) {
            throw new InvalidResponseException(
                    "no service token contained in response");
        }
        return new AccountInfo(userId, passToken, serviceToken, security);
    }

    public static void sendActivateSms(Context context,
            PendingIntent sentIntent, String deviceId, String imsi) {
        deviceId = hashDeviceInfo(deviceId);
        imsi = hashDeviceInfo(imsi);
        SmsManager sm = SmsManager.getDefault();
        String gw = selectSmsGw(context);
        String smsBody = "RP/" + deviceId + "/" + imsi;
        sm.sendTextMessage(gw, null, smsBody, sentIntent, null);
    }

    public static String queryPhone(String deviceId, String imsi)
            throws IOException, InvalidResponseException {
        deviceId = hashDeviceInfo(deviceId);
        imsi = hashDeviceInfo(imsi);
        String url = String.format(Constants.Account.URL_QUERY_PHONE, deviceId);
        EasyMap<String, String> param = new EasyMap<String, String>("imsi", imsi);
        SimpleRequest.MapContent mapContent = null;
        try {
            mapContent = SimpleRequest
                    .getAsMap(url, param, null, true);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
        if (mapContent == null) {
            throw new IOException("failed to get response from server");
        }
        Object code = mapContent.getFromBody("code");
        if (INT_0.equals(code)) {
            Object dataObj = mapContent.getFromBody("data");
            if (dataObj instanceof Map) {
                Map dataMap = (Map) dataObj;
                Object phoneObj = dataMap.get("phone");
                Object imsiObj = dataMap.get("imsi");
                if (imsi.equals(imsiObj)) {
                    if (phoneObj instanceof String) {
                        return (String) phoneObj;
                    }
                }
                return null;
            }
        }
        throw new InvalidResponseException(
                "invalid response from server, description:" + mapContent
                        .getFromBody("description"));
    }

    public static void regBySms(Context context, PendingIntent sentIntent,
            String deviceId, String imsi, String password) {
        deviceId = hashDeviceInfo(deviceId);
        imsi = hashDeviceInfo(imsi);
        SmsManager sm = SmsManager.getDefault();
        String gw = Utils.Preference.getStringPref(context, Constants.Prefence.PREF_KEY_SMS_WG,
                Constants.Account.SMS_GW_DEFAULT);
        String smsBody = "XM/" + deviceId + "/" + imsi + "/" + password;
        sm.sendTextMessage(gw, null, smsBody, sentIntent, null);
    }

    public static String regByEmail(String email, String password)
            throws IOException, InvalidResponseException {
        EasyMap<String, String> params = new EasyMap<String, String>()
                .easyPut("email", email)
                .easyPut("password", password);
        SimpleRequest.MapContent regContent = null;
        try {
            regContent = SimpleRequest
                    .postAsMap(Constants.Account.URL_REG, params, null, true);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
        if (regContent == null) {
            throw new IOException("failed to register, no response");
        }
        Object code = regContent.getFromBody("code");
        if (INT_0.equals(code)) {
            Object data = regContent.getFromBody("data");
            LogUtil.d(TAG, "get data node:" + data);
            if (data instanceof Map) {
                Map dataMap = (Map) data;
                Object userIdObj = dataMap.get("userId");
                if (userIdObj instanceof Integer) {
                    return String.valueOf(userIdObj);
                }
            }
        }
        LogUtil.w(TAG, String.format("register failed, code: %s, description: %s",
                code, regContent.getFromBody("description")));
        throw new InvalidResponseException(
                "failed to register due to invalid response from server");
    }

    public static void sendActivateEmail(String userId, String email)
            throws IOException, InvalidResponseException {
        EasyMap<String, String> params = new EasyMap<String, String>()
                .easyPut("userId", userId)
                .easyPut("addressType", "EM")
                .easyPut("address", email);
        SimpleRequest.MapContent regContent = null;
        try {
            regContent = SimpleRequest
                    .getAsMap(Constants.Account.URL_RESEND_EMAIL, params, null, true);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
        if (regContent == null) {
            throw new IOException("failed to register, no response");
        }
        Object code = regContent.getFromBody("code");
        if (!INT_0.equals(code)) {
            throw new InvalidResponseException(
                    "invalid response, failed to send activate email");
        }
    }

    public static String getUserIdForEmail(String email)
            throws IOException, InvalidResponseException {
        return getUserId(email, "EM");
    }

    public static String getUserIdForPhone(String phone)
            throws IOException, InvalidResponseException {
        return getUserId(phone, "PH");
    }

    private static String getUserId(String address, String type)
            throws IOException, InvalidResponseException {
        EasyMap<String, String> param = new EasyMap<String, String>()
                .easyPut("type", type)
                .easyPut("externalId", address);
        SimpleRequest.MapContent mapContent = null;
        try {
            mapContent = SimpleRequest
                    .getAsMap(Constants.Account.URL_USER_EXISTS, param, null, true);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        }
        if (mapContent == null) {
            throw new IOException(
                    "failed to get response when getting user id");
        }
        Object code = mapContent.getFromBody("code");
        if (INT_0.equals(code)) {
            Object data = mapContent.getFromBody("data");
            if (data instanceof Map) {
                Map dataMap = (Map) data;
                Object userIdObj = dataMap.get("userId");
                if (userIdObj instanceof Integer) {
                    Integer userId = (Integer) userIdObj;
                    if (userId > 0) {
                        return String.valueOf(userId);
                    } else {
                        LogUtil.d(TAG, "user not registered, id:" + address);
                        return null;
                    }
                }
                LogUtil.w(TAG, "invalid user id:" + userIdObj);
            }
        }
        throw new InvalidResponseException(String.format(
                "server error when getting user id, reason:%s, description:%s, code:%s",
                mapContent.getFromBody("reason"),
                mapContent.getFromBody("description"),
                mapContent.getFromBody("code")));
    }

    protected static String selectSmsGw(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String mccmnc = tm.getNetworkOperator();
        return CARRIER_SELECTOR.selectValue(mccmnc);
    }

    public static String selectSmsGwByServer(Context context) throws IOException,
            InvalidResponseException, SocketTimeoutException {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String mccmnc = tm.getNetworkOperator();
        SimpleRequest.StringContent result = null;
        EasyMap<String, String> params = new EasyMap<String, String>()
                .easyPut("region", mccmnc)
                .easyPut(Parameters.Keys.CLIENT_ID, Parameters.Values.CLIENT_ID);
        try {
            result = SimpleRequest
                    .getAsString(Constants.Account.URL_QUERY_SMS_GW, params, null, true);
            if (result != null) {
                if (!TextUtils.isEmpty(result.getBody())) {
                    JSONObject o = new JSONObject(result.getBody());
                    int code = o.getInt("code");
                    if (code != 0) {
                        throw new InvalidResponseException("fetchSmsGateway: code = " + code);
                    }
                    JSONObject data = o.getJSONObject("data");
                    if (data == null) {
                        throw new InvalidResponseException("fetchSmsGateway: null data");
                    }
                    String gw = data.getString("info");
                    if (TextUtils.isEmpty(gw)) {
                        throw new InvalidResponseException("fetchSmsGateway: null gw");
                    }
                    return gw;
                } else {
                    throw new InvalidResponseException("fetchSmsGateway: error");
                }
            } else {
                throw new InvalidResponseException("fetchSmsGateway: error");
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AccessDeniedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new InvalidResponseException(
                "failed to get sms gw due to invalid response from server");
    }

    protected static String getClientSign(Long nonce, String security) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("nonce", String.valueOf(nonce));
        return CloudCoder.generateSignature(null, null, params, security);
    }

    /**
     * hash deviceId or imsi
     * 
     * @param plain plain representation of device id or imsi
     * @return 16 bytes long hash value
     */
    private static String hashDeviceInfo(String plain) {
        synchronized (mDeviceInfoMap) {
            if (plain == null) {
                plain = "0";
            }
            String hash = mDeviceInfoMap.get(plain);
            if (hash == null) {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    hash = Base64.encodeToString(md.digest(plain.getBytes()),
                            Base64.URL_SAFE).substring(0, 16);
                    mDeviceInfoMap.put(plain, hash);
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException(
                            "failed to init SHA1 digest");
                }
            }
            return hash;
        }
    }

}
