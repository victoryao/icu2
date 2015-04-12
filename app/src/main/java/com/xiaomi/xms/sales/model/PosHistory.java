package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Date;

public class PosHistory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -120218308883438644L;

	public String mOrderId;
	public String mRefNo;// 流水号
	public Date addTime;
	public String info;
	public int payId;
	public String mUserId;

	public PosHistory(String mOrderId, String mRefNo, String info, Date addTime) {
		super();
		this.mOrderId = mOrderId;
		this.mRefNo = mRefNo;
		this.info = info;
		this.addTime = addTime;
		payId = 100;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof PosHistory) {
			PosHistory msgInfo = (PosHistory) o;
			return TextUtils.equals(msgInfo.mOrderId, mOrderId);
		}
		return false;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getmOrderId() {
		return mOrderId;
	}

	public void setmOrderId(String mOrderId) {
		this.mOrderId = mOrderId;
	}

	public String getmRefNo() {
		return mRefNo;
	}

	public void setmRefNo(String mRefNo) {
		this.mRefNo = mRefNo;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getmUserId() {
		return mUserId;
	}

	public void setmUserId(String mUserId) {
		this.mUserId = mUserId;
	}

}
