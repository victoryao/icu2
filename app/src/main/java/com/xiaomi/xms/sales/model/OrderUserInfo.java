
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderUserInfo {

    private String mUserName;
    private String mUserTel;
    private String mUserEmail;
    private String mInvoiceType;
    private String mInvoiceTitle;

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getUserTel() {
        return mUserTel;
    }

    public void setUserTel(String mUserTel) {
        this.mUserTel = mUserTel;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public void setUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
    }

    public String getInvoiceType() {
        return mInvoiceType;
    }

    public void setInvoiceType(String mInvoiceType) {
        this.mInvoiceType = mInvoiceType;
    }

    public String getInvoiceTitle() {
        return mInvoiceTitle;
    }

    public void setInvoiceTitle(String mInvoiceTitle) {
        this.mInvoiceTitle = mInvoiceTitle;
    }

    public static OrderUserInfo fromJSONObject(JSONObject json) {
        OrderUserInfo orderUserInfo = null;
        try {
            if (Tags.isJSONReturnedOK(json)) {
                String bodyStr = json.optString(Tags.BODY);
                if (!TextUtils.isEmpty(bodyStr)) {
                    JSONObject body = new JSONObject(bodyStr);
                    if (body != null) {
                        orderUserInfo = new OrderUserInfo();
                        orderUserInfo.setUserName(body.optString("consignee"));
                        orderUserInfo.setUserTel(body.optString("tel"));
                        orderUserInfo.setUserEmail(body.optString("email"));
                        orderUserInfo.setInvoiceType(body.optString("invoiceType"));
                        orderUserInfo.setInvoiceTitle(body.optString("invoiceTitle"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderUserInfo;
    }
}
