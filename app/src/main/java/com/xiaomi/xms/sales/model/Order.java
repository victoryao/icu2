
package com.xiaomi.xms.sales.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.xiaomi.xms.sales.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Order {
    private String mOrderId; // 订单Id
    private double mFee; // 金额
    private String mConsignee; // 收货人
    private String mConsigneePhone; // 收货人电话
    private String mConsigneeAddress; // 收货人地址
    private String mDeliveryTime; // 送货时间
    private String mAddTime; // 订单添加时间
    private String mInvoiceInfo; // 发票信息
    private int mOrderStatus; // 支付状态
    private String mOrderStatusInfo; // 支付状态信息
    private ArrayList<OrderTrack> mTracks; // 流程信息
    private String mProvince; // 收货地址-省
    private String mCity; // 收货地址-市
    private String mDistrict; // 收货地址-区
    private String mZipCode; // 收货地址-邮编
    private ArrayList<ProductBrief> mProducts; // 商品列表
    private ArrayList<ProductBrief> mProductSnList; // 商品Sn列表
    private ArrayList<String> mNextList;
    private OrderExpress mExpress;
    private int mProvinceId;
    private int mCityId;
    private int mDistrictId;
    private boolean mHasPhone;
    private ArrayList<String> mShowType;
    private ArrayList<DeliverOrder> mDeliverOrderList;
    private String mReducePrice;
    private String mOriginalPrice;
    private String mShipmentExpense;
    private PickupInfo mPickupInfo;
    private String mOrderPayNo;
    private String mOrderPayTime;
    private String mOrderUserName;
    private String mOrderUserTel;
    private String mOrderUserEmail;
    private int orderType;   //订单类型
    private String orgName;   //机构名
	private String orgTel;   //机构电话
	private String orgAddress;  //机构地址
	private int payId;  //支付类型
	private String mituShuo; //米兔说
	private String mMerchantName; //商户名称
	private String mPosName; //pos机名
	private String posRequstId;//流水号
	private String referenceNumber;//交易参考号
	private String mPayStatus; //支付状态
    /**
     * OrderTrack 用来记录流程中的一个步骤的信息
     */
    public static class OrderTrack {
        public String mText;
        public String mTime;
    }

    /**
     * ProductBrief 商品的简单介绍
     */
    public static class ProductBrief implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 3996395189010372008L;
		public String mProductId;
        public String mProductName;
        public Image mProductImage;
        public int mProductCount;
        public String mProductPrice;
        public String mTotalPrice;
        public String mImei;
        public String mSn;
        public String mNewImei;
        public String mNewSn;
        public String mIsMobile;

        public ProductBrief(String productId, String productName, String productPrice,
                int productCount, String imageURL, String totalPrice,String imei,String sn,String isMobile) {
            mProductId = productId;
            mProductName = productName;
            mProductPrice = productPrice;
            mProductCount = productCount;
            mProductImage = new Image(imageURL);
            mTotalPrice = totalPrice;
            mImei = imei;
            mSn = sn;
            mIsMobile = isMobile;
        }
        
        public void setSnOrImei(String mSn){
    		if(mIsMobile.equals("0")){
    			setmSn(mSn);
    		}else{
    			setmImei(mSn);
    		}
    	}
    	
    	public void setNewSnOrImei(String mNewdSn){
    		if(mIsMobile.equals("0")){
    			setmNewSn(mNewdSn);
    		}else{
    			setmNewImei(mNewdSn);
    		}
    	}

		public String getmNewImei() {
			return mNewImei;
		}

		public void setmNewImei(String mNewImei) {
			this.mNewImei = mNewImei;
		}

		public String getmNewSn() {
			return mNewSn;
		}

		public void setmNewSn(String mNewSn) {
			this.mNewSn = mNewSn;
		}

		public String getmImei() {
			return mImei;
		}

		public void setmImei(String mImei) {
			this.mImei = mImei;
		}

		public String getmSn() {
			return mSn;
		}

		public void setmSn(String mSn) {
			this.mSn = mSn;
		}
        
    }

    /**
     * DeliverOrder 发货单的内容
     */
    public static class DeliverOrder {
        public String mDeliverId;
        public String mOrderStatusInfo;
        public ArrayList<OrderTrack> mTrackList;
        public ArrayList<ProductBrief> mDeliveProducts;
        public String mShipmentExpense;
        public OrderExpress mDeliveExpress;

        public DeliverOrder(String deliverId, String orderStatusInfo,
                ArrayList<OrderTrack> trackList, ArrayList<ProductBrief> products,
                String shipmentExpense, OrderExpress express) {
            mDeliverId = deliverId;
            mOrderStatusInfo = orderStatusInfo;
            mTrackList = trackList;
            mDeliveProducts = products;
            mShipmentExpense = shipmentExpense;
            mDeliveExpress = express;
        }
    }

    /**
     * 物流信息
     */
    public static class OrderExpressTrace implements Serializable {
        public String mText;
        public String mTime;
        public String mType;
    }

    public static class OrderExpress implements Parcelable {
        public String mExpressId;
        public String mExpressName;
        public String mExpressSN;
        public String mUpdateTime;
        public boolean mIsShow = false;
        public ArrayList<OrderExpressTrace> mTraces;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flag) {
            dest.writeString(mExpressId);
            dest.writeString(mExpressName);
            dest.writeString(mExpressSN);
            dest.writeString(mUpdateTime);
            dest.writeBooleanArray(new boolean[] {
                    mIsShow
            });
            dest.writeList(mTraces);
        }

        public static final Parcelable.Creator<OrderExpress> CREATOR = new Creator<OrderExpress>() {

            @Override
            public OrderExpress[] newArray(int size) {
                return new OrderExpress[size];
            }

            @Override
            public OrderExpress createFromParcel(Parcel p) {
                OrderExpress order = new OrderExpress();
                order.mExpressId = p.readString();
                order.mExpressName = p.readString();
                order.mExpressSN = p.readString();
                order.mUpdateTime = p.readString();
                p.readBooleanArray(new boolean[] {
                        order.mIsShow
                });
                p.readList(order.mTraces, OrderExpressTrace.class.getClassLoader());
                return order;
            }
        };
    }

    public static class PickupInfo {
        public String mPickupAddress;
        public String mPickupName;
        public String mPickupTel;
        public String mPickupLonLat;

        public PickupInfo(String pickupAddress, String pickupName, String pickupTel, String pickupLonLat) {
            mPickupAddress = pickupAddress;
            mPickupName = pickupName;
            mPickupTel = pickupTel;
            mPickupLonLat = pickupLonLat;
        }
    }

    public Order(String orderId, double fee, String consignee, String phoneNumber, String address,
            String deliveryTime, String addTime, String invoiceInfo, int orderStatus,
            String statusString, String province, String city, String district, String zipcode,
            ArrayList<OrderTrack> tracks, ArrayList<ProductBrief> products, ArrayList<String> next,
            boolean hasPhone, ArrayList<String> showType, ArrayList<DeliverOrder> deliverOrderList) {
        mOrderId = orderId;
        mFee = fee;
        mConsignee = consignee;
        mConsigneePhone = phoneNumber;
        mConsigneeAddress = address;
        mDeliveryTime = deliveryTime;
        mInvoiceInfo = invoiceInfo;
        mOrderStatus = orderStatus;
        mOrderStatusInfo = statusString;
        mProvince = province;
        mCity = city;
        mDistrict = district;
        mZipCode = zipcode;
        mTracks = tracks;
        mProducts = products;
        mNextList = next;
        mAddTime = addTime;
        mHasPhone = hasPhone;
        mShowType = showType;
        mDeliverOrderList = deliverOrderList;
    }

    public Order() {
        super();
    }

    public String getOrderId() {
        return mOrderId;
    }

    public double getFee() {
        return mFee;
    }

    public String getConsignee() {
        return mConsignee;
    }

    public String getConsigneePhone() {
        return mConsigneePhone;
    }

    public String getConsigneeAddress() {
        return mConsigneeAddress;
    }

    public String getDeliveryTime() {
        return mDeliveryTime;
    }

    public String getAddTime() {
        return mAddTime;
    }

    public String getInvoiceInfo() {
        return mInvoiceInfo;
    }

    public int getOrderStatus() {
        return mOrderStatus;
    }

    public String getOrderStatusInfo() {
        return mOrderStatusInfo;
    }

    public String getProvince() {
        return mProvince;
    }

    public String getCity() {
        return mCity;
    }

    public String getDistrict() {
        return mDistrict;
    }

    public String getZipCode() {
        return mZipCode;
    }

    public ArrayList<ProductBrief> getProductList() {
        return mProducts;
    }

    public ArrayList<OrderTrack> getTracks() {
        return mTracks;
    }

    public ArrayList<String> getNexts() {
        return mNextList;
    }

    public void setOrderId(String mOrderId) {
        this.mOrderId = mOrderId;
    }

    public void setFee(double mFee) {
        this.mFee = mFee;
    }

    public void setConsignee(String mConsignee) {
        this.mConsignee = mConsignee;
    }

    public void setConsigneePhone(String mConsigneePhone) {
        this.mConsigneePhone = mConsigneePhone;
    }

    public void setConsigneeAddress(String mConsigneeAddress) {
        this.mConsigneeAddress = mConsigneeAddress;
    }

    public void setDeliveryTime(String mDeliveryTime) {
        this.mDeliveryTime = mDeliveryTime;
    }

    public void setAddTime(String mAddTime) {
        this.mAddTime = mAddTime;
    }

    public void setInvoiceInfo(String mInvoiceInfo) {
        this.mInvoiceInfo = mInvoiceInfo;
    }

    public void setOrderStatus(int mOrderStatus) {
        this.mOrderStatus = mOrderStatus;
    }

    public void setOrderStatusInfo(String mOrderStatusInfo) {
        this.mOrderStatusInfo = mOrderStatusInfo;
    }

    public void setTracks(ArrayList<OrderTrack> mTracks) {
        this.mTracks = mTracks;
    }

    public void setProvince(String mProvince) {
        this.mProvince = mProvince;
    }

    public void setCity(String mCity) {
        this.mCity = mCity;
    }

    public void setDistrict(String mDistrict) {
        this.mDistrict = mDistrict;
    }

    public void setZipCode(String mZipCode) {
        this.mZipCode = mZipCode;
    }

    public void setProducts(ArrayList<ProductBrief> mProducts) {
        this.mProducts = mProducts;
    }

    public void setNextList(ArrayList<String> mNextList) {
        this.mNextList = mNextList;
    }

    public void setExpress(OrderExpress mExpress) {
        this.mExpress = mExpress;
    }

    public void setHasPhone(boolean mHasPhone) {
        this.mHasPhone = mHasPhone;
    }

    public void setShowType(ArrayList<String> mShowType) {
        this.mShowType = mShowType;
    }

    public boolean hasPhone() {
        return mHasPhone;
    }

    public boolean isMihomeBuy() {
        return mShowType != null && mShowType.contains("MIHOME");
    }

    public static ArrayList<OrderTrack> getTracks(JSONArray tracks) {
        ArrayList<OrderTrack> list = new ArrayList<OrderTrack>();
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject one = tracks.optJSONObject(i);
            if (one != null) {
                OrderTrack t = new OrderTrack();
                t.mText = one.optString(Tags.Order.TRACK_TEXT);
                t.mTime = one.optString(Tags.Order.TRACK_TIME);
                list.add(t);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    public static ArrayList<ProductBrief> getProducts(JSONObject json) {
        JSONArray productlist = json.optJSONArray("salesOrderItemList");
        ArrayList<ProductBrief> list = new ArrayList<ProductBrief>();
        for (int i = 0; i < productlist.length(); i++) {
            JSONObject one = productlist.optJSONObject(i);
            if (one != null) {
                String productId = one.optString("goodsId");
                String productName = one.optString("goodsName");
                String productPrice = one.optString("realShopPrice");
                String totalPrice = one.optString("realShopPrice");
                int productCount = one.optInt("goodsCount");
                String productImage = TextUtils.isEmpty(one.optString("imageUrl")) ? "" : one.optString("imageUrl")
                        + "?width=180&height=180";
                list.add(new ProductBrief(productId, productName, productPrice, productCount,
                        productImage, totalPrice, "", "", ""));
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }
    
    public static ArrayList<ProductBrief> getProductSnList(JSONObject json) {
        JSONArray productlist = json.optJSONArray("goodsInfo");
        
        ArrayList<ProductBrief> list = new ArrayList<ProductBrief>();
        if(productlist == null){
        	return list;
        }
        for (int i = 0; i < productlist.length(); i++) {
            JSONObject one = productlist.optJSONObject(i);
            if (one != null) {
                String productId = one.optString("goodsId");
                String productName = one.optString("goodsName");
                String productPrice = one.optString("realShopPrice");
                String totalPrice = one.optString("realShopPrice");
                String imei = one.optString("imei");
                String sn = one.optString("sn");
                String isMobile = one.optString("isMobile");
                int productCount = one.optInt("goodsCount");
                String productImage = TextUtils.isEmpty(one.optString("imageUrl")) ? "" : one.optString("imageUrl")
                        + "?width=180&height=180";
                list.add(new ProductBrief(productId, productName, productPrice, productCount,
                        productImage, totalPrice, imei, sn, isMobile));
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }
    

    public static ArrayList<OrderExpressTrace> getExpressTraces(JSONObject json) {
        ArrayList<OrderExpressTrace> traces = new ArrayList<OrderExpressTrace>();
        JSONArray list = json.optJSONArray(Tags.Order.EXPRESS_TRACE);
        if (list == null) {
            return null;
        }
        for (int i = 0; i < list.length(); i++) {
            JSONObject one = list.optJSONObject(i);
            if (one != null) {
                OrderExpressTrace t = new OrderExpressTrace();
                t.mText = one.optString(Tags.Order.EXPRESS_TRACE_TEXT);
                t.mTime = one.optString(Tags.Order.EXPRESS_TRACE_TIME);
                t.mType = Constants.OrderExpressType.ORDER_EXPRESS_LIST_TYPE_DEFAULT;
                traces.add(0, t);
            }
        }
        return traces;
    }

    public int getProvinceId() {
        return mProvinceId;
    }

    public void setProvinceId(int mProvinceId) {
        this.mProvinceId = mProvinceId;
    }

    public String getReducePrice() {
        return mReducePrice;
    }

    public void setReducePrice(String reducePrice) {
        this.mReducePrice = reducePrice;
    }

    public String getShipmentExpense() {
        return mShipmentExpense;
    }

    public void setShipmentExpense(String shipmentExpense) {
        this.mShipmentExpense = shipmentExpense;
    }

    public String getOriginalPrice() {
        return mOriginalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        this.mOriginalPrice = originalPrice;
    }

    public int getCityId() {
        return mCityId;
    }

    public void setCityId(int mCityId) {
        this.mCityId = mCityId;
    }

    public int getDistrictId() {
        return mDistrictId;
    }

    public void setDistrictId(int mDistrictId) {
        this.mDistrictId = mDistrictId;
    }

    public void setDeliverOrderList(ArrayList<DeliverOrder> deliverOrderList) {
        mDeliverOrderList = deliverOrderList;
    }

    public ArrayList<DeliverOrder> getDeliverOrderList() {
        return mDeliverOrderList;
    }

    public void setPickupInfo(PickupInfo pickupInfo) {
        mPickupInfo = pickupInfo;
    }

    public PickupInfo getPickupInfo() {
        return mPickupInfo;
    }

    public String getOrderPayNo() {
        return mOrderPayNo;
    }

    public void setOrderPayNo(String mOrderPayNo) {
        this.mOrderPayNo = mOrderPayNo;
    }

    public String getOrderPayTime() {
        return mOrderPayTime;
    }

    public void setOrderPayTime(String mOrderPayTime) {
        this.mOrderPayTime = mOrderPayTime;
    }

    public String getOrderUserName() {
        return mOrderUserName;
    }

    public void setOrderUserName(String mOrderUserName) {
        this.mOrderUserName = mOrderUserName;
    }

    public String getOrderUserTel() {
        return mOrderUserTel;
    }

    public void setOrderUserTel(String mOrderUserTel) {
        this.mOrderUserTel = mOrderUserTel;
    }

    public String getOrderUserEmail() {
        return mOrderUserEmail;
    }

    public void setOrderUserEmail(String mOrderUserEmail) {
        this.mOrderUserEmail = mOrderUserEmail;
    }

    public static Order valueOf(JSONObject json) throws JSONException {
        Order order = null;
        if (Tags.isJSONReturnedOK(json)) {
            String bodyStr = json.optString(Tags.BODY);
            if (!TextUtils.isEmpty(bodyStr)) {
                JSONObject bodyJson = new JSONObject(bodyStr);
                if (bodyJson != null) {
                    JSONObject dataJson = bodyJson.optJSONObject("data");
                    JSONObject posInfoJson = bodyJson.optJSONObject("posInfo");
					JSONObject payCompanyInfoJson = bodyJson.optJSONObject("payCompanyInfo");
					ArrayList<ProductBrief> productSns = getProductSnList(bodyJson);
                    if (dataJson != null) {
                        // product list
                        ArrayList<ProductBrief> products = getProducts(dataJson);
                        // own fields
                        String consignee = dataJson.optString(Tags.Order.CONSIGNEE);
                        double fee = dataJson.optDouble("realTotalPrice");
                        String reducePrice = dataJson.optString(Tags.Order.REDUCE_PRICE);
                        String originalPrice = dataJson.optString(Tags.Order.ORIGINAL_PRICE);
                        String shipmentExpress = dataJson.optString(Tags.Order.SHIPMENT_EXPRENSE);
                        String orderId = dataJson.optString("serviceNumber");
                        String invoiceTitle = dataJson.optString(Tags.Order.INVOICE_TITLE);
                        String deliveryTime = dataJson.optString(Tags.Order.BEST_TIME);
                        String addTime = dataJson.optString("addTime");
                        String consigneePhone = dataJson.optString(Tags.Order.CONSIGNEE_PHONE);
                        String address = dataJson.optString(Tags.Order.ADDRESS);
                        int status = dataJson.optInt("orderStatus");
                        int orderType = dataJson.optInt("orderType");
                        String statusStr = dataJson.optString("orderStatusName");
                        String zipcode = dataJson.optString(Tags.Order.ZIPCODE);
                        boolean hasPhone = dataJson.optBoolean(Tags.Order.HAS_PHONE, false);
                        order = new Order(orderId, fee, consignee, consigneePhone, address, deliveryTime,
                                addTime, invoiceTitle, status, statusStr, null, null, null,
                                zipcode, null, products, null, hasPhone, null, null);
                        order.setmProductSnList(productSns);
                        order.setShipmentExpense(shipmentExpress);
                        order.setReducePrice(reducePrice);
                        order.setOriginalPrice(originalPrice);
                        String orderPayNo = dataJson.optString("companyOrderNumber");
                        String orderPayTime = dataJson.optString("payTime");
                        String orderUserName = dataJson.optString("consignee");
                        String orderUserTel = dataJson.optString("tel");
                        String orderUserEmail = dataJson.optString("email");
                        order.setOrderPayNo(orderPayNo);
                        order.setOrderPayTime(orderPayTime);
                        order.setOrderUserName(orderUserName);
                        order.setOrderUserTel(orderUserTel);
                        order.setOrderUserEmail(orderUserEmail);
                        order.setOrderType(orderType);  //订单类型
                        if(dataJson.has(Tags.Order.ORDER_STATUS_DESC)){
							String orderStatusDesc = dataJson.optString(Tags.Order.ORDER_STATUS_DESC);
							order.setOrderStatusInfo(orderStatusDesc);
						}
                        JSONArray subSalesOrderList = dataJson.optJSONArray("subSalesOrderList");
						if(subSalesOrderList != null){
							if(subSalesOrderList.length() == 1){
								if(subSalesOrderList.optJSONObject(0).optString("payId").equals("100") 
										&& subSalesOrderList.optJSONObject(0).optString("orderStatus").equals("39")
										&& subSalesOrderList.optJSONObject(0).optString("orderStatusDesc").equals("已退款")){
									String orderStatusDesc = subSalesOrderList.optJSONObject(0).optString("orderStatusDesc");
									order.setOrderStatusInfo(orderStatusDesc);
								}
							}
						}
                        order.setPayId(dataJson.optInt("payId"));
                        order.setOrgName(dataJson.optString("orgName"));
                        order.setOrgAddress(dataJson.optString("orgAddress"));
                        order.setOrgTel(dataJson.optString("orgTel"));
                        order.setMituShuo(dataJson.optString("mituShuo"));
                    }
                    if (posInfoJson != null) {
						if(posInfoJson.has(Tags.Order.DEVICE_NAME)){
							String deviceName = posInfoJson.optString(Tags.Order.DEVICE_NAME);
							order.setmPosName(deviceName);
						}
					}
					if(bodyJson.has("companyName")){
						String companyName = bodyJson.optString("companyName");
						order.setmMerchantName(companyName);
					}
					if (payCompanyInfoJson != null) {
						if(payCompanyInfoJson.has(Tags.Order.RETURN_POSREQUSTID)){
							String posRequstId = payCompanyInfoJson.optString(Tags.Order.RETURN_POSREQUSTID);
							order.setPosRequstId(posRequstId);
						}
						if(payCompanyInfoJson.has(Tags.Order.RETURN_REFERENCENUMBER)){
							String referenceNumber = payCompanyInfoJson.optString(Tags.Order.RETURN_REFERENCENUMBER);
							order.setReferenceNumber(referenceNumber);
						}
						if(payCompanyInfoJson.has(Tags.Order.RETURN_PAY_STATUS)){
							String payStatus = payCompanyInfoJson.optString(Tags.Order.RETURN_PAY_STATUS);
							order.setmPayStatus(payStatus);
						}
					}
                }
            }
        }
        return order;
    }

    public static String getErrorInfo(JSONObject json) {
        return json.optString(Tags.DESCRIPTION);
    }

    public static String getErrorDescInfo(JSONObject json) throws JSONException {
		String headerStr = json.optString(Tags.HEADER);
		JSONObject headerJson = new JSONObject(headerStr);
		return headerJson.optString(Tags.DESC);
	}
    
	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgTel() {
		return orgTel;
	}

	public void setOrgTel(String orgTel) {
		this.orgTel = orgTel;
	}

	public String getOrgAddress() {
		return orgAddress;
	}

	public void setOrgAddress(String orgAddress) {
		this.orgAddress = orgAddress;
	}

	public int getPayid() {
		return payId;
	}

	public void setPayId(int payId) {
		this.payId = payId;
	}

	public String getMituShuo() {
		return mituShuo;
	}

	public void setMituShuo(String mituShuo) {
		this.mituShuo = mituShuo;
	}

	public String getmMerchantName() {
		return mMerchantName;
	}

	public void setmMerchantName(String mMerchantName) {
		this.mMerchantName = mMerchantName;
	}

	public String getmPosName() {
		return mPosName;
	}

	public void setmPosName(String mPosName) {
		this.mPosName = mPosName;
	}

	public String getPosRequstId() {
		return posRequstId;
	}

	public void setPosRequstId(String posRequstId) {
		this.posRequstId = posRequstId;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getmPayStatus() {
		return mPayStatus;
	}

	public void setmPayStatus(String mPayStatus) {
		this.mPayStatus = mPayStatus;
	}

	public ArrayList<ProductBrief> getmProductSnList() {
		return mProductSnList;
	}

	public void setmProductSnList(ArrayList<ProductBrief> mProductSnList) {
		this.mProductSnList = mProductSnList;
	}

}
