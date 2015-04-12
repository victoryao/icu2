/**
 * @author
 * @since
 **/

package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
    private String mUserId;
    private String mUserName;
    private String mOrgId;
    private String mOrgName;
    private String mName;
    private String mAuths;

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getOrgId() {
        return mOrgId;
    }

    public void setOrgId(String mOrgId) {
        this.mOrgId = mOrgId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getOrgName() {
        return mOrgName;
    }

    public void setOrgName(String mOrgName) {
        this.mOrgName = mOrgName;
    }

    public static UserInfo fromJSONObject(JSONObject json) {
        UserInfo userInfo = null;
        try {
            if (Tags.isJSONReturnedOK(json)) {
                String bodyStr = json.optString(Tags.BODY);
                if (!TextUtils.isEmpty(bodyStr)) {
                    JSONObject body = new JSONObject(bodyStr);
                    if (body != null) {
                        userInfo = new UserInfo();
                        userInfo.setName(body.optString(Tags.UserInfo.JSON_KEY_NAME));
                        String userName = body.optString(Tags.UserInfo.JSON_KEY_USER_NAME);
                        userInfo.setUserName(userName);
                        userInfo.setUserId(body.optString(Tags.UserInfo.JSON_KEY_USER_ID));
                        String orgId = body.optString(Tags.UserInfo.JSON_KEY_ORGID);
                        userInfo.setOrgId(orgId);
                        String orgName = body.optString(Tags.UserInfo.JSON_KEY_ORGNAME);
                        userInfo.setOrgName(orgName);
                        String auths = body.optString(Tags.UserInfo.JSON_KEY_AUTHS);
                        userInfo.setmAuths(auths);
                        Utils.Preference.setStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_NAME, userName);
                        Utils.Preference.setStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, orgId);
                        Utils.Preference.setStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGNAME, orgName);
                        Utils.Preference.setStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_AUTHS, auths);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

	public String getmAuths() {
		return mAuths;
	}

	public void setmAuths(String mAuths) {
		this.mAuths = mAuths;
	}
}
