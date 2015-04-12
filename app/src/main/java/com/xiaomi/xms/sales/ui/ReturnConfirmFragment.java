package com.xiaomi.xms.sales.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.SameDayReturnActivity;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader.PrinterMode;
import com.xiaomi.xms.sales.loader.RefundConfirmLoader;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.yeepay.bluetooth.pos.controll.BizConstant;
import com.yeepay.bluetooth.pos.controll.IController;
import com.yeepay.bluetooth.pos.controll.Me31Controller;
import com.yeepay.bluetooth.pos.user.BizResult;
import com.yeepay.bluetooth.pos.user.OnUserCallback;

public class ReturnConfirmFragment extends BaseFragment {
	private static final String TAG = "ReturnConfirmFragment";
	public static final int ADD_PAYTYPE = 1;
	public static final int GET_PRINTER_LIST = 2;
	public static int PAY_TYPE = 100;
	private static final int PACKED_INFO_LOADER = 0;
	private static final int REFUND_CONFIRM_LOADER = 1;
	private static final int PAY_CHECK = 1000;
	private static final int GET_ORDER_INFO = 1001;
	private static final int SAVE_PRINTER = 100;
	private static final int CONNECTLOST = 1;
	private static final int CLOSEDIALOG = 2;
	private static final int PRINTSTART = 3;
	private static final int PRINTEND = 4;
	private static final int CANCELLED = 5;
	private Button mPayButton;
	private ReturnOrderLayout mReturnOrderLayout;
	private Order mOrder;
	private String mOrderId;
	private Bundle mBundle;
	private ArrayList<PrinterMode> mPrinterList;
	private int orderType;
	private String actionType = "";
	private IController mcontroller;
	private Handler mHandler;
	private ProgressDialog mProgressDialog;
	private String mOrderIdStr;
	private String isGood;
	private String mReferencenumber;
	private String mRosRequstid;
	private String cancelAmount;
	private EmptyLoadingView mLoadingView;
	private boolean isAsynSuccess = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.return_confirm_fragment, container, false);

		mReferencenumber = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_REFERENCE_NUMBER, "");
		mRosRequstid = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_POSREQUSTID, "");
		cancelAmount = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_ORDER_FEE, "");
		isGood = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_CHOICE_RESULT, "1");
		mOrderIdStr = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_SERVICENUMBER, "");
		mcontroller = Me31Controller.getInstance();
		mcontroller.setUserCallback(mOnUserCallback);
		mPayButton = (Button) v.findViewById(R.id.return_cancel_btn);
		mPayButton.setOnClickListener(mPayButtonClickListener);
		mReturnOrderLayout = (ReturnOrderLayout) v.findViewById(R.id.return_order_table);
		mLoadingView = (EmptyLoadingView) v.findViewById(R.id.loading);
		handleIntent();
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setCancelable(false);
		mHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case CONNECTLOST:
					// 如果蓝牙断开了连接，则取消所有的任务
					mProgressDialog.dismiss();
					mcontroller.cancelTask();
					if (getActivity() != null) {
						ToastUtil.show(getActivity(), "蓝牙连接断开了");
					}
					break;
				case CLOSEDIALOG:
					mProgressDialog.dismiss();
					break;
				case PRINTSTART:
					mProgressDialog.dismiss();
					switch (msg.arg1) {
					case BizConstant.BIZ_SIGNIN:
						break;
					case BizConstant.BIZ_CONSUME_REVERSE:
						ToastUtil.show(getActivity(), "交易冲正");
						break;
					case BizConstant.BIZ_CANCEL_CONSUME:
						 mProgressDialog.setMessage("消费撤销成功，正在打印购物小票, 请稍等...");
                         mProgressDialog.show();
						break;
					default:
						break;
					}
					break;
				case CANCELLED:
					mProgressDialog.dismiss();
					switch (msg.arg1) {
					case BizConstant.BIZ_CONSUME_REVERSE:
						ToastUtil.show(getActivity(), "交易冲正");
						break;
					case BizConstant.BIZ_PRINT:
					case BizConstant.BIZ_CANCEL_CONSUME:
						returnAsynInfo();
						break;
					case BizConstant.BIZ_CONSUME:
						break;
					default:
						break;
					}
					break;
				case PRINTEND:
					mProgressDialog.dismiss();
					switch (msg.arg1) {
					case BizConstant.BIZ_CONSUME_REVERSE:
						ToastUtil.show(getActivity(), "交易冲正");
						break;
					case BizConstant.BIZ_PRINT:
					case BizConstant.BIZ_CANCEL_CONSUME:
						cancelFinished();
						break;
					case BizConstant.BIZ_CONSUME:

						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
				return false;
			}

		});

		return v;
	}

	private void cancelFinished() {
		if (getActivity() != null && isAsynSuccess) {
			getActivity().finish();
		}
	}
	
	private void returnAsynInfo() {
		if(isAsynSuccess){
			if (getActivity() != null) {
				ToastUtil.show(getActivity(), "消费撤销成功");
				getActivity().finish();
			}
			return;
		}
		
		getLoaderManager().restartLoader(REFUND_CONFIRM_LOADER, null, new LoaderCallbacks<RefundConfirmLoader.Result>() {

			@Override
			public RefundConfirmLoader onCreateLoader(int id, Bundle arg1) {
				if (id == REFUND_CONFIRM_LOADER) {
					RefundConfirmLoader refundConfirmLoader = new RefundConfirmLoader(getActivity(), mOrderIdStr, isGood);
					refundConfirmLoader.setNeedDatabase(false);
					refundConfirmLoader.setProgressNotifiable(mLoadingView);
					return refundConfirmLoader;
				}
				return null;
			}

			@Override
			public void onLoadFinished(Loader<RefundConfirmLoader.Result> loader, RefundConfirmLoader.Result data) {
				if (data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")) {
					isAsynSuccess = true;
					ToastUtil.show(getActivity(), "消费撤销成功");
					
				} else {
					ToastUtil.show(getActivity(), data.responseInfo);
					setSumbitButtonAttribute(true, R.string.return_asyninfo);
					isAsynSuccess = false;
				}

			}

			@Override
			public void onLoaderReset(Loader<RefundConfirmLoader.Result> arg0) {

			}
		});
	}
	
	private void handleIntent() {
		mBundle = getArguments();
		if (mBundle != null) {
			mOrder = new Order();
			actionType = mBundle.getString(Constants.Intent.EXTRA_RETURN_SWITCH_TO_RETURN);
			if (actionType == null) {
				actionType = "";
			}
			mOrderId = mBundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
			orderType = mBundle.getInt(Constants.Intent.EXTRA_ORDER_TYPE);
			mOrder.setAddTime(Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_ORDER_DATE, ""));
			mOrder.setmPosName(Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_ORDER_DEVICE_NAME, ""));
			mOrder.setmMerchantName(Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_MERCHANT_NAME, ""));
			mOrder.setOrderId(Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_RETURN_ORDER_ID, ""));
			mOrder.setFee(Double.parseDouble(Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_ORDER_FEE, "")));
			mOrder.setOrderType(orderType);
		} else {
			mBundle = new Bundle();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mReturnOrderLayout.fillOrderTable(mOrder);

	}

	private OnClickListener mPayButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (actionType.equals("returnOrder")) {

				if (TextUtils.equals(mPayButton.getText(), getString(R.string.bluetooth_pos_reprint))) {
					mPayButton.setEnabled(false);
					mcontroller.printPre();
				} else if (TextUtils.equals(mPayButton.getText(), getString(R.string.bluetooth_pos_resign))) { // 重新签到
					mPayButton.setEnabled(false);
					mcontroller.signIn();
				} else if (TextUtils.equals(mPayButton.getText(), getString(R.string.return_asyninfo))) { // 重新同步信息
					returnAsynInfo();
				}else {
					setSumbitButtonAttribute(false, R.string.return_returning);
					mProgressDialog.setMessage("开始撤销...");
					mProgressDialog.show();
					mcontroller.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
					mcontroller.cancelConsume(Float.valueOf(cancelAmount), mReferencenumber, mRosRequstid);
				}
			} else {
				SameDayReturnActivity father = (SameDayReturnActivity) getActivity();
				father.showFragment(SameDayReturnActivity.Fragments.TAG_RETURN_BLUETOOTH_FRAGMENT, mBundle, true);
			}

		}

		
	};

	private OnUserCallback mOnUserCallback = new OnUserCallback() {

		@Override
		public void onTestData(final int bizType, final String data) {
			LogUtil.i(TAG, "onTestData bizType = " + getBizType(bizType));
			LogUtil.i(TAG, "onTestData data = " + data);
		}

		@Override
		public void onSuccess(int bizType, BizResult result) {
			LogUtil.i(TAG, "onSuccess bizType = " + getBizType(bizType));
			LogUtil.i(TAG, "onSuccess BizResult = " + result);
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,getBizTypeCode(bizType)+"_SUCCESS",cancelAmount+"_"+orderType+"_"+mcontroller.getPosInfo(),result.toString());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
			mHandler.sendEmptyMessage(CLOSEDIALOG);
			/**
			 * 这个接口如果是消费类型的话回调不一定代表成功消费 只是代表期间没有异常，需要判断mSuccess是否为TRUE
			 */
			switch (bizType) {
			case BizConstant.BIZ_SIGNIN:
				if (result.mSuccess) {
					setSumbitButtonAttribute(true, R.string.bluetooth_pos_submit);
					Utils.Preference.setLongPref(getActivity(), Constants.Prefence.PREF_PAYMENT_POS_SIGNIN_TIME, System.currentTimeMillis());
				} else {
					// 需要重新签到
					ToastUtil.show(getActivity(), R.string.payment_pos_error_sigin);
					setSumbitButtonAttribute(true, R.string.bluetooth_pos_resign);
				}
				break;

			case BizConstant.BIZ_CONSUME:

				break;
			case BizConstant.BIZ_CONSUME_REVERSE:
				ToastUtil.show(getActivity(), "交易冲正");
				break;
			case BizConstant.BIZ_CANCEL_CONSUME:
				if (result.mSuccess) {
					Message msg = new Message();
					msg.what = CANCELLED;
					msg.arg1 = bizType;
					mHandler.sendMessage(msg);
				} else {
					ToastUtil.show(getActivity(), "消费撤销失败,请确保使用原卡撤销，请再试一次！");
				}
				break;

			default:
				break;
			}
		}

		@Override
		public void onStart(int bizType) {
			LogUtil.i(TAG, "onStart bizType = " + getBizType(bizType));
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,getBizTypeCode(bizType)+"_START","cancelAmount"+"_"+orderType+"_"+mcontroller.getPosInfo(),"");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
			switch (bizType) {
			case BizConstant.BIZ_SIGNIN:
				mProgressDialog.setMessage("正在签到, 请稍等...");
				break;
			case BizConstant.BIZ_CONSUME:
				mProgressDialog.setMessage("开始支付, 请稍等...");
				break;
			case BizConstant.BIZ_CONSUME_REVERSE:
				ToastUtil.show(getActivity(), "交易冲正");
				break;
			case BizConstant.BIZ_CANCEL_CONSUME:
				mProgressDialog.setMessage("正在撤销消费, 请稍等...");
				break;
			default:
				break;
			}
			mProgressDialog.show();
		}

		@Override
		public void onPrintStart(final int bizType) {
			LogUtil.i(TAG, "onPrintStart bizType = " + getBizType(bizType));
			 try {
	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.PRINT_START,"","");
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
			Message msg = new Message();
			msg.what = PRINTSTART;
			msg.arg1 = bizType;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onPrintEnd(int bizType) {
			LogUtil.i(TAG, "onPrintEnd bizType = " + getBizType(bizType));
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.PRINT_END,"","");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
			Message msg = new Message();
			msg.what = PRINTEND;
			msg.arg1 = bizType;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onError(int bizType, int errorType) {
			LogUtil.i(TAG, "onError bizType = " + getBizType(bizType) + " errorType = " + errorType);
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,getBizTypeCode(bizType)+"_ERROR","",getErrorTypeCode(errorType));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
			mHandler.sendEmptyMessage(CLOSEDIALOG);
			switch (bizType) {
			case BizConstant.BIZ_SIGNIN:
				ToastUtil.show(getActivity(), "签到失败");
				break;
			case BizConstant.BIZ_CONSUME:
				break;
			case BizConstant.BIZ_CONSUME_REVERSE:
				ToastUtil.show(getActivity(), "交易冲正失败");
				break;
			case BizConstant.BIZ_CANCEL_CONSUME:
				if (errorType == BizConstant.BIZ_ERROR_NO_PAPER) {
					ToastUtil.show(getActivity(), "Pos打印无纸，请重新打印");
					setSumbitButtonAttribute(true, R.string.bluetooth_pos_reprint);
				} else if (BizConstant.BIZ_ERROR_NEED_SIGNIN == errorType) {
					mcontroller.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
					mcontroller.signIn();
				} else {
					ToastUtil.show(getActivity(), "消费撤销失败，请稍候再试");
					ToastUtil.show(getActivity(), R.string.payment_pos_error_info);
					setSumbitButtonAttribute(true, R.string.return_cancel);
				}
				
				break;
			case BizConstant.BIZ_PRINT:
				if (errorType == BizConstant.BIZ_ERROR_NO_PAPER) {
					ToastUtil.show(getActivity(), "Pos打印无纸，请重新打印");
					setSumbitButtonAttribute(true, R.string.bluetooth_pos_reprint);
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onEnd(int bizType) {
			LogUtil.i(TAG, "onEnd bizType = " + getBizType(bizType));
			mHandler.sendEmptyMessage(CLOSEDIALOG);
		}

		@Override
		public void onConnectLost(Throwable arg0) {
			LogUtil.i(TAG, "onConnectLost");
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.POS_ONCONNECT_LOST,"","");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
			mHandler.sendEmptyMessage(CONNECTLOST);

		}

		@Override
		public void onInputPin() {
			mProgressDialog.setMessage("请输入银行卡密码...");
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.ON_INPUTPIN,"","");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

		}

		@Override
		public void onSwipeCard() {
			mProgressDialog.setMessage("请刷卡...");
			try {
    			LogHelper.getInstance(getActivity()).save(mOrderIdStr, Constants.LogType.ON_SWIPECARD,"","");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

		}

		@Override
		public void onInputPinEnd() {
			mProgressDialog.setMessage("验证中...");
		}

		@Override
		public void onSendData(int arg0) {
			mProgressDialog.setMessage("数据发送中...");
		}

		@Override
		public void onSwipeCardEnd(String arg0) {
			mProgressDialog.setMessage("刷卡成功");

		}
	};

	private String getBizType(int bizType) {
		String result = null;
		switch (bizType) {
		case BizConstant.BIZ_SIGNIN:
			result = "签到";
			break;
		case BizConstant.BIZ_CONSUME:
			result = "消费";
			break;
		case BizConstant.BIZ_CONSUME_REVERSE:
			result = "交易冲正";
			break;
		case BizConstant.BIZ_CANCEL_CONSUME:
			result = "消费撤销";
			break;
		case BizConstant.BIZ_CANCEL_CONSUME_REVERSE:
			result = "消费撤销冲正";
			break;
		case BizConstant.BIZ_SALE_RETURN:
			result = "退货";
			break;
		case BizConstant.BIZ_SETTLEMENT:
			result = "结算";
			break;
		case BizConstant.BIZ_PRINT:
			result = "重新打印";
			break;
		case BizConstant.BIZ_TMS_REQUEST:
			result = "TMS请求";
			break;
		case BizConstant.BIZ_TMS_DOWNLOAD:
			result = "TMS下载";
			break;
		case BizConstant.BIZ_TMS_NOTIFY:
			result = "TMS通知";
			break;
		default:
			result = "未知";
			break;
		}
		return result;
	}

	private void setSumbitButtonAttribute(boolean isClick, int resId) {
		mPayButton.setEnabled(isClick);
		mPayButton.setText(resId);
	}

	private String getErrorTypeCode(int errorType) {
		String result = null;
		switch (errorType) {
		case BizConstant.BIZ_ERROR_NO_PAPER:
			result = "BIZ_ERROR_NO_PAPER";
			break;
		case BizConstant.BIZ_ERROR_NEED_SIGNIN:
			result = "BIZ_ERROR_NEED_SIGNIN";
			break;
		case BizConstant.BIZ_ERROR_ARGUMENT:
			result = "BIZ_ERROR_ARGUMENT";
			break;
		case BizConstant.BIZ_ERROR_CONNECT_POS:
			result = "BIZ_ERROR_CONNECT_POS";
			break;
		case BizConstant.BIZ_ERROR_CONNECT_SERVER:
			result = "BIZ_ERROR_CONNECT_SERVER";
			break;
		case BizConstant.BIZ_ERROR_EMPTY_PRINT_PRE:
			result = "BIZ_ERROR_EMPTY_PRINT_PRE";
			break;
		case BizConstant.BIZ_ERROR_INPUT_PARAM_NOT_FOUND:
			result = "BIZ_ERROR_INPUT_PARAM_NOT_FOUND";
			break;
		case BizConstant.BIZ_ERROR_NEED_CANCEL_CONSUME_REVERSE:
			result = "BIZ_ERROR_NEED_CANCEL_CONSUME_REVERSE";
			break;
		case BizConstant.BIZ_ERROR_NEED_CONSUME_REVERSE:
			result = "BIZ_ERROR_NEED_CONSUME_REVERSE";
			break;
		case BizConstant.BIZ_ERROR_POS_PACK:
			result = "BIZ_ERROR_POS_PACK";
			break;
		case BizConstant.BIZ_ERROR_REVERSE_DATE_ERROR:
			result = "BIZ_ERROR_REVERSE_DATE_ERROR";
			break;
		case BizConstant.BIZ_ERROR_SAIL_RETURN_NOT_ALLOW:
			result = "BIZ_ERROR_SAIL_RETURN_NOT_ALLOW";
			break;
		case BizConstant.BIZ_ERROR_SERVER_DATA:
			result = "BIZ_ERROR_SERVER_DATA";
			break;
		case BizConstant.BIZ_ERROR_SETTLEMENT_STATUS_ERROR:
			result = "BIZ_ERROR_SETTLEMENT_STATUS_ERROR";
			break;
		case BizConstant.BIZ_ERROR_TRADE_FOR_SETTLEMENT_ERROR:
			result = "BIZ_ERROR_TRADE_FOR_SETTLEMENT_ERROR";
			break;
		default:
			result = "BIZ_ERROR_UNKNOW";
			break;
		}
		return result;
	}

	private String getBizTypeCode(int bizType) {
		String result = null;
		switch (bizType) {
		case BizConstant.BIZ_SIGNIN:
			result = "BIZ_SIGNIN";
			break;
		case BizConstant.BIZ_CONSUME:
			result = "BIZ_CONSUME";
			break;
		case BizConstant.BIZ_CONSUME_REVERSE:
			result = "BIZ_CONSUME_REVERSE";
			break;
		case BizConstant.BIZ_CANCEL_CONSUME:
			result = "BIZ_CANCEL_CONSUME";
			break;
		case BizConstant.BIZ_CANCEL_CONSUME_REVERSE:
			result = "BIZ_CANCEL_CONSUME_REVERSE";
			break;
		case BizConstant.BIZ_SALE_RETURN:
			result = "BIZ_SALE_RETURN";
			break;
		case BizConstant.BIZ_SETTLEMENT:
			result = "BIZ_SETTLEMENT";
			break;
		case BizConstant.BIZ_PRINT:
			result = "BIZ_PRINT";
			break;
		case BizConstant.BIZ_TMS_REQUEST:
			result = "BIZ_TMS_REQUEST";
			break;
		case BizConstant.BIZ_TMS_DOWNLOAD:
			result = "BIZ_TMS_DOWNLOAD";
			break;
		case BizConstant.BIZ_TMS_NOTIFY:
			result = "BIZ_TMS_NOTIFY";
			break;
		default:
			result = "BIZ_UNKNOW";
			break;
		}
		return result;
	}

}