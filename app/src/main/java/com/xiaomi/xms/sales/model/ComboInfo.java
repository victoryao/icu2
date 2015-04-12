
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ComboInfo {

    public static final String TAG = "ComboInfo";
    private ArrayList<HashMap<String, ProductInfo>> mComboProductList;
    private ArrayList<ProductInfo> mSelectedProducts;

    public ComboInfo() {
    }

    public void setComboProductList(ArrayList<HashMap<String, ProductInfo>> list) {
        mComboProductList = list;
    }

    public ArrayList<HashMap<String, ProductInfo>> getComboProductList() {
        return mComboProductList;
    }

    public void setSelectedProducts(ArrayList<ProductInfo> listProduct) {
        mSelectedProducts = listProduct;
    }

    public ArrayList<ProductInfo> getSelectedProducts() {
        return mSelectedProducts;
    }

    public static ComboInfo valueOf(JSONObject json) throws JSONException {
        ComboInfo info = null;
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject body = new JSONObject(bodyStr);
                if (body != null) {
                    JSONArray comboJsonArray = body.optJSONArray("data");
                    info = new ComboInfo();
                    if (comboJsonArray != null) {
                        ArrayList<ProductInfo> listproduct = new ArrayList<ProductInfo>();
                        ArrayList<HashMap<String, ProductInfo>> list = new ArrayList<HashMap<String, ProductInfo>>();
                        for (int i = 0; i < comboJsonArray.length(); i++) {
                            if (!comboJsonArray.isNull(i)) {
                                JSONObject comboJsonObject = comboJsonArray.optJSONObject(i);
                                Iterator comboKeys = comboJsonObject.keys();
                                if (comboKeys.hasNext()) {
                                    String key = comboKeys.next().toString();
                                    JSONObject jsonobj = comboJsonObject.optJSONObject(key);
                                    String productId = jsonobj.optString(Tags.ComboList.PRODUCT_ID);
                                    String productName = jsonobj.optString(Tags.ComboList.PRODUCT_NAME);
                                    String styleName = jsonobj.optString(Tags.ComboList.PRODUCT_STYLE);
                                    boolean hasProduct = jsonobj.optBoolean(Tags.ComboList.IS_SALE);
                                    String imageUrl = jsonobj.optString(Tags.ComboList.IMAGE_URL)
                                            + "?width=180&height=180";
                                    HashMap<String, ProductInfo> map = new HashMap<String, ProductInfo>();
                                    ProductInfo productInfo = null;
                                    if (comboJsonObject.length() > 1) {
                                        Iterator styleKeys = comboJsonObject.keys();
                                        while (styleKeys.hasNext()) {
                                            String comboKey = styleKeys.next().toString();
                                            JSONObject jsonspinner = comboJsonObject.optJSONObject(comboKey);
                                            String styleItemProductId = jsonspinner
                                                    .optString(Tags.ComboList.PRODUCT_ID);
                                            String styleItemproductName = jsonspinner
                                                    .optString(Tags.ComboList.PRODUCT_NAME);
                                            String styleItemstyleName = jsonspinner
                                                    .optString(Tags.ComboList.PRODUCT_STYLE);
                                            boolean styleItemhasProduct = jsonspinner
                                                    .getBoolean(Tags.ComboList.IS_SALE);
                                            String styleItemimageUrl = jsonspinner.optString(Tags.ComboList.IMAGE_URL)
                                                    + "?width=180&height=180";
                                            if (!styleItemhasProduct) {
                                                productInfo = new ProductInfo(styleItemProductId,
                                                        styleItemproductName, null, null, styleItemstyleName,
                                                        !styleItemhasProduct, new Image(styleItemimageUrl));
                                                if (!TextUtils.isEmpty(styleItemstyleName)) {
                                                    map.put(styleItemstyleName, productInfo);
                                                }
                                            }
                                        }
                                        if (map.isEmpty() && productInfo != null) {
                                            map.put("", productInfo);
                                        }
                                    }
                                    ProductInfo product = new ProductInfo(productId, productName, null, null,
                                            styleName, hasProduct, new Image(imageUrl));
                                    list.add(map);
                                    listproduct.add(product);
                                }
                            }
                        }
                        info.setComboProductList(list);
                        info.setSelectedProducts(listproduct);
                    }
                }
            }
        }
        return info;
    }
}
