
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item.FeatureItem;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item.MediaItem;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item.RecommendItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MiPhoneDetailInfo {
    public static final String TAG = "MiPhoneDetailInfo";
    private Image mFocusImg;
    private ArrayList<Item> mItems;
    private String mPhoneType;
    private String mNextItem;
    private String mLastItem;
    private boolean mNextIsPhone;
    private boolean mLastIsPhone;

    public Image getFocusImg() {
        return mFocusImg;
    }

    public void setFocusImg(Image focusImg) {
        mFocusImg = focusImg;
    }

    public String getPhoneType() {
        return mPhoneType;
    }

    public void setPhoneType(String phoneType) {
        mPhoneType = phoneType;
    }

    public String getNextItem() {
        return mNextItem;
    }

    public void setNextItem(String nextItem) {
        mNextItem = nextItem;
    }

    public String getLastItem() {
        return mLastItem;
    }

    public void setLastItem(String lastItem) {
        mLastItem = lastItem;
    }

    public boolean getNextIsPhone() {
        return mNextIsPhone;
    }

    public void setNextIsPhone(boolean isPhone) {
        mNextIsPhone = isPhone;
    }

    public boolean getLastIsPhone() {
        return mLastIsPhone;
    }

    public void setLastIsPhone(boolean isPhone) {
        mLastIsPhone = isPhone;
    }

    public ArrayList<Item> getItem() {
        return mItems;
    }

    public void setItem(ArrayList<Item> item) {
        mItems = item;
    }

    public static class Item {
        public final static int TYPE_MEDIA = 1;
        public final static int TYPE_FEATURES = 2;
        public final static int TYPE_GALLERY = 3;
        public final static int TYPE_MAX_COUNT = 4;
        private int mType;
        private Node mNode;

        public int getType() {
            return mType;
        }

        public void setType(int type) {
            this.mType = type;
        }

        public Node getNode() {
            return mNode;
        }

        public void setNode(Node node) {
            this.mNode = node;
        }

        public static abstract class Node {

        }

        public static class MediaItem extends Node {
            private Image mMediaItemImg;
            private String mMediaItemUrl;
            private String mMediaItemText;

            public void setMediaItemImg(Image mediaItemImg) {
                mMediaItemImg = mediaItemImg;
            }

            public Image getMediaItemImg() {
                return mMediaItemImg;
            }

            public void setMediaItemUrl(String mediaItemUrl) {
                mMediaItemUrl = mediaItemUrl;
            }

            public String getMediaItemUrl() {
                return mMediaItemUrl;
            }

            public void setMediaItemText(String mediaItemText) {
                mMediaItemText = mediaItemText;
            }

            public String getMediaItemText() {
                return mMediaItemText;
            }
        }

        public static class FeatureItem extends Node {
            private Image mFeatureItemImg;
            private String mFeatureName;
            private ArrayList<String> mFeatureDetails;

            public void setFeatureItemImg(Image featureItemImg) {
                mFeatureItemImg = featureItemImg;
            }

            public Image getFeatureItemImg() {
                return mFeatureItemImg;
            }

            public void setFeatureName(String featureName) {
                mFeatureName = featureName;
            }

            public String getFeatureName() {
                return mFeatureName;
            }

            public void setFeatureDetail(ArrayList<String> featureDetails) {
                mFeatureDetails = featureDetails;
            }

            public ArrayList<String> getFeatureDetail() {
                return mFeatureDetails;
            }
        }

        public static class RecommendItem extends Node {
            private Image mGalleryItemImg;
            private String mProductId;
            private String mProductName;
            private String mProductBrief;
            private String mProductPrice;
            private boolean mIsCanBuy;
            private int mIsPhone;
            private String mActivityUrl;

            public void setRecommendItemImg(Image mediaItemImg) {
                mGalleryItemImg = mediaItemImg;
            }

            public Image getRecommendItemImg() {
                return mGalleryItemImg;
            }

            public String getProductBrief() {
                return mProductBrief;
            }

            public void setProductBrief(String productBrief) {
                mProductBrief = productBrief;
            }

            public String getProductName() {
                return mProductName;
            }

            public void setProductName(String productName) {
                mProductName = productName;
            }

            public String getProductId() {
                return mProductId;
            }

            public void setProductId(String productId) {
                mProductId = productId;
            }

            public String getProductPrice() {
                return mProductPrice;
            }

            public void setProductPrice(String productPrice) {
                mProductPrice = productPrice;
            }

            public void setIsCanBuy(boolean isCanBuy) {
                mIsCanBuy = isCanBuy;
            }

            public boolean getIsCanBuy() {
                return mIsCanBuy;
            }

            public void setIsPhone(int isphone) {
                mIsPhone = isphone;
            }

            public int isPhone() {
                return mIsPhone;
            }

            public void setActivityUrl(String activityUrl) {
                mActivityUrl = activityUrl;
            }

            public String getActivityUrl() {
                return mActivityUrl;
            }
        }
    }

    public static MiPhoneDetailInfo valueOf(JSONObject json) throws JSONException {
        MiPhoneDetailInfo info = null;

        if (Tags.isJSONResultOK(json)) {
            info = new MiPhoneDetailInfo();
            JSONObject resultjson = json.getJSONObject(Tags.DATA);
            if (resultjson != null) {
                String focusImg = resultjson.getString(Tags.MiPhoneDetails.FOCUS_IMG);
                info.setFocusImg(new Image(focusImg));
                String phoneType = resultjson.optString(Tags.MiPhoneDetails.PHONE_TYPE);
                info.setPhoneType(phoneType);

                JSONObject nextItemJson = resultjson.optJSONObject(Tags.ProductDetails.NEXT_ITEM);
                if (nextItemJson != null) {
                    Iterator<String> it = nextItemJson.keys();
                    while (it.hasNext()) {
                        if (TextUtils.equals(it.next(), Tags.ProductDetails.PRODUCT_ID)) {
                            info.setNextItem(nextItemJson.optString(Tags.ProductDetails.PRODUCT_ID));
                        } else {
                            info.setNextIsPhone(nextItemJson
                                    .optBoolean(Tags.ProductDetails.IS_PHONE));
                        }
                    }
                }

                JSONObject lastItemJson = resultjson.optJSONObject(Tags.ProductDetails.LAST_ITEM);
                if (lastItemJson != null) {
                    Iterator<String> it = lastItemJson.keys();
                    while (it.hasNext()) {
                        if (TextUtils.equals(it.next(), Tags.ProductDetails.PRODUCT_ID)) {
                            info.setLastItem(lastItemJson.optString(Tags.ProductDetails.PRODUCT_ID));
                        } else {
                            info.setLastIsPhone(lastItemJson
                                    .optBoolean(Tags.ProductDetails.IS_PHONE));
                        }
                    }
                }

                Item item = null;
                ArrayList<Item> items = new ArrayList<Item>();
                JSONArray featureJsonArray = resultjson.getJSONArray(Tags.MiPhoneDetails.FEATURES);
                if (featureJsonArray != null) {
                    for (int i = 0; i < featureJsonArray.length(); i++) {
                        if (!featureJsonArray.isNull(i)) {
                            item = new Item();
                            FeatureItem fItem = new FeatureItem();
                            JSONObject featureItemJsonObject = (JSONObject) featureJsonArray.get(i);
                            String featureName = featureItemJsonObject
                                    .getString(Tags.MiPhoneDetails.FEATURE_NAME);
                            String featureItemImg = featureItemJsonObject
                                    .getString(Tags.MiPhoneDetails.IMG);
                            JSONArray featureDetailJsonArray = featureItemJsonObject
                                    .getJSONArray(Tags.MiPhoneDetails.DETAILS);
                            ArrayList<String> detail = new ArrayList<String>();
                            if (featureDetailJsonArray != null) {
                                for (int j = 0; j < featureDetailJsonArray.length(); j++) {
                                    if (!featureDetailJsonArray.isNull(j)) {
                                        detail.add((String) featureDetailJsonArray.get(j));
                                    }
                                }
                            }
                            fItem.setFeatureItemImg(new Image(featureItemImg));
                            fItem.setFeatureName(featureName);
                            fItem.setFeatureDetail(detail);
                            item.setType(Item.TYPE_FEATURES);
                            item.setNode(fItem);
                            items.add(item);
                        }
                    }
                }

                JSONArray mediaJsonArray = resultjson.getJSONArray(Tags.MiPhoneDetails.MEDIA);
                if (mediaJsonArray != null) {
                    for (int i = 0; i < mediaJsonArray.length(); i++) {
                        if (!mediaJsonArray.isNull(i)) {
                            item = new Item();
                            MediaItem medItem = new MediaItem();
                            JSONObject mediaItemJsonObject = (JSONObject) mediaJsonArray.get(i);
                            String meidaItemImg = mediaItemJsonObject
                                    .getString(Tags.MiPhoneDetails.IMG);
                            String meidaItemUrl = mediaItemJsonObject
                                    .getString(Tags.MiPhoneDetails.URL);
                            String meidaItemIText = mediaItemJsonObject
                                    .getString(Tags.MiPhoneDetails.TEXT);
                            medItem.setMediaItemImg(new Image(meidaItemImg));
                            medItem.setMediaItemUrl(meidaItemUrl);
                            medItem.setMediaItemText(meidaItemIText);
                            item.setNode(medItem);
                            item.setType(Item.TYPE_MEDIA);
                            items.add(item);
                        }
                    }
                }

                JSONArray galleryJsonArray = resultjson.getJSONArray(Tags.MiPhoneDetails.GALLERY);
                if (galleryJsonArray != null) {
                    for (int i = 0; i < galleryJsonArray.length(); i++) {
                        if (!galleryJsonArray.isNull(i)) {
                            item = new Item();
                            RecommendItem gItem = new RecommendItem();
                            JSONObject galleryItemJsonObject = (JSONObject) galleryJsonArray.get(i);
                            String productId = galleryItemJsonObject
                                    .getString(Tags.MiPhoneDetails.PRODUCT_ID);
                            String productName = galleryItemJsonObject
                                    .getString(Tags.MiPhoneDetails.PRODUCT_NAME);
                            String productBrief = galleryItemJsonObject
                                    .getString(Tags.MiPhoneDetails.PRODUCT_BRIEF);
                            String productPrice = galleryItemJsonObject
                                    .getString(Tags.MiPhoneDetails.PRODUCT_PRICE);
                            String productImg = galleryItemJsonObject
                                    .getString(Tags.MiPhoneDetails.PRODUCT_IMG);
                            String activityUrl = galleryItemJsonObject
                                    .optString(Tags.MiPhoneDetails.ACTIVITY_URL);
                            int hasCanBuy = galleryItemJsonObject
                                    .optInt(Tags.MiPhoneDetails.IS_AVAIL);
                            int isPhone = galleryItemJsonObject
                                    .optInt(Tags.MiPhoneDetails.IS_PHONE);
                            gItem.setRecommendItemImg(new Image(productImg));
                            gItem.setProductId(productId);
                            gItem.setProductName(productName);
                            gItem.setProductBrief(productBrief);
                            gItem.setProductPrice(productPrice);
                            gItem.setIsPhone(isPhone);
                            gItem.setActivityUrl(activityUrl);
                            if (hasCanBuy == 1) {
                                gItem.setIsCanBuy(true);
                            }
                            item.setType(Item.TYPE_GALLERY);
                            item.setNode(gItem);
                            items.add(item);
                        }
                    }
                }
                info.setItem(items);
            }
        }
        return info;
    }
}
