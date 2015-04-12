
package com.xiaomi.xms.sales.util;

import android.text.TextUtils;

import com.xiaomi.xms.sales.misc.BASE64Decoder;
import com.xiaomi.xms.sales.misc.BASE64Encoder;
import com.xiaomi.xms.sales.model.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

public class JsonUtil {
    private final static String TAG = "JsonUtil";

    /**
     * 拼装请求的json
     * 
     * @param bodyJsonObj
     * @return
     */
    public static String creatRequestJson(String requestMethod, JSONObject bodyJsonObj) {
        String requestJson = "";
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject headJsonObj = new JSONObject();
            headJsonObj.put(Tags.RequestKey.APPID, Tags.RequestValue.APPID);
            headJsonObj.put(Tags.RequestKey.KEY, Tags.RequestValue.KEY);
            String body = bodyJsonObj.toString();
            String sign = MD5Utils.getMD5(Tags.RequestValue.APPID + body + Tags.RequestValue.KEY).toUpperCase(
                    Locale.getDefault());
            headJsonObj.put(Tags.RequestKey.SIGN, sign);
            headJsonObj.put(Tags.RequestKey.URL, "");
            headJsonObj.put(Tags.RequestKey.METHOD, requestMethod);
            headJsonObj.put(Tags.RequestKey.OPERATORID, "");
            headJsonObj.put(Tags.RequestKey.OPERATORMIHOME, "");
            headJsonObj.put(Tags.RequestKey.APITYPE, Tags.RequestValue.APITYPE);
            jsonObject.put(Tags.RequestKey.HEADER, headJsonObj);
            jsonObject.put(Tags.RequestKey.BODY, body);
        } catch (JSONException e) {
            LogUtil.i(TAG, "creatRequestJson error:" + e);
            return requestJson;
        }
        requestJson = jsonObject.toString();
        if (!TextUtils.isEmpty(requestJson)) {
            String base64 = encrypt(requestJson);
            base64 = base64.trim().replace("\\r", " ").replace("\\n", " ");
            return base64;
        }
        return requestJson;
    }

    /**
     * base64加密处理
     * 
     * @param key
     * @return
     */
    public static String encrypt(String key) {
        return (new BASE64Encoder()).encodeBuffer(key.getBytes());
    }

    /**
     * base64解密处理
     * 
     * @param data
     * @return
     * @throws IOException
     */
    public static String decrypt(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(data);
        return new String(bytes);
    }

}
