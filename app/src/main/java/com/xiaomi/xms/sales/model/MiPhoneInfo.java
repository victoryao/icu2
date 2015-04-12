
package com.xiaomi.xms.sales.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MiPhoneInfo {

    private String mProductId;
    private String mProductName;
    private String mProductPrice;
    private String mBrief;
    private String mDescribe;
    private String mDescribe2;
    private String mLeftText;
    private String mRightText;
    private String mLeftUrl;
    private String mRightUrl;
    private String mProductUrl;
    private String mDisplayType;
    private Image mImage;

    public MiPhoneInfo(String productId, String productName, String productPrice, String brief,
            String describe, String describe2, String leftText, String rightText, String leftUrl,
            String rightUrl,
            String productUrl, Image image, String displayType) {
        mProductId = productId;
        mProductName = productName;
        mProductPrice = productPrice;
        mBrief = brief;
        mDescribe = describe;
        mDescribe2 = describe2;
        mLeftText = leftText;
        mRightText = rightText;
        mLeftUrl = leftUrl;
        mRightUrl = rightUrl;
        mProductUrl = productUrl;
        mImage = image;
        mDisplayType = displayType;
    }

    public String getProductPrice() {
        return mProductPrice;
    }

    public Image getImage() {
        return mImage;
    }

    public String getProductName() {
        return mProductName;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getBrief() {
        return mBrief;
    }

    public String getDescribe() {
        return mDescribe;
    }

    public String getDescribe2() {
        return mDescribe2;
    }

    public String getLeftText() {
        return mLeftText;
    }

    public String getRightText() {
        return mRightText;
    }

    public String getLeftUrl() {
        return mLeftUrl;
    }

    public String getRightUrl() {
        return mRightUrl;
    }

    public String getProductUrl() {
        return mProductUrl;
    }

    public String getDisplayType() {
        return mDisplayType;
    }

    public static ArrayList<MiPhoneInfo> valueOf(JSONObject json) throws JSONException {
        ArrayList<MiPhoneInfo> list = null;
        if (Tags.isJSONResultOK(json)) {
            JSONArray productJsonArray = json.getJSONObject(Tags.DATA)
                    .getJSONArray(Tags.Product.PRODUCT);
            if (productJsonArray != null) {
                list = new ArrayList<MiPhoneInfo>();
                for (int i = 0; i < productJsonArray.length(); i++) {
                    if (!productJsonArray.isNull(i)) {
                        JSONObject proJsonobject = (JSONObject) productJsonArray
                                .get(i);
                        String productId = proJsonobject.getString(Tags.MiPhone.PRODUCT_ID);
                        String productName = proJsonobject.getString(Tags.MiPhone.PRODUCT_NAME);
                        String price = proJsonobject.getString(Tags.MiPhone.PRICE);
                        String brief = proJsonobject.getString(Tags.MiPhone.BRIEF);
                        String describe = proJsonobject.getString(Tags.MiPhone.DESCRIBE);
                        String describe2 = proJsonobject.getString(Tags.MiPhone.DESCRIBE_TWO);
                        String leftText = proJsonobject.getString(Tags.MiPhone.LEFT_BUTTON);
                        String rightText = proJsonobject.getString(Tags.MiPhone.RIGHT_BUTTON);
                        String leftUrl = proJsonobject.getString(Tags.MiPhone.LEFT_URL);
                        String rightUrl = proJsonobject.getString(Tags.MiPhone.RIGHT_URL);
                        String imageUrl = proJsonobject.getString(Tags.MiPhone.IMAGE_URL);
                        String productUrl = proJsonobject.getString(Tags.MiPhone.PRODUCT_URL);
                        String displayType = proJsonobject.optString(Tags.MiPhone.DISPLAY_TYPE,
                                Tags.Product.DISPLAY_NATIVE);
                        list.add(new MiPhoneInfo(productId, productName, price, brief, describe,
                                describe2, leftText, rightText, leftUrl,
                                rightUrl, productUrl, new Image(imageUrl), displayType));
                    }
                }
            }
        }
        return list;
    }
}
