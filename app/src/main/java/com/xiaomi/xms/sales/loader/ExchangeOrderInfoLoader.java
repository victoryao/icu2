package com.xiaomi.xms.sales.loader;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class ExchangeOrderInfoLoader extends BaseLoader<ExchangeOrderInfoLoader.Result> {
	// private static final String TAG = "OrderInfoLoader";
	private static final String CACHE_KEY = "ExchangeOrderInfo";

	private String mOrderId;
	private boolean mNeedSecurityKey;

	public static final class Result extends BaseResult {
		public Order mOrderInfo;
		public String mOrderError;
		public String mSecurityKey; // 安全支付Key

		@Override
		public BaseResult shallowClone() {
			Result newResult = new Result();
			newResult.mOrderInfo = mOrderInfo;
			newResult.mOrderError = mOrderError;
			newResult.mSecurityKey = mSecurityKey;
			return newResult;
		}

		@Override
		protected int getCount() {
			return mOrderInfo == null ? 0 : 1;
		}
	}

	public ExchangeOrderInfoLoader(Context context) {
		super(context);
		mNeedSecurityKey = true;
	}

	public void setOrderId(String orderId) {
		mOrderId = orderId;
	}

	public void setNeedSecurityKeyTask(boolean need) {
		mNeedSecurityKey = need;
	}

	@Override
	protected void initTaskList(List<AsyncTask<Void, Void, Result>> tasks) {
		if (mNeedDatabase) {
			DatabaseTask task = getDatabaseTask();
			if (task != null) {
				tasks.add(task);
			}
		}
		if (mNeedSecurityKey) {
			tasks.add(new SecurityPayUpdateTask());
		}
		tasks.add(new OrderInfoUpdateTask());
	}

	private class OrderInfoUpdateTask extends UpdateTask {
		@Override
		protected Request getRequest() {
			Request request = new Request(HostManager.URL_XMS_SALE_API);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(Tags.XMSAPI.ORGID, Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
				jsonObject.put(Tags.XMSAPI.USERID, LoginManager.getInstance().getUserId());
				jsonObject.put(Tags.XMSAPI.KEYWORDS, mOrderId);
				jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
				jsonObject.put(Tags.XMSAPI.OPTYPE, "QUERY");
				String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_DAYEXCHANGEREQUEST, jsonObject);
				if (!TextUtils.isEmpty(data)) {
					request.addParam(Tags.RequestKey.DATA, data);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return request;
		}
	}

	private class SecurityPayUpdateTask extends UpdateTask {
		@Override
		protected Request getRequest() {
			Request request = new Request(HostManager.getSecurityPay());
			request.addParam(Parameters.Keys.ORDER_ID, mOrderId);
			return request;
		}

		@Override
		protected Result parseTaskResult(JSONObject json) {
			Result result = getResultInstance();
			if (json != null) {
				String security = json.optString(Parameters.Keys.SECURITY_PAYMENT_KEY);
				if (!TextUtils.isEmpty(security)) {
					result.mSecurityKey = security;
				} else {
					result.setResultStatus(ResultStatus.DATA_ERROR);
				}
			} else {
				result.setResultStatus(ResultStatus.DATA_ERROR);
			}
			return result;
		}

		@Override
		protected Result onDataLoaded(Result oldResult, Result newResult) {
			if (newResult != null && oldResult != null) {
				newResult.mOrderInfo = oldResult.mOrderInfo;
			}
			return newResult;
		}
	}

	@Override
	protected String getCacheKey() {
		return CACHE_KEY + mOrderId;
	}

	@Override
	protected boolean isUserRelated() {
		return true;
	}

	@Override
	protected Result parseResult(JSONObject json, Result result) throws Exception {
		result.mOrderInfo = Order.valueOf(json);
		
		if (result.mOrderInfo == null) {
			result.mOrderError = Order.getErrorDescInfo(json);
		}else{
			Utils.Preference.setStringPref(getContext(), Constants.SameDayReturn.PREF_KEY_RETURN_ORDER_ID, result.mOrderInfo.getOrderId());
			Utils.Preference.setStringPref(getContext(), Constants.SameDayReturn.PREF_KEY_ORDER_FEE, String.valueOf(result.mOrderInfo.getFee()));
		}
		return result;
	}

	@Override
	protected Result getResultInstance() {
		return new Result();
	}
}
