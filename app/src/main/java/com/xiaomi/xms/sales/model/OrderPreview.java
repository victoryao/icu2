
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import com.xiaomi.xms.sales.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderPreview {
    private String mOrderId; // 订单Id
    private double mFee; // 金额
    private String mOrderStatusString; // 支付状态字符串
    private String mAddTime; // 下单时间
    private Image mImage; // 图片地址
    private int mDeliverCount;
    private boolean isMessageCheck;

    public OrderPreview(String orderId, String addTime, double fee, String statusString, String image, int deliverCount) {
        mOrderId = orderId;
        mAddTime = addTime;
        mFee = fee;
        mOrderStatusString = statusString;
        mImage = new Image(image);
        mDeliverCount = deliverCount;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public String getFee() {
        return Utils.Money.valueOf(mFee);
    }

    public String getOrderStatusString() {
        return mOrderStatusString;
    }

    public String getAddTime() {
        return mAddTime;
    }

    public Image getImage() {
        return mImage;
    }

    public int getDeliverCount() {
        return mDeliverCount;
    }

    public boolean isMessageCheck() {
        return isMessageCheck;
    }

    public void setMessageCheck(boolean isMessageCheck) {
        this.isMessageCheck = isMessageCheck;
    }

    public static ArrayList<OrderPreview> valueOfOrderList(JSONObject json) throws JSONException {
        ArrayList<OrderPreview> list = null;
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject dataJson = new JSONObject(bodyStr);
                if (dataJson != null) {
                    JSONArray listJson = dataJson.optJSONArray(Tags.DATA);
                    if (listJson != null) {
                        list = new ArrayList<OrderPreview>();
                        for (int i = 0; i < listJson.length(); i++) {
                            JSONObject one = listJson.optJSONObject(i);
                            if (one != null) {
                                JSONArray productArrayList = null;
                                JSONObject firstProduct = null;
                                JSONArray subSalesOrderList = null;
                                productArrayList = one.optJSONArray("salesOrderItemList");
                                if (productArrayList == null) {
                                    continue;
                                }
                                firstProduct = productArrayList.optJSONObject(0);
                                String image = TextUtils.isEmpty(firstProduct.optString("imageUrl")) ? ""
                                        : firstProduct.optString("imageUrl") + "?width=180&height=180";
                                String orderId = one.optString("serviceNumber");
                                String addTime = one.optString("addTime");
                                double fee = one.optDouble("realTotalPrice");
                                String statusString = one.optString("orderStatusName");
                                subSalesOrderList = one.optJSONArray("subSalesOrderList");
                                if(subSalesOrderList != null){
                                	if(subSalesOrderList.length() == 1){
    									if(subSalesOrderList.optJSONObject(0).optString("payId").equals("100") 
    											&& subSalesOrderList.optJSONObject(0).optString("orderStatus").equals("39")
    											&& subSalesOrderList.optJSONObject(0).optString("orderStatusDesc").equals("已退款")){
    										statusString = subSalesOrderList.optJSONObject(0).optString("orderStatusDesc");
    									}
    								}
                                }
                                OrderPreview orderPreview = new OrderPreview(orderId, addTime, fee, statusString,
                                        image, 0);
                                list.add(orderPreview);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}
