
package com.xiaomi.xms.sales.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MiHomeCheckInfo {

    private String mMihomeId;
    private String mMihomeName;
    private String mCheckInCount;
    private Image mImage;
    private String mDesc;
    private String mColor;

    public MiHomeCheckInfo(String mihomeId, String mihomeName, String checkInCount, String desc, Image image,
            String color) {
        mMihomeId = mihomeId;
        mMihomeName = mihomeName;
        mCheckInCount = checkInCount;
        mDesc = desc;
        mImage = image;
        mColor = color;
    }

    public String getMihomeId() {
               return mMihomeId;
          }

    public String getCheckInCount() {
        return mCheckInCount;
    }

    public String getMihomeName() {
        return mMihomeName;
    }

    public String getDesc() {
        return mDesc;
    }

    public Image getImage() {
        return mImage;
    }

    public String getColor() {
        return mColor;
    }

    public static MiHomeCheckInfo fromJSONObject(JSONObject json) {
        try {
            JSONObject data = json.getJSONObject(Tags.DATA);
            String checkInCount = data.getString(Tags.MihomeCheckInfo.SIGNS);
            String mihomeName = data.getString(Tags.MihomeCheckInfo.MIHOME_NAME);
            String desc = data.getString(Tags.MihomeCheckInfo.DESC);
            String url = data.getString(Tags.MihomeCheckInfo.IMAGE_URL);
            String color = data.getString(Tags.MihomeCheckInfo.COLOR);
            String mihomeId = data.getString(Tags.MihomeCheckInfo.CLIENT_MIHOME_ID);
            return new MiHomeCheckInfo(mihomeId, mihomeName, checkInCount, desc, new Image(url), color);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
