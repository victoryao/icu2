
package com.xiaomi.xms.sales.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FcodeSelectProduct {

    private String mProductId;
    private String mPrice;
    private String mImageURL;
    private String mName;
    private String mBrief;
    private String mSingleImage;
    private boolean mIsChange;

    public FcodeSelectProduct(String productId, String price, String image, String name,
            String brief, boolean isChange, String singleImage) {
        mProductId = productId;
        mPrice = price;
        mImageURL = image;
        mName = name;
        mBrief = brief;
        mIsChange = isChange;
        mSingleImage = singleImage;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getPrice() {
        return mPrice;
    }

    public Image getImage() {
        return new Image(mImageURL);
    }

    public String getName() {
        return mName;
    }

    public String getBrief() {
        return mBrief;
    }

    public boolean isChangeStyle() {
        return mIsChange;
    }

    public Image getSingleImage() {
        return new Image(mSingleImage);
    }

    public static ArrayList<FcodeSelectProduct> valueOf(String jsonStr) {
        ArrayList<FcodeSelectProduct> list = new ArrayList<FcodeSelectProduct>();
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject one = array.getJSONObject(i);
                String productId = one.optString(Tags.FCodeSelectProduct.PRODUCT_ID);
                String price = one.optString(Tags.FCodeSelectProduct.PRICE);
                JSONObject images = one.optJSONObject(Tags.FCodeSelectProduct.IMAGE_URL);
                String imageURL;
                String singleImageURL;
                if (images == null) {
                    imageURL = one.optString(Tags.FCodeSelectProduct.IMAGE_URL);
                    singleImageURL = imageURL;
                } else {
                    imageURL = images.optString(Tags.FCodeSelectProduct.SIZE);
                    singleImageURL = images.optString(Tags.FCodeSelectProduct.SIZE_SINGLE);
                }
                String name = one.optString(Tags.FCodeSelectProduct.NAME);
                String brief = one.optString(Tags.FCodeSelectProduct.BRIEF);
                boolean isChange = one.optBoolean(Tags.FCodeSelectProduct.IS_CHANGE_STYLE);
                list.add(new FcodeSelectProduct(productId, price, imageURL, name, brief, isChange,
                        singleImageURL));
            }
        } catch (JSONException e) {
            return null;
        }
        return list;
    }
}
