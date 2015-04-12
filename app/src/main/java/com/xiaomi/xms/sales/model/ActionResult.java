
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONObject;

public class ActionResult {

    private String mMessage;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public static ActionResult valueOf(JSONObject json) {
        if (json != null) {
            ActionResult result = new ActionResult();
            JSONObject dataJson = json.optJSONObject(Tags.DATA);
            if (dataJson != null
                    && TextUtils.equals(dataJson.optString(Tags.RESULT), Tags.RESULT_TRUE)) {
                result.setMessage(Tags.RESULT_OK);
            } else if (dataJson == null
                    && TextUtils.equals(json.optString(Tags.RESULT), "error")) {
                result.setMessage(json.optString(Tags.DESCRIPTION));
            }
            return result;
        }
        return null;
    }
}
