
package com.xiaomi.xms.sales.request;

import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Coder;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class Request {
    private static final String TAG = "Request";
    /**
     * The following variables started with STATUS_ are request response status
     * code to indicate callers what happens.
     */
    // Status OK
    public static final int STATUS_OK = 0;
    // Server error, related to HTTPStatus 40X and 50X
    public static final int STATUS_SERVER_ERROR = 1;
    // Client error, include io
    public static final int STATUS_CLIENT_ERROR = 2;
    // Connection parameters error
    public static final int STATUS_PARAM_ERROR = 3;
    // Status network unavailable
    public static final int STATUS_NETWORK_UNAVAILABLE = 4;
    // Status service unavailable
    public static final int STATUS_SERVICE_UNAVAILABLE = 5;
    // Status unknown error
    public static final int STATUS_UNKNOWN_ERROR = 6;
    // NOT MODIFIED
    public static final int STATUS_NOT_MODIFIED = 7;
    // Auth error
    public static final int STATUS_AUTH_ERROR = 8;

    // Timeout (in ms) we specify for each http request
    protected static final int HTTP_REQUEST_TIMEOUT_MS = 10 * 1000;
    protected static final int HTTP_REQUEST_DELAY_MS = 5 * 1000;

    private static final CookieManager sCookieManager = new CookieManager(null,
            CookiePolicy.ACCEPT_ALL);

    // The responsed json result and the user id and Auth token
    private JSONObject mJSONResult;
    private String mUserId;
    private ExtendedAuthToken mToken;
    private static String sUserAgent;
    private static String sCookie;
    private String mEtag;

    // The parameters list
    private List<NameValuePair> mParamsList;
    protected String mRequestUrl;
    private String mRequestMethod;
    private boolean mNeedSignature;
    private HashMap<String, String> mRequestHeaders;

    public Request(String url) {
        mRequestUrl = url;
        mRequestMethod = HttpPost.METHOD_NAME;
        mNeedSignature = false;
        mRequestHeaders = new HashMap<String, String>();
    }

    public Request addHeader(String name, String value) {
        mRequestHeaders.put(name, value);
        return this;
    }

    public Request setHttpMethod(String method) {
        mRequestMethod = method;
        return this;
    }

    // Get the parameters which will be appended to the requested url
    public void addParam(String key, String value) {
        if (mParamsList == null) {
            mParamsList = new ArrayList<NameValuePair>();
        }

        BasicNameValuePair param = new BasicNameValuePair(key, value);
        if (!mParamsList.contains(param)) {
            mParamsList.add(new BasicNameValuePair(key, value));
        }
    }

    // Clear all parameters
    public void clearParams() {
        if (mParamsList != null) {
            mParamsList.clear();
        }
    }

    /**
     * Weather the parameters required to be signed
     * 
     * @param needed
     * @return this
     */
    public Request setNeedSignature(boolean needed) {
        mNeedSignature = needed;
        return this;
    }

    protected HttpURLConnection getConn() {
        final String url = getRequestUrl();
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        LogUtil.d(TAG, "getConn:The connection url is " + url);
        HttpURLConnection conn = null;
        try {
            final URL req = new URL(url);
            conn = (HttpURLConnection) req.openConnection();
            conn.setReadTimeout(HTTP_REQUEST_TIMEOUT_MS);
            conn.setConnectTimeout(HTTP_REQUEST_TIMEOUT_MS);
            conn.setRequestMethod(mRequestMethod);
            if (TextUtils.equals(mRequestMethod, HttpPost.METHOD_NAME)) {
                conn.setDoOutput(true);
                conn.setUseCaches(false);
            }
            String cookie = getCookies();
            if (!TextUtils.isEmpty(cookie)) {
                conn.setRequestProperty("Cookie", getCookies());
            }
            conn.setRequestProperty("User-Agent", getUserAgent());
            if (mRequestHeaders != null && mRequestHeaders.size() > 0) {
                Iterator iter = mRequestHeaders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // 获取请求结果
    public int getStatus() {
        if (!Utils.Network.isNetWorkConnected(ShopApp.getContext())) {
            return STATUS_NETWORK_UNAVAILABLE;
        }

        HttpURLConnection conn = null;
        BufferedReader br = null;
        int statusCode = STATUS_CLIENT_ERROR;

        try {
            conn = getConn();
            // Connect to the server
            conn.connect();

            if (TextUtils.equals(mRequestMethod, HttpPost.METHOD_NAME)) {
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(getParams());
                out.flush();
            }
            // Check the response code
            int responseCode = conn.getResponseCode();
            LogUtil.d(TAG, "The response code is:" + responseCode);
            if (responseCode == HttpStatus.SC_OK) {

                String etag = conn.getHeaderField("etag");
                if (!TextUtils.isEmpty(etag)) {
                    mEtag = etag;
                }
                String compressed = conn
                        .getHeaderField(HostManager.Parameters.Keys.COMPRESS_HEADER);

                // Read cookie
                putCookies(conn.getHeaderFields());

                // Read response from the connection to after posting info
                InputStream in = conn.getInputStream();
                if (compressed != null && compressed.equals(HostManager.Parameters.Keys.COMPRESS)) {
                    Inflater inflater = new Inflater(true);
                    in = new InflaterInputStream(in, inflater);
                }
                br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                if (mNeedSignature) {
                    if (mToken != null && !TextUtils.isEmpty(mToken.security)) {
                        mJSONResult = new JSONObject(Coder.decodeAES(
                                sb.toString(), mToken.security));
                        statusCode = STATUS_OK;
                    } else {
                        statusCode = STATUS_PARAM_ERROR;
                    }
                } else {
                    mJSONResult = new JSONObject(sb.toString());
                    statusCode = STATUS_OK;
                }
            } else if (responseCode == HttpStatus.SC_NOT_MODIFIED) {
                statusCode = STATUS_NOT_MODIFIED;
            } else if (isServerError(responseCode)) {
                if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                    LoginManager.getInstance().invalidAuthToken();
                    statusCode = STATUS_AUTH_ERROR;
                } else {
                    statusCode = STATUS_SERVER_ERROR;
                }
            } else {
                statusCode = STATUS_UNKNOWN_ERROR;
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            statusCode = STATUS_SERVICE_UNAVAILABLE;
        } catch (JSONException e) {
            statusCode = STATUS_SERVICE_UNAVAILABLE;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            if (!Utils.Network.isNetWorkConnected(ShopApp.getContext())) {
                statusCode = STATUS_NETWORK_UNAVAILABLE;
            } else {
                statusCode = STATUS_SERVICE_UNAVAILABLE;
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
        LogUtil.d(TAG, "getStatus:The status code is " + statusCode);
        LogUtil.d(TAG, "etag is " + conn.getHeaderField("etag"));
        return statusCode;
    }

    // 获取请求的JSON数据
    public JSONObject requestJSON() {
        return mJSONResult;
    }

    protected boolean isServerError(int statusCode) {
        return statusCode == HttpStatus.SC_BAD_REQUEST
                || statusCode == HttpStatus.SC_UNAUTHORIZED
                || statusCode == HttpStatus.SC_FORBIDDEN
                || statusCode == HttpStatus.SC_NOT_ACCEPTABLE
                || statusCode / 100 == 5;
    }

    protected List<NameValuePair> signParamters() {
        return mParamsList;
    }

    private String getParams() {
        List<NameValuePair> paramList = null;
        if (mNeedSignature) {
            paramList = signParamters();
        } else {
            paramList = mParamsList;
        }

        if (paramList == null) {
            paramList = new ArrayList<NameValuePair>();
        }

        paramList.add(new BasicNameValuePair(Parameters.Keys.COMPRESS, "1"));
        paramList.add(new BasicNameValuePair(Parameters.Keys.CLIENT_ID, Parameters.Values.CLIENT_ID));
        paramList.add(new BasicNameValuePair(Parameters.Keys.DEVICE_DENSITY, String
                .valueOf(Device.DISPLAY_DENSITY)));
        paramList.add(new BasicNameValuePair(Parameters.Keys.VERSION,
                String.valueOf(Device.SHOP_VERSION)));
        return URLEncodedUtils.format(paramList, HTTP.UTF_8);
    }

    // The constructed url to requested
    public String getRequestUrl() {
        if (TextUtils.equals(mRequestMethod, HttpPost.METHOD_NAME)) {
            return mRequestUrl + "?random=" + UUID.randomUUID().toString().replaceAll("-", "");
        }
        final String params = getParams();
        return TextUtils.isEmpty(params) ? mRequestUrl : mRequestUrl.contains("?") ? String.format("%s&%s",
                mRequestUrl,
                params) : String.format("%s?%s", mRequestUrl,
                params);
    }

    private String getUserAgent() {
        if (sUserAgent == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(Device.COUNTRY).append(";");
            sb.append(Device.LANGUAGE).append(";");
            sb.append(Device.SHOP_VERSION);
            sUserAgent = sb.toString();
        }
        return sUserAgent;
    }

    private void putCookies(Map<String, List<String>> map) {
        try {
            sCookieManager.put(URI.create(mRequestUrl), map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCookies() {
        String userId = LoginManager.getInstance().getUserId();
        ExtendedAuthToken token = LoginManager.getInstance().getExtendedAuthToken(
                Constants.Account.DEFAULT_SERVICE_ID);
        if (token == null) {
            token = ExtendedAuthToken.build("", "");
        }

        if (!TextUtils.equals(userId, mUserId) || !token.equals(mToken)) {
            mUserId = userId;
            if (TextUtils.isEmpty(token.authToken)) {
                mToken = null;
            } else {
                mToken = token;
            }
            sCookie = null;
        }

        // 如果Cookie有变化，同时UserId和authToken不为空，那么重新构造验证Cookie
        if (!TextUtils.isEmpty(mUserId) && mToken != null && !TextUtils.isEmpty(mToken.authToken)
                && sCookie == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("serviceToken=");
            sb.append(URLEncoder.encode(mToken.authToken));
            sb.append("; userId=");
            sb.append(mUserId);
            sCookie = sb.toString();
        }

        List<HttpCookie> cookieList = sCookieManager.getCookieStore().getCookies();
        if (cookieList == null || cookieList.size() == 0) {
            return sCookie;
        }

        StringBuilder sbCookie = new StringBuilder();
        for (HttpCookie cookie : cookieList) {
            if (TextUtils.indexOf(URI.create(mRequestUrl).getHost(), cookie.getDomain()) > 0) {
                sbCookie.append(cookie.getName());
                sbCookie.append("=");
                sbCookie.append(cookie.getValue());
                sbCookie.append("; ");
            }
        }

        return sbCookie.toString() + sCookie;
    }

    public String getEtag() {
        return mEtag;
    }

}
