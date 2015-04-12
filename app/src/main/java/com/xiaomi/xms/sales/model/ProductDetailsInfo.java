
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ProductDetailsInfo implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TAG = "ProductDetailsInfo";
    private String mProductId;
    private String mProductName;
    private String mProductBrief;
    private String mProductPrice;
    private String mStyleName;
    private String mProductMarketPrice;
    private String mNextItem;
    private String mLastItem;
    private boolean mNextIsPhone;
    private boolean mLastIsPhone;
    private boolean mHasProduct;
    private boolean mIsChoiceCombo;
    private int mBuyNumber;
    private ArrayList<ProductDetailsInfoItem> mItems;
    private ArrayList<CanJoinActsItem> mCanJoinActsList;
    private LinkedHashMap<String, String> mAdaptPhone;
    private Image mSupplyImage;
    private Image mStylePhoto;
    private ArrayList<StyleItem> mStyleList;

    public ProductDetailsInfo() {
        mItems = new ArrayList<ProductDetailsInfoItem>();
        mCanJoinActsList = new ArrayList<CanJoinActsItem>();
        mStyleList = new ArrayList<StyleItem>();
    }

    private void setProductsName(String productName) {
        mProductName = productName;
    }

    public String getProductsName() {
        return mProductName;
    }

    private void setStyleName(String styleName) {
        mStyleName = styleName;
    }

    public String getStyleName() {
        return mStyleName;
    }

    public String getProductBrief() {
        return mProductBrief;
    }

    public void setProductBrief(String productBrief) {
        mProductBrief = productBrief;
    }

    public int getBuyNumber() {
        return mBuyNumber;
    }

    public void setBuyNumber(int buyNumber) {
        mBuyNumber = buyNumber;
    }

    public String getProductPrice() {
        return mProductPrice;
    }

    public void setProductPrice(String productPrice) {
        mProductPrice = productPrice;
    }

    public boolean isChoiceCombo() {
        return mIsChoiceCombo;
    }

    public void setIsChoiceCombo(boolean isChoiceCombo) {
        mIsChoiceCombo = isChoiceCombo;
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

    public void setAdaptPhone(LinkedHashMap<String, String> adaptPhone) {
        mAdaptPhone = adaptPhone;
    }

    public LinkedHashMap<String, String> getAdaptPhone() {
        return mAdaptPhone;
    }

    public boolean hasProduct() {
        return mHasProduct;
    }

    public void setHasProduct(boolean hasProduct) {
        mHasProduct = hasProduct == false ? true : false;
    }

    public ArrayList<ProductDetailsInfoItem> getItems() {
        return mItems;
    }

    public void setItems(ArrayList<ProductDetailsInfoItem> items) {
        this.mItems = items;
    }

    public String getProductMarketPrice() {
        return mProductMarketPrice;
    }

    public void setProductMarketPrice(String productMarketPrice) {
        mProductMarketPrice = productMarketPrice;
    }

    public Image getSupplyImage() {
        return mSupplyImage;
    }

    public void setSupplyImage(Image supplyImage) {
        mSupplyImage = supplyImage;
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

    public void setStylePhoto(Image stylePhoto) {
        mStylePhoto = stylePhoto;
    }

    public Image getStylePhoto() {
        return mStylePhoto;
    }

    public ArrayList<CanJoinActsItem> getCanJoinActsList() {
        return mCanJoinActsList;
    }

    public void setCanJoinActsList(ArrayList<CanJoinActsItem> canJoinActsList) {
        this.mCanJoinActsList = canJoinActsList;
    }

    public ArrayList<StyleItem> getStyleList() {
        return mStyleList;
    }

    public void setStyleList(ArrayList<StyleItem> styleList) {
        mStyleList = styleList;
    }

    public static ProductDetailsInfo valueOf(JSONObject json) throws JSONException {
        ProductDetailsInfo info = null;

        if (Tags.isJSONResultOK(json)) {
            JSONObject dataJson = json.optJSONObject(Tags.DATA);
            if (dataJson != null) {
                info = new ProductDetailsInfo();
                JSONObject resultjson = dataJson.optJSONObject(
                        Tags.ProductDetails.RESULT);

                info.setProductId(resultjson.optString(Tags.ProductDetails.PRODUCT_ID));

                info.setProductsName(resultjson.optString(Tags.ProductDetails.PRODUCT_NAME));

                info.setProductPrice(resultjson.optString(Tags.ProductDetails.PRICE));

                info.setStyleName(resultjson.optString(Tags.ProductDetails.STYLE_NAME));

                info.setProductBrief(resultjson.optString(Tags.ProductDetails.PRODUCT_BRIEF));

                info.setBuyNumber(resultjson.optInt(Tags.ProductDetails.BUY_LIMIT));

                info.setHasProduct(resultjson.optBoolean(Tags.ProductDetails.IS_COS));

                info.setIsChoiceCombo(resultjson.optBoolean(Tags.ProductDetails.IS_CHANGE_STYLE));

                info.setProductMarketPrice(resultjson.optString(Tags.ProductDetails.MARKET_PRICE));

                JSONObject nextItemJson = resultjson.optJSONObject(Tags.ProductDetails.NEXT_ITEM);
                if (nextItemJson != null) {
                    Iterator<String> it = nextItemJson.keys();
                    while (it.hasNext()) {
                        if (TextUtils.equals(it.next(), Tags.ProductDetails.PRODUCT_ID)) {
                            info.setNextItem(nextItemJson.optString(Tags.ProductDetails.PRODUCT_ID));
                        } else {
                            info.setNextIsPhone(nextItemJson.optBoolean(Tags.ProductDetails.IS_PHONE));
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
                            info.setLastIsPhone(lastItemJson.optBoolean(Tags.ProductDetails.IS_PHONE));
                        }
                    }
                }

                JSONArray styleJsonArray = resultjson.optJSONArray(Tags.ProductDetails.PRODUCT_NEW_STYLE);
                ArrayList<StyleItem> styleList = new ArrayList<StyleItem>();
                if (styleJsonArray != null) {
                    for (int i = 0; i < styleJsonArray.length(); i++) {
                        if (!styleJsonArray.isNull(i)) {
                            JSONObject itemJsonObject = (JSONObject) styleJsonArray.get(i);
                            StyleItem styleItem = new StyleItem();
                            String sytleType = itemJsonObject
                                    .optString(Tags.ProductDetails.PRODUCT_STYLE_TYPE);
                            styleItem.setStyleType(sytleType);
                            JSONArray styleDataArray = itemJsonObject
                                    .optJSONArray(Tags.ProductDetails.PRODUCT_STYLE_DATA);
                            LinkedHashMap<String, String> styleMap = new LinkedHashMap<String, String>();
                            for (int j = 0; j < styleDataArray.length(); j++) {
                                if (!styleDataArray.isNull(j)) {
                                    JSONObject dataJsonObject = (JSONObject) styleDataArray.get(j);
                                    String productId = dataJsonObject
                                            .optString(Tags.ProductDetails.PRODUCT_STYLE_ID);
                                    String styleName = dataJsonObject
                                            .optString(Tags.ProductDetails.PRODUCT_STYLE_NAME);
                                    styleMap.put(styleName, productId);
                                }
                            }
                            styleItem.setStyleDataMap(styleMap);
                            styleList.add(styleItem);
                        }
                    }
                    info.setStyleList(styleList);
                }

                // adapt phone
                JSONObject adaptjson = resultjson.optJSONObject(Tags.ProductDetails.ADAPT);
                if (adaptjson != null) {
                    Iterator<String> it = adaptjson.keys();
                    LinkedHashMap<String, String> adaptMap = new LinkedHashMap<String, String>();
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    while (it.hasNext()) {
                        int adaptKey = Integer.parseInt(it.next());
                        list.add(adaptKey);
                    }
                    Collections.sort(list, Collections.reverseOrder());
                    for (int adaptkey : list) {
                        String adaptValue = adaptjson.getString(String.valueOf(adaptkey));
                        adaptMap.put(String.valueOf(adaptkey), adaptValue);
                    }
                    info.setAdaptPhone(adaptMap);
                }

                // listview item
                ProductDetailsInfoItem item = null;
                ArrayList<ProductDetailsInfoItem> list = new ArrayList<ProductDetailsInfoItem>();

                JSONArray productImageJsonArray = resultjson
                        .getJSONArray(Tags.ProductDetails.PRODUCT_DESC_IMG);
                if (productImageJsonArray != null) {
                    for (int i = 0; i < productImageJsonArray.length(); i++) {
                        String imageUrl = productImageJsonArray.getString(i);
                        item = new ProductDetailsInfoItem();
                        item.setImage(new Image(imageUrl));
                        list.add(item);
                    }
                }
                info.setItems(list);

                JSONObject productImageJson = resultjson
                        .optJSONObject(Tags.ProductDetails.IMAGES);
                if (productImageJson != null) {
                    for (int i = 1; i <= productImageJson.length(); i++) {
                        String image = productImageJson.optString(String.valueOf(i));
                        if (!TextUtils.isEmpty(image)) {
                            item = new ProductDetailsInfoItem();
                            item.setImage(new Image(image));
                            list.add(item);
                        }
                    }
                    info.setSupplyImage(new Image(productImageJson
                            .optString(Tags.ProductDetails.IMAGES_ONE)));
                }

                String image = resultjson.optString(Tags.ProductDetails.IMAGE_URL);
                if (!TextUtils.isEmpty(image)) {
                    info.setStylePhoto(new Image(image));
                }

                JSONObject canJoinActsJson = resultjson
                        .optJSONObject(Tags.ProductDetails.CANJOINACTS);
                if (canJoinActsJson != null) {
                    Iterator<String> it = canJoinActsJson.keys();
                    ArrayList<CanJoinActsItem> canJoinActsList = new ArrayList<CanJoinActsItem>();
                    while (it.hasNext()) {
                        String styleKey = it.next();
                        CanJoinActsItem actsItem = new CanJoinActsItem();
                        JSONObject canJoinActsKeyJson = canJoinActsJson.optJSONObject(styleKey);
                        if (canJoinActsKeyJson != null) {
                            String typeDesc = canJoinActsKeyJson.getString(Tags.ProductDetails.TYPE_DESC);
                            String discription = canJoinActsKeyJson
                                    .getString(Tags.ProductDetails.TITLE);
                            actsItem.setActsType(typeDesc);
                            actsItem.setActsDescription(discription);
                        }
                        canJoinActsList.add(actsItem);
                    }
                    info.setCanJoinActsList(canJoinActsList);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        return info;
    }

    public static class CanJoinActsItem {
        private String mType;
        private String mDescription;

        public String getActsType() {
            return mType;
        }

        public void setActsType(String type) {
            mType = type;
        }

        public String getActsDescription() {
            return mDescription;
        }

        public void setActsDescription(String description) {
            mDescription = description;
        }
    }

    public static class StyleItem implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String mStyleType;
        private LinkedHashMap<String, String> mStyleDataMap;

        public String getStyleType() {
            return mStyleType;
        }

        public void setStyleType(String type) {
            mStyleType = type;
        }

        public LinkedHashMap<String, String> getStyleDataMap() {
            return mStyleDataMap;
        }

        public void setStyleDataMap(LinkedHashMap<String, String> styleDataMap) {
            mStyleDataMap = styleDataMap;
        }
    }

}
