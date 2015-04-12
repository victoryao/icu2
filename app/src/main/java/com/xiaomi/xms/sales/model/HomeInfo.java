
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import com.xiaomi.xms.sales.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeInfo {
    public static final String TAG = "HomeInfo";

    private Image mHomeThumbnail;
    private Image mHomePhoto;
    private int mItemType;
    private String mActivityUrl;
    private String mProductId;
    private String mProductName;
    private String mProductDetail;
    private String mProductPrice;
    private String mFullPrice;
    private Image mActivityIcon;
    public Image mHomeBigPhoto;

    // 预留的扩展字段，如果服务端需要添加新字段，旧版本app从该字段内解析内容进行显示
    private ArrayList<?> mProductExt;
    public static final int ITEM_TYPE_AD = 0;
    public static final int ITEM_TYPE_PRODUCT = 1;
    public static final int ITEM_TYPE_MIPHONE = 2;
    public static final int ITEM_TYPE_FULLSCREEN_AD = 3;

    public HomeInfo(Image homeThumbnail, Image homePhoto, int itemType, String activityUrl, String productId,
            String productName, String productDetail, String productPrice, String fullPrice,
            Image activityIcon, ArrayList<?> productExt, Image homeBigPhoto) {
        mHomeThumbnail = homeThumbnail;
        mHomePhoto = homePhoto;
        mItemType = itemType;
        mActivityUrl = activityUrl;
        mProductId = productId;
        mProductName = productName;
        mProductDetail = productDetail;
        mProductPrice = productPrice;
        mFullPrice = fullPrice;
        mActivityIcon = activityIcon;
        mProductExt = productExt;
        mHomeBigPhoto = homeBigPhoto;
    }

    public static ArrayList<HomeInfo> valueOf(JSONObject json) throws JSONException {
        LogUtil.d(TAG, "parseResult:" + json);
        ArrayList<HomeInfo> list = null;
        if (Tags.isJSONResultOK(json)) {
            JSONArray itemsJsonArray = json.getJSONObject(Tags.DATA).getJSONArray(
                    Tags.Home.ITEMS);
            // JSONArray itemsJsonArray = json.getJSONArray(Tags.DATA);
            if (itemsJsonArray != null) {
                list = new ArrayList<HomeInfo>();
                for (int i = 0; i < itemsJsonArray.length(); i++) {
                    if (!itemsJsonArray.isNull(i)) {
                        JSONObject itemJsonObject = (JSONObject) itemsJsonArray.get(i);
                        int itemType = itemJsonObject.getInt(Tags.Home.ITEM_TYPE);
                        String thumbnailUrl = itemJsonObject.getString(Tags.Home.THUMBNAIL_URL);
                        String photoUrl = itemJsonObject.optString(Tags.Home.PHOTO_URL);
                        HomeInfo entry = null;
                        // 展示项是产品
                        JSONObject productJsonObject = itemJsonObject
                                .getJSONObject(Tags.Home.PRODUCT);
                        LogUtil.d(TAG, productJsonObject.toString());
                        String productId = productJsonObject
                                .getString(Tags.Home.PRODUCT_ID);
                        String productName = productJsonObject
                                .getString(Tags.Home.PRODUCT_NAME);
                        String productDetail = productJsonObject
                                .getString(Tags.Home.PRODUCT_DETAIL);
                        String productPrice = productJsonObject
                                .optString(Tags.Home.PRODUCT_PRICE);
                        String fullPrice = productJsonObject
                                .optString(Tags.Home.FULL_PRICE);
                        String activityIcon = productJsonObject
                                .getString(Tags.Home.ACTIVITY_ICON);
                        Image iconImage = (TextUtils.isEmpty(activityIcon)) ? null
                                : new Image(activityIcon);
                        String activityUrl = itemJsonObject.getString(Tags.Home.ACTIVITY_URL);
                        String bigImageUrl = itemJsonObject.optString(Tags.Home.BIG_PHOTO_URL);
                        Image homeBigPhoto = null;
                        if (!TextUtils.isEmpty(bigImageUrl)) {
                            homeBigPhoto = new Image(bigImageUrl);
                        }

                        entry = new HomeInfo(new Image(thumbnailUrl), new Image(photoUrl), itemType, activityUrl, productId,
                                productName, productDetail, productPrice, fullPrice,
                                iconImage, null, homeBigPhoto);
                        list.add(entry);
                    }
                }
            }
        }
        return list;
    }

    public Image getHomeThumbnail() {
        return mHomeThumbnail;
    }

    public Image getHomePhoto() {
        return mHomePhoto;
    }

    public int getItemType() {
        return mItemType;
    }

    public String getActivityUrl() {
        return mActivityUrl;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getProductName() {
        return mProductName;
    }

    public String getProductDetail() {
        return mProductDetail;
    }

    public String getProductPrice() {
        return mProductPrice;
    }

    public String getFullPrice() {
        return mFullPrice;
    }

    public Image getActivityIcon() {
        return mActivityIcon;
    }

    public ArrayList<?> getProductExt() {
        return mProductExt;
    }

    public Image getHomeBigPhoto() {
        return mHomeBigPhoto;
    }
}
