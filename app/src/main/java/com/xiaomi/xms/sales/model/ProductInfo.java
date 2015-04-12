
package com.xiaomi.xms.sales.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductInfo implements Parcelable,Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mProductId;
    private String mProductName;
    private String mProductPrice;
    private String mMarketPrice;
    private String mStyleName;
    private String mUrl;
    private Image mProductPhoto;
    private boolean mHasProduct;
    private boolean mDataError;
    private String mDisplayType;
    private String mPid;
    private boolean mIsBatched;
    private String mContainId;

    public ProductInfo(String productId, String productName, String productPrice,
            String marketPrice, String styleName,
            boolean hasProduct, Image photo) {
        mProductId = productId;
        mProductPrice = productPrice;
        mMarketPrice = marketPrice;
        mProductPhoto = photo;
        mProductName = productName;
        mHasProduct = hasProduct;
        mStyleName = styleName;
    }

    public ProductInfo(String productId, String productName, String productPrice,
            String marketPrice, String styleName,
            boolean hasProduct, Image photo, String url, String displayType) {
        mProductId = productId;
        mProductPrice = productPrice;
        mMarketPrice = marketPrice;
        mProductPhoto = photo;
        mProductName = productName;
        mHasProduct = hasProduct;
        mStyleName = styleName;
        mUrl = url;
        mDisplayType = displayType;
    }

    // 读取顺序必须与 writeToParcel 写入顺序一致
    public ProductInfo(Parcel source) {
        mProductId = source.readString();
        mProductName = source.readString();
        mProductPrice = source.readString();
        mMarketPrice = source.readString();
        mStyleName = source.readString();
        mUrl = source.readString();
        mProductPhoto = new Image(source.readString());
        mHasProduct = source.readByte() == 1;
        mDataError = source.readByte() == 1;
    }

    public String getProductPrice() {
        return mProductPrice;
    }

    public Image getImage() {
        return mProductPhoto;
    }

    public String getProductName() {
        return mProductName;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getStyleName() {
        return mStyleName;
    }

    public boolean hasProduct() {
        return mHasProduct;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getMarketPrice() {
        return mMarketPrice;
    }

    public String getDisplayType() {
        return mDisplayType;
    }

    public String getPid() {
        return mPid;
    }

    public void setPid(String mPid) {
        this.mPid = mPid;
    }

    public boolean isIsBatched() {
        return mIsBatched;
    }

    public void setIsBatched(boolean mIsBatched) {
        this.mIsBatched = mIsBatched;
    }

    public String getContainId() {
        return mContainId;
    }

    public void setContainId(String mContainId) {
        this.mContainId = mContainId;
    }

    public static String getCateName(JSONObject json) throws JSONException {
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                return body.optJSONObject("catinfo").optString(Tags.Product.CATE_NAME);
            }
        }
        return null;
    }
    
    public static int getPageSize(JSONObject json) throws JSONException {
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                return body.optJSONObject("catinfo").optInt("pagesize");
            }
        }
        return 0;
    }

    public static String getSearchResultCount(JSONObject json) throws JSONException {
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                return body.optJSONObject("catinfo").optString(Tags.Product.TOTAL_COUNT);
            }
        }
        return null;
    }

    public static ArrayList<ProductInfo> valueOf(JSONObject json) throws JSONException {
        ArrayList<ProductInfo> list = null;
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                JSONArray productJsonArray = body.optJSONArray(Tags.Product.PRODUCT);
                if (productJsonArray != null) {
                    list = new ArrayList<ProductInfo>();
                    for (int i = 0; i < productJsonArray.length(); i++) {
                        if (!productJsonArray.isNull(i)) {
                            JSONObject proJsonobject = (JSONObject) productJsonArray.get(i);
                            String productId = proJsonobject.optString(Tags.Product.PRODUCT_ID);
                            String productName = proJsonobject.optString(Tags.Product.PRODUCT_NAME);
                            String price = proJsonobject.optString(Tags.Product.PRICE);
                            String marketPrice = proJsonobject.optString(Tags.Product.MARKET_PRICE);
                            String styleName = proJsonobject.optString(Tags.Product.STYLE_NAME);
                            boolean hasProduct = proJsonobject.optBoolean(Tags.Product.IS_COS);
                            String imageUrl = proJsonobject.optString(Tags.Product.IMAGE_URL);
                            String url = proJsonobject.optString(Tags.Product.URL, "");
                            String displayType = proJsonobject.optString(Tags.Product.DISPLAY_TYPE,
                                    Tags.Product.DISPLAY_NATIVE);
                            String pid = proJsonobject.optString(Tags.Product.P_ID, productId);
                            boolean isBatched = proJsonobject.optBoolean(Tags.Product.IS_BATCHED,false);
                            String containId = proJsonobject.optString(Tags.Product.CONTAINID);
                            ProductInfo productInfo = new ProductInfo(productId, productName, price, marketPrice,
                                    styleName, !hasProduct, new Image(imageUrl), url, displayType);
                            productInfo.setPid(pid);
                            productInfo.setIsBatched(isBatched);
                            productInfo.setContainId(containId);
                            list.add(productInfo);
                        }
                    }
                }
            }
        }
        return list;
    }

    public static ProductInfo valueof(JSONObject proJsonobject){
        String productId = proJsonobject.optString("product_id");
        String productName =  proJsonobject.optString("product_name");
        String price =  proJsonobject.optString("price");
        String marketPrice =  proJsonobject.optString("market_price");
        String styleName =  proJsonobject.optString("style_name");
        boolean hasProduct =  true;
        String imageUrl =  proJsonobject.optString("image_url");
        String url =  "";
        String displayType =  "";
        String pid =  proJsonobject.optString("p_id");
        boolean isBatched = proJsonobject.optBoolean("is_batched");
        String containId = proJsonobject.optString("sku");
        ProductInfo productInfo = new ProductInfo(productId, productName, price, marketPrice,
                styleName, !hasProduct, new Image(imageUrl), url, displayType);
        if(productInfo != null){
        	 productInfo.setPid(pid);
             productInfo.setIsBatched(isBatched);
             productInfo.setContainId(containId);
        }
        return productInfo;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    // 实现Parcelable的方法writeToParcel，将ProductInfo序列化为一个Parcel对象
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProductId);
        dest.writeString(mProductName);
        dest.writeString(mProductPrice);
        dest.writeString(mMarketPrice);
        dest.writeString(mStyleName);
        dest.writeString(mUrl);
        dest.writeString(mProductPhoto.getFileUrl());
        dest.writeByte((byte) (mHasProduct ? 1 : 0));
        dest.writeByte((byte) (mDataError ? 1 : 0));
    }

    // 实例化静态内部对象CREATOR实现接口Parcelable.Creator
    public static final Parcelable.Creator<ProductInfo> CREATOR = new Creator<ProductInfo>() {
        @Override
        public ProductInfo createFromParcel(Parcel source) {
            return new ProductInfo(source);
        }

        // 将Parcel对象反序列化为ProductInfo
        @Override
        public ProductInfo[] newArray(int size) {
            return new ProductInfo[size];
        }
    };
}
