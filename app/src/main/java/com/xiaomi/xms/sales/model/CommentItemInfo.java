
package com.xiaomi.xms.sales.model;

import android.view.View;

import org.json.JSONObject;

public class CommentItemInfo {

    private String mUserName;
    private String mContent;
    private float mAverageGrade;
    private long mAddTime;
    private int mBottomLineVisibility;

    public CommentItemInfo(String userName, String content, float averageGrade, long addTime) {
        mUserName = userName;
        mContent = content;
        mAverageGrade = averageGrade;
        mAddTime = addTime;
        mBottomLineVisibility = View.VISIBLE;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getContent() {
        return mContent;
    }

    public Long getAddTime() {
        return mAddTime;
    }

    public float getAverageGrade() {
        return mAverageGrade;
    }

    public int getBottomLineVisibility() {
        return mBottomLineVisibility;
    }

    public void setBottomLineVisibility(int visibility) {
        mBottomLineVisibility = visibility;
    }

    public static CommentItemInfo valueOf(JSONObject one) {
        if (one != null) {
            String userName = one.optString(Tags.CommentInfo.USER_NAME);
            String content = one.optString(Tags.CommentInfo.COMMENT_CONTENT);
            double averageGrade = one.optDouble(Tags.CommentInfo.AVERAGE_GRADE);
            long time = one.optLong(Tags.CommentInfo.ADD_TIME);
            return new CommentItemInfo(userName, content, (float) averageGrade, time);
        }
        return null;
    }
}
