
package com.xiaomi.xms.sales.model;

import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderSubmitInfo extends BaseJsonModel {
    private String mCount;
    private String mShipment;
    private String mAmount;
    private String mProductMoney;
    private String mActivityDiscountMoney;
    private String mCouponDiscountMoney;
    private String mAmountDesc;
    private boolean mHasCheckCode;

    public String getAmountDesc() {
        return mAmountDesc;
    }

    public void setAmountDesc(String amountDesc) {
        this.mAmountDesc = amountDesc;
    }

    public String getmProductMoney() {
        return mProductMoney;
    }

    public void setProductMoney(String productMoney) {
        this.mProductMoney = productMoney;
    }

    public String getmActivityDiscountMoney() {
        return mActivityDiscountMoney;
    }

    public void setActivityDiscountMoney(String activityDiscountMoney) {
        this.mActivityDiscountMoney = activityDiscountMoney;
    }

    public String getCouponDiscountMoney() {
        return mCouponDiscountMoney;
    }

    public void setmCouponDiscountMoney(String couponDiscountMoney) {
        this.mCouponDiscountMoney = couponDiscountMoney;
    }

    private ShoppingCartListInfo mCart;

    public ShoppingCartListInfo getCart() {
        return mCart;
    }

    public void setCart(ShoppingCartListInfo cart) {
        this.mCart = cart;
    }

    public String getCount() {
        return mCount;
    }

    public void setCount(String count) {
        this.mCount = count;
    }

    public String getShipment() {
        return mShipment;
    }

    public void setShipment(String shipment) {
        this.mShipment = shipment;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        this.mAmount = amount;
    }

    public void setCheckCode(boolean hasCheckCode) {
        mHasCheckCode = hasCheckCode;
    }

    public boolean hasCheckCode() {
        return mHasCheckCode;
    }

    public static OrderSubmitInfo valueOf(JSONObject json) throws JSONException {

        OrderSubmitInfo info = new OrderSubmitInfo();
        ShoppingCartListInfo cartlist = null;

        if (json == null) {
            info.setNoJson(true);
            return info;
        }

        info.setCode(json.optInt(Tags.CODE));
        info.setDescription(json.optString(Tags.DESCRIPTION));
        info.setResult(json.optString(Tags.RESULT));

        if (!Tags.isJSONResultOK(json)) {
            return info;
        }

        cartlist = new ShoppingCartListInfo();
        info.setCart(cartlist);
        info.setAmount(json.getJSONObject(Tags.DATA).getString(Tags.OrderSubmit.AMOUNT));
        info.setCount(json.getJSONObject(Tags.DATA).getString(Tags.OrderSubmit.TOTAL));
        info.setShipment(json.getJSONObject(Tags.DATA).getString(Tags.OrderSubmit.SHIPMENT));
        info.setProductMoney(json.getJSONObject(Tags.DATA).getString(
                    Tags.OrderSubmit.PRODUCT_MONEY));
        info.setAmountDesc(json.getJSONObject(Tags.DATA).getString(Tags.OrderSubmit.AMOUNT_DESC));
        info.setActivityDiscountMoney(json.getJSONObject(Tags.DATA).getString(
                    Tags.OrderSubmit.ACTIVITY_DISCOUNT_MONEY));
        info.setmCouponDiscountMoney(json.getJSONObject(Tags.DATA).getString(
                    Tags.OrderSubmit.COUPON_DISCOUNT_MONEY));
        info.setCheckCode(json.getJSONObject(Tags.DATA).optBoolean(
                Tags.OrderSubmit.NEEDCHECKCODE, false));
        JSONArray array = json.getJSONObject(Tags.DATA)
                    .getJSONObject(Tags.OrderSubmit.CARTLIST)
                    .getJSONArray(Tags.ShoppingCartList.ITEMS);

        if (array == null || array.length() == 0) {
            return info;
        }

        ArrayList<Item> items = new ArrayList<Item>();

        for (int i = 0; i < array.length(); i++) {
            if (array.isNull(i)) {
                break;
            }

            JSONObject jsonObject = array.getJSONObject(i);
            Item item = new Item();
            item.setType(Item.TYPE_CARTLIST);
            CartListNode node = ShoppingCartListInfo
                        .parseShoppingCartListItem(jsonObject);
            item.setNode(node);
            items.add(item);
        }

        cartlist.setItems(items);

        return info;
    }
}
