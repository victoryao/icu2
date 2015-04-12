package com.xiaomi.xms.sales.xmsf.account.utils;

import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.xmsf.account.exception.AccessDeniedException;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SimpleRequest {
    private static final String TAG = "SimpleRequest";
    private static final String ENC = "utf-8";

    private static final int TIMEOUT = 30000;

    protected static String appendUrl(String origin,
            List<NameValuePair> nameValuePairs) {
        if (origin == null) {
            throw new NullPointerException("origin is not allowed null");
        }
        StringBuilder urlBuilder = new StringBuilder(origin);
        if (nameValuePairs != null) {
            final String paramPart = URLEncodedUtils
                    .format(nameValuePairs, ENC);
            if (paramPart != null && paramPart.length() > 0) {
                if (origin.contains("?")) {
                    urlBuilder.append("&");
                } else {
                    urlBuilder.append("?");
                }
                urlBuilder.append(paramPart);
            }
        }
        return urlBuilder.toString();
    }

    public static StringContent getAsString(String url,
            Map<String, String> params, Map<String, String> cookies,
            boolean readBody) throws IOException, AccessDeniedException {
        List<NameValuePair> nameValuePairs = ObjectUtils.mapToPairs(params);
        final String fullUrl = appendUrl(url, nameValuePairs);
        LogUtil.d(TAG, "requesting " + fullUrl);
        HttpURLConnection conn = makeConn(fullUrl, cookies);
        if (conn == null) {
            LogUtil.e(TAG, "failed to create URLConnection");
            return null;
        }
        try {
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.connect();

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK
                    || code == HttpURLConnection.HTTP_MOVED_TEMP) {
                final Map<String, List<String>> headerFields = conn
                        .getHeaderFields();
                final CookieManager cm = new CookieManager();
                final URI reqUri = URI.create(fullUrl);
                cm.put(reqUri, headerFields);
                List<HttpCookie> httpCookies = cm.getCookieStore().get(reqUri);
                Map<String, String> cookieMap = parseCookies(httpCookies);
                cookieMap.putAll(ObjectUtils.listToMap(headerFields));
                StringBuilder sb = new StringBuilder();
                if (readBody) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()), 1024);
                    try {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        br.close();
                    }
                }
                final StringContent stringContent = new StringContent(
                        sb.toString());
                stringContent.putHeaders(cookieMap);
                return stringContent;
            } else if (code == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new AccessDeniedException(
                        "access denied, encrypt error or user is forbidden to access the resource");
            } else {
                LogUtil.e(TAG, "http status error when GET: " + code);
            }
        } catch (ProtocolException e) {
            throw new IOException("protocol error");
        } finally {
            conn.disconnect();
        }
        return null;
    }

    public static MapContent getAsMap(String url,
            Map<String, String> params, Map<String, String> cookies,
            boolean readBody) throws IOException, AccessDeniedException {
        StringContent stringContent = getAsString(url, params, cookies,
                readBody);
        return convertStringToMap(stringContent);
    }

    public static StringContent postAsString(String url,
            Map<String, String> params, Map<String, String> cookies,
            boolean readBody) throws IOException, AccessDeniedException {
        LogUtil.d(TAG, "requesting " + url);
        HttpURLConnection conn = makeConn(url, cookies);
        if (conn == null) {
            LogUtil.e(TAG, "failed to create URLConnection");
            return null;
        }
        try {
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.connect();

            List<NameValuePair> nameValuePairs = ObjectUtils.mapToPairs(params);
            if (nameValuePairs != null) {
                String content = URLEncodedUtils.format(nameValuePairs, ENC);
                OutputStream os = conn.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(os);
                try {
                    bos.write(content.getBytes(ENC));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK
                    || code == HttpURLConnection.HTTP_MOVED_TEMP) {
                final Map<String, List<String>> headerFields = conn
                        .getHeaderFields();
                final CookieManager cm = new CookieManager();
                final URI reqUri = URI.create(url);
                cm.put(reqUri, headerFields);
                Map<String, String> cookieMap = parseCookies(
                        cm.getCookieStore().get(reqUri));
                cookieMap.putAll(ObjectUtils.listToMap(headerFields));
                StringBuilder sb = new StringBuilder();
                if (readBody) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()),
                            1024);
                    try {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
                final StringContent stringContent = new StringContent(
                        sb.toString());
                stringContent.putHeaders(cookieMap);
                return stringContent;
            } else if (code == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new AccessDeniedException(
                        "access denied, encrypt error or user is forbidden to access the resource");
            } else {
                LogUtil.e(TAG, "http status error when POST: " + code);
            }
        } catch (ProtocolException e) {
            throw new IOException("protocol error");
        } finally {
            conn.disconnect();
        }
        return null;
    }

    public static MapContent postAsMap(String url,
            Map<String, String> params, Map<String, String> cookies,
            boolean readBody) throws IOException, AccessDeniedException {
        StringContent stringContent = postAsString(url, params, cookies,
                readBody);
        return convertStringToMap(stringContent);
    }

    protected static MapContent convertStringToMap(StringContent stringContent) {
        if (stringContent == null) {
            return null;
        }
        String bodyString = stringContent.getBody();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(bodyString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Map<String, Object> contentMap = ObjectUtils
                .jsonToMap(jsonObject);
        MapContent mapContent = new MapContent(contentMap);
        mapContent.putHeaders(stringContent.getHeaders());
        return mapContent;
    }

    protected static HttpURLConnection makeConn(String url,
            Map<String, String> cookies) {
        URL req = null;
        try {
            req = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (req == null) {
            LogUtil.e(TAG, "failed to init url");
            return null;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) req.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(TIMEOUT);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            if (cookies != null) {
                conn.setRequestProperty("Cookie", joinMap(cookies, "; "));
            }
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static String joinMap(Map<String, String> map, String sp) {
        if (map == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> entries = map.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            if (i > 0) {
                sb.append(sp);
            }
            final String key = entry.getKey();
            final String value = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(value);
            i++;
        }
        return sb.toString();
    }

    protected static Map<String, String> parseCookies(
            List<HttpCookie> cookies) {
        Map<String, String> cookieMap = new HashMap<String, String>();
        for (HttpCookie cookie : cookies) {
            if (!cookie.hasExpired()) {
                final String name = cookie.getName();
                final String value = cookie.getValue();
                if (name != null) {
                    cookieMap.put(name, value);
                }
            } else {
                LogUtil.d(TAG, "cookie has expired, key:" + cookie.getName());
            }
        }
        return cookieMap;
    }

    public static class HeaderContent {

        private final Map<String, String> headers
                = new HashMap<String, String>();

        public void putHeader(String key, String value) {
            headers.put(key, value);
        }

        public String getHeader(String key) {
            return headers.get(key);
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void putHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
        }

        @Override
        public String toString() {
            return "HeaderContent{" +
                    "headers=" + headers +
                    '}';
        }
    }

    public static class StringContent extends HeaderContent {

        private String body;

        public StringContent(String body) {
            this.body = body;
        }

        public String getBody() {
            return body;
        }

        @Override
        public String toString() {
            return "StringContent{" +
                    "body='" + body + '\'' +
                    '}';
        }
    }

    public static class MapContent extends HeaderContent {

        private Map<String, Object> bodies;

        public MapContent(Map<String, Object> bodies) {
            this.bodies = bodies;
        }

        public Object getFromBody(String key) {
            return bodies.get(key);
        }

        @Override
        public String toString() {
            return "MapContent{" +
                    "bodies=" + bodies +
                    '}';
        }
    }

    public static class StreamContent extends HeaderContent {

        private InputStream stream;

        public StreamContent(InputStream stream) {
            this.stream = stream;
        }

        public InputStream getStream() {
            return stream;
        }

    }

}
