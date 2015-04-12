
package com.xiaomi.xms.sales.model;

public class CheckoutFormInfo {
    private String mAddressId;
    private String mPayId;
    private String mPickupId;
    private String mShipmentId;
    private String mBestTimeId;
    private String mInvoiceType;
    private String mInvoiceTitle;
    private String mCouponType;
    private String mCouponCode;
    private ShoppingCartListInfo mCart;
    private String mMihomeBuyId;
    private String mMihomeBuyConsignee;
    private String mMihomeBuyTel;

    public String getMihomeBuyId() {
        return mMihomeBuyId;
    }

    public void setMihomeBuyId(String mihomeBuyId) {
        this.mMihomeBuyId = mihomeBuyId;
    }

    public String getMihomeBuyConsignee() {
        return this.mMihomeBuyConsignee;
    }

    public void setMihomeBuyConsignee(String mihomeBuyConsignee) {
        this.mMihomeBuyConsignee = mihomeBuyConsignee;
    }

    public String getMihomeBuyTel() {
        return this.mMihomeBuyTel;
    }

    public void setMihomeBuyTel(String mihomeBuyTel) {
        this.mMihomeBuyTel = mihomeBuyTel;
    }

    public ShoppingCartListInfo getCart() {
        return mCart;
    }

    public void setCart(ShoppingCartListInfo cart) {
        this.mCart = cart;
    }

    public String getAddressId() {
        return mAddressId;
    }

    public void setAddressId(String addressId) {
        this.mAddressId = addressId;
    }

    public String getPayId() {
        return mPayId;
    }

    public void setPayId(String payId) {
        this.mPayId = payId;
    }

    public String getPickupId() {
        return mPickupId;
    }

    public void setPickupId(String pickupId) {
        this.mPickupId = pickupId;
    }

    public String getShipmentId() {
        return mShipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.mShipmentId = shipmentId;
    }

    public String getBestTimeId() {
        return mBestTimeId;
    }

    public void setBestTimeId(String bestTimeId) {
        this.mBestTimeId = bestTimeId;
    }

    public String getInvoiceType() {
        return mInvoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.mInvoiceType = invoiceType;
    }

    public String getInvoiceTitle() {
        return mInvoiceTitle;
    }

    public void setInvoiceTitle(String invoiceTitle) {
        this.mInvoiceTitle = invoiceTitle;
    }

    public String getCouponType() {
        return mCouponType;
    }

    public void setCouponType(String couponType) {
        this.mCouponType = couponType;
    }

    public String getCouponCode() {
        return mCouponCode;
    }

    public void setCouponCode(String couponCode) {
        this.mCouponCode = couponCode;
    }
}
