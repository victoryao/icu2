
package com.xiaomi.xms.sales.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PhoneModelInfo {
    private String mPhone;
    private String mPhoneSymbol;
    private Integer mCode;
    private String mImageText;
    private Image mImage;

    //phoneCode为接口查询数据库所需字段，因为之前的接口中已经使用，为了统一，在此处也用该字段来做判断
    public PhoneModelInfo(String phoneName, String phoneSymbol, Integer phoneCode, String imageText, Image image) {
        mPhoneSymbol = phoneSymbol;
        mPhone = phoneName;
        mCode = phoneCode;
        mImageText = imageText;
        mImage = image;
    }

    public String getPhoneName() {
        return mPhone;
    }

    public String getPhoneSymbol() {
        return mPhoneSymbol;
    }

    public Integer getPhoneCode() {
        return mCode;
    }

    public String getText() {
        return mImageText;
    }

    public Image getImage() {
        return mImage;
    }

    public static ArrayList<PhoneModelInfo> valueOf(JSONObject json) throws JSONException {
        ArrayList<PhoneModelInfo> list = null;
        if (Tags.isJSONResultOK(json)) {
            list = new ArrayList<PhoneModelInfo>();
            JSONArray jsonData = json.optJSONArray(Tags.DATA);
            if (jsonData != null) {
                for (int i = 0; i < jsonData.length(); i++) {
                    if (!jsonData.isNull(i)) {
                        JSONObject jsonadapt = jsonData.getJSONObject(i);
                        String name = jsonadapt.optString(Tags.PhoneModel.NAME);
                        String symbol = jsonadapt.optString(Tags.PhoneModel.SYMBOL);
                        Integer code = jsonadapt.optInt(Tags.PhoneModel.CODE);
                        String imageText = jsonadapt.optString(Tags.PhoneModel.TEXT);
                        String imageUrl = jsonadapt.optString(Tags.PhoneModel.IMAGE_URL);
                        list.add(new PhoneModelInfo(name, symbol, code, imageText,new Image(imageUrl)));
                    }
                }
            }
        }
        return list;
    }

}
