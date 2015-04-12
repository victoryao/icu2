
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class RemindInfo {
    private int mUnPayCount;
    private int mExpressCount;
    private int mReviewCount;

    public RemindInfo(int unpay, int express, int review) {
        mUnPayCount = unpay;
        mExpressCount = express;
        mReviewCount = review;
    }

    public int getUnPayCount() {
        return mUnPayCount;
    }

    public int getExpressCount() {
        return mExpressCount;
    }

    public int getReviewCount() {
        return mReviewCount;
    }

    public static RemindInfo fromJSONObject(JSONObject json) {
        try {
            if (Tags.isJSONReturnedOK(json)) {
                String bodyStr = json.optString(Tags.BODY);
                if (!TextUtils.isEmpty(bodyStr)) {
                    JSONObject body = new JSONObject(bodyStr);
                    if (body != null) {
                        int unpay = body.optInt(Tags.RemindInfo.JSON_KEY_TOPAYCOUNT);
                        int express = body.optInt(Tags.RemindInfo.JSON_KEY_TOARRIVECOUNT);
                        int review = body.optInt(Tags.RemindInfo.JSON_KEY_REVIEW);
                        return new RemindInfo(unpay, express, review);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
