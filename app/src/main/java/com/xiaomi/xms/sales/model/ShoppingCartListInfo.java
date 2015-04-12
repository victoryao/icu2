
package com.xiaomi.xms.sales.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.TitleNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class ShoppingCartListInfo extends BaseJsonModel {
    public final static String PROMOTION_TYPE_SUPPLY = "1";
    public final static String PROMOTION_TYPE_GIFT = "2";
    private final static String TAG = "ShoppingCartListInfo";
    private boolean mIsEmpty;
    private int mCount;
    private String mTotal;
    private ArrayList<Item> mItems;

    public boolean hasEmpty() {
        return mIsEmpty;
    }

    public void setIsEmpty(boolean isEmpty) {
        mIsEmpty = isEmpty;
    }

    public ShoppingCartListInfo() {
        mItems = new ArrayList<Item>();
    }

    public ArrayList<Item> getItems() {
        return mItems;
    }

    public void setItems(ArrayList<Item> items) {
        this.mItems = items;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public String getTotal() {
        return mTotal;
    }

    public void setTotal(String total) {
        this.mTotal = total;
    }

    public static class Item{
        /**
		 * 
		 */
		public final static int TYPE_COUNT = 7;
        public final static int TYPE_TITLE = 1;
        public final static int TYPE_SUPPLY = 2;
        public final static int TYPE_CARTLIST = 3;
        public final static int TYPE_BLACK = 4;
        public final static int TYPE_INCAST = 5;
        public final static int TYPE_ACT = 6;
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

        public static class TitleNode extends Node {
            private String mTitle;

            public String getTitle() {
                return mTitle;
            }

            public void setTitle(String title) {
                this.mTitle = title;
            }
        }

        public static class SupplyNode extends Node {
            private String mActId;
            private String mBargainName;
            private String mProductId;
            private boolean mCheckStatus;
            private String mItemId;
            private ArrayList<SelectableProduct> mSelectableProducts;

            public ArrayList<SelectableProduct> getSelectableProducts() {
                return mSelectableProducts;
            }

            public void setSelectableProducts(ArrayList<SelectableProduct> selectableProducts) {
                this.mSelectableProducts = selectableProducts;
            }

            public String getActId() {
                return mActId;
            }

            public void setActId(String actId) {
                this.mActId = actId;
            }

            public String getBargainName() {
                return mBargainName;
            }

            public void setBargainName(String bargainName) {
                this.mBargainName = bargainName;
            }

            public String getProductId() {
                return mProductId;
            }

            public void setProductId(String productId) {
                mProductId = productId;
            }

            public boolean getCheckedStatus() {
                return mCheckStatus;
            }

            public void setCheckedStatus(boolean checkStatus) {
                mCheckStatus = checkStatus;
            }

            public String getItemId() {
                return mItemId;
            }

            public void setItemId(String itemId) {
                this.mItemId = itemId;
            }
        }

        public static class CartListNode extends Node implements Serializable{

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			// 商品标题
            private String mTitle;
            // 商品上架价格
            private String mPrice;
            // 商品数量
            private int mCount;
            // 商品大图
            private Image mPhoto;
            // 商品缩略图
            private Image mThumbnail;
            // 商品总价
            private String mTotal;
            // 适配机型
            private HashMap<String, String> mAdaptPhone;

            // 可购买数量
            private int mBuyLimit;

            // 购物车内商品的itemid
            private String mItemId;

            // 套餐商品对应的子商品的id列表
            private String mItemIds;

            //  如果为true可以修改商品数量，否则不能
            private boolean mCanChangeNum;

            private boolean mCanDelete;

            // 显示类型 如赠品、加价购、秒杀、特价、摇一摇
            private String mShowType;

            // 如果是赠品活动，且活动物品是可选的，所有的可选物品
            private ArrayList<SelectableProduct> mSelectableProducts;

            public ArrayList<SelectableProduct> getSelectableProducts() {
                return mSelectableProducts;
            }

            public void setSelectableProducts(ArrayList<SelectableProduct> selectableProducts) {
                this.mSelectableProducts = selectableProducts;
            }

            public void setShowType(String showType) {
                mShowType = showType;
            }

            public String getShowType() {
                return mShowType;
            }

            public boolean getCanDelete() {
                return mCanDelete;
            }

            public void setCanDelete(boolean canDelete) {
                mCanDelete = canDelete;
            }

            public boolean getCanChangeNum() {
                return mCanChangeNum;
            }

            public void setCanChangeNum(boolean canChangeNum) {
                this.mCanChangeNum = canChangeNum;
            }

            public String getItemId() {
                return mItemId;
            }

            public void setItemId(String mItemId) {
                this.mItemId = mItemId;
            }

            public String getTotal() {
                return mTotal;
            }

            public void setTotal(String mTotal) {
                this.mTotal = mTotal;
            }

            public String getTitle() {
                return mTitle;
            }

            public void setTitle(String mTitle) {
                this.mTitle = mTitle;
            }

            public String getPrice() {
                return mPrice;
            }

            public void setPrice(String mPrice) {
                this.mPrice = mPrice;
            }

            public int getCount() {
                return mCount;
            }

            public void setCount(int mCount) {
                this.mCount = mCount;
            }

            public Image getPhoto() {
                return mPhoto;
            }

            public Image getThumbnail() {
                return mThumbnail;
            }

            public void setThumbnail(Image thumbnail) {
                mThumbnail = thumbnail;
            }

            public void setPhoto(Image mPhoto) {
                this.mPhoto = mPhoto;
            }

            public void setBuyLimit(int buyLimit) {
                this.mBuyLimit = buyLimit;
            }

            public int getBuyLimit() {
                return mBuyLimit;
            }

            public void setAdaptPhone(HashMap<String, String> adaptPhone) {
                mAdaptPhone = adaptPhone;
            }

            public HashMap<String, String> getAdaptPhone() {
                return mAdaptPhone;
            }

            public String getItemIds() {
                return mItemIds;
            }

            public void setItemIds(String mItemIds) {
                this.mItemIds = mItemIds;
            }
        }

        /**
         * 凑单信息。
         */
        public static class IncastNode extends Node {
            private String mBalance;// 距离免运费的差额
            private ArrayList<IncastProduct> mIncastProducts;// 凑单商品

            public ArrayList<IncastProduct> getPostFreeProducts() {
                return mIncastProducts;
            }

            public void setPostFreeProducts(ArrayList<IncastProduct> postFreeProducts) {
                this.mIncastProducts = postFreeProducts;
            }

            public String getBalance() {
                return mBalance;
            }

            public void setmBalance(String balance) {
                this.mBalance = balance;
            }

            public static class IncastProduct {
                private String mProductId;
                private String mProductName;
                private String mProductPrice;
                private Image mThumbnail;
                private Image mPhoto;

                public String getProductId() {
                    return mProductId;
                }

                public void setProductId(String productId) {
                    this.mProductId = productId;
                }

                public String getProductName() {
                    return mProductName;
                }

                public void setProductName(String productName) {
                    this.mProductName = productName;
                }

                public String getProductPrice() {
                    return mProductPrice;
                }

                public void setProductPrice(String productPrice) {
                    this.mProductPrice = productPrice;
                }

                public Image getThumbnail() {
                    return mThumbnail;
                }

                public void setThumbnail(Image thumbnail) {
                    this.mThumbnail = thumbnail;
                }

                public Image getPhoto() {
                    return mPhoto;
                }

                public void setPhoto(Image photo) {
                    this.mPhoto = photo;
                }

                public static String serialize(ArrayList<IncastProduct> list) {
                    JSONArray array = new JSONArray();
                    if (list != null) {
                        for (IncastProduct product : list) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put(Tags.ProductDetails.PRODUCT_ID, product.getProductId());
                                obj.put(Tags.ProductDetails.PRODUCT_NAME, product.getProductName());
                                obj.put(Tags.ProductDetails.PRICE, product.getProductPrice());
                                obj.put(Tags.ShoppingCartList.IMAGE_PHOTO, product.getPhoto()
                                        .getFileUrl());
                                obj.put(Tags.ShoppingCartList.IMAGE_THUMBNAIL, product
                                        .getThumbnail().getFileUrl());
                                array.put(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return array.toString();
                }

                public static ArrayList<IncastProduct> deserialize(String str) {
                    ArrayList<IncastProduct> list = new ArrayList<IncastProduct>();
                    if (!TextUtils.isEmpty(str)) {
                        try {
                            JSONArray array = new JSONArray(str);
                            int len = array.length();
                            for (int i = 0; i < len; i++) {
                                JSONObject obj = array.optJSONObject(i);
                                IncastProduct product = new IncastProduct();
                                product.setProductId(obj.getString(Tags.ProductDetails.PRODUCT_ID));
                                product.setProductName(obj
                                        .getString(Tags.ProductDetails.PRODUCT_NAME));
                                product.setProductPrice(obj.getString(Tags.ProductDetails.PRICE));
                                product.setPhoto(new Image(obj
                                        .getString(Tags.ShoppingCartList.IMAGE_PHOTO)));
                                product.setThumbnail(new Image(obj
                                        .getString(Tags.ShoppingCartList.IMAGE_THUMBNAIL)));
                                list.add(product);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return list;
                }

            }
        }

        /**
         * 活动描述。
         */
        public static class ActNode extends Node {
            public String type;
            public String info;
        }
    }

    /**
     * 加价购、赠品等活动商品如果为可选，记录一个可选商品的信息。
     */
    public static class SelectableProduct implements Parcelable {
        // 活动类型
        public String promotionType;
        // 活动id
        public String actId;
        // 商品id
        public String productId;
        // 商品名称
        public String name;

        public SelectableProduct(String promotionType, String actId, String productId, String name) {
            this.promotionType = promotionType;
            this.actId = actId;
            this.productId = productId;
            this.name = name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flag) {
            dest.writeString(promotionType);
            dest.writeString(actId);
            dest.writeString(productId);
            dest.writeString(name);
        }

        public static final Parcelable.Creator<SelectableProduct> CREATOR = new Creator<SelectableProduct>() {

            @Override
            public SelectableProduct[] newArray(int size) {
                return new SelectableProduct[size];
            }

            @Override
            public SelectableProduct createFromParcel(Parcel p) {
                SelectableProduct order = new SelectableProduct(p.readString(), p.readString(),
                        p.readString(), p.readString());
                return order;
            }
        };
    }

    public static ShoppingCartListInfo valueOf(JSONObject json) throws JSONException {

        ShoppingCartListInfo info = new ShoppingCartListInfo();

        if (json == null) {
            info.setNoJson(true);
            return info;
        }

        info.setCode(json.optJSONObject(Tags.HEADER).optInt(Tags.CODE));
        info.setDescription(json.getJSONObject(Tags.HEADER).optString(Tags.DESC));

        if (!Tags.isJSONReturnedOK(json)) {
            info.setResult(Tags.RESULT_ERROR);
            return info;
        }

        Item item = null;

        String bodyStr = json.optString(Tags.BODY);
        if (!TextUtils.isEmpty(bodyStr)) {
            info.setResult(Tags.RESULT_OK);
            JSONObject body = new JSONObject(bodyStr);
            if (body != null) {
                JSONArray cartListArray = body.optJSONArray(Tags.DATA);
                if (cartListArray == null || cartListArray.length() == 0) {
                    info.setIsEmpty(true);
                    return info;
                }

                ArrayList<Item> items = new ArrayList<Item>();
                info.setIsEmpty(false);
                info.setTotal(body.optString(Tags.ShoppingCartList.TOTAL_PRICE));
                info.setCount(body.optInt(Tags.ShoppingCartList.COUNT));

                item = new Item();
                TitleNode tNode = new TitleNode();
                tNode.setTitle(ShopApp.getContext().getString(R.string.shopping_cartlist_title));
                item.setNode(tNode);
                item.setType(Item.TYPE_TITLE);
                items.add(item);

                int n = 0;
                for (int i = 0; i < cartListArray.length(); i++) {
                    if (cartListArray.isNull(i)) {
                        break;
                    }
                    JSONObject jsonObject = cartListArray.optJSONObject(i);
                    CartListNode node = parseShoppingCartListItem(jsonObject);
                    item = new Item();
                    item.setType(Item.TYPE_CARTLIST);
                    item.setNode(node);
                    items.add(item);
                    n++;
                }

                if (n == 0) {
                    info.setIsEmpty(true);
                    return info;
                }

                item = new Item();
                item.setType(Item.TYPE_BLACK);
                items.add(item);
                info.setItems(items);
            }
        }

        return info;
    }

    public static CartListNode parseShoppingCartListItem(JSONObject json)
            throws JSONException {
        return parseShoppingCartListItem(json, null, null);
    }

    public static CartListNode parseShoppingCartListItem(JSONObject json,
            HashMap<String, ArrayList<SelectableProduct>> selectableProductMap,
            StringBuffer productIdSb)
            throws JSONException {
        CartListNode node = new CartListNode();
        node.setCanChangeNum(true);
        node.setTitle(json.optString("goodsName"));
        node.setCount(json.optInt("goodsCount"));
        node.setPrice(json.optString("realShopPrice"));
        double total = mul(json.optInt("goodsCount"), json.optDouble("realShopPrice"));
        node.setTotal(String.valueOf(total));
        node.setItemId(json.optString("goodsId"));
        if (productIdSb != null) {
            productIdSb.append(json.optString(Tags.ShoppingSupply.PRODUCT_ID));
        }
        String imageUrl = json.optString("imageUrl");
        node.setPhoto(new Image(imageUrl + "?width=800&height=800"));
        node.setThumbnail(new Image(imageUrl + "?width=180&height=180"));
        node.setBuyLimit(json.optInt("buyLimit"));
        node.setCanDelete(true);
        node.setItemIds(json.optString("itemIds"));
        node.setShowType(json.optString(Tags.ShoppingCartList.SHOWTYPE));
        if (selectableProductMap != null) {
            if (node.getShowType().equals(Tags.ShoppingCartList.SHOWTYPE_GIFT)) {
                JSONObject properties = json.optJSONObject(Tags.ShoppingCartList.PROPERTIES);
                if (properties != null) {
                    String actId = properties.optString(Tags.ShoppingSupply.ACT_ID);
                    if (actId != null) {
                        node.setSelectableProducts(selectableProductMap.get(actId));
                    }
                }
            }
        }
        return node;
    }

    public static Double mul(int v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.multiply(b2).doubleValue();
    }
}
