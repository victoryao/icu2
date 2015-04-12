
package com.xiaomi.xms.sales.model;

import org.json.JSONObject;

public class CommentSumInfo {

    private int mGood;
    private int mGeneral;
    private int mBad;
    private int mTotal;

    public CommentSumInfo(int good, int general, int bad) {
        mGood = good;
        mGeneral = general;
        mBad = bad;
        mTotal = mGood + mGeneral + mBad;
    }

    public int getGood() {
        return 100 - getGeneral() - getBad();
    }

    public int getGeneral() {
        if (mTotal == 0)
            return 0;
        return mGeneral * 100 / mTotal;
    }

    public int getBad() {
        if (mTotal == 0)
            return 0;
        return mBad * 100 / mTotal;
    }

    public int getTotal() {
        return mTotal;
    }

    public static CommentSumInfo valueOf(JSONObject json) {
        if (Tags.isJSONResultOK(json)) {
            JSONObject data = json.optJSONObject(Tags.DATA);
            if (data != null) {
                int good = data.optInt(Tags.CommentInfo.COMMENTS_GOOD);
                int general = data.optInt(Tags.CommentInfo.COMMENTS_GENERAL);
                int bad = data.optInt(Tags.CommentInfo.COMMENTS_BAD);
                return new CommentSumInfo(good, general, bad);
            }
        }
        return null;
    }
}
