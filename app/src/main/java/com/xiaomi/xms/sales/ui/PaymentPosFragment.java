
package com.xiaomi.xms.sales.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.loader.PackedLoader;
import com.xiaomi.xms.sales.loader.PayCheckLoader;
import com.xiaomi.xms.sales.loader.PosPayLoader;
import com.xiaomi.xms.sales.model.PosHistory;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.yeepay.bluetooth.pos.controll.BizConstant;
import com.yeepay.bluetooth.pos.controll.IController;
import com.yeepay.bluetooth.pos.controll.Me31Controller;
import com.yeepay.bluetooth.pos.user.BizResult;
import com.yeepay.bluetooth.pos.user.OnUserCallback;

public class PaymentPosFragment extends BaseFragment{
    private static final String TAG = "PaymentPosFragment";
    private static final int PACKED_INFO_LOADER = 0;
    private static final int POS_PAY_LOADER = 1;
    private static final int PAY_CHECK = 1000;
    private static final int GET_ORDER_INFO = 1001;
    private static final int SAVE_PRINTER = 100;
    private static final int CONNECTLOST = 1;
    private static final int CLOSEDIALOG = 2;
    private static final int PRINTSTART = 3;
    private static final int PRINTEND = 4;
    private static final int DOUBLEPAID = 5;
    private TextView mMerchantName;
    private TextView mOrderId;
    private TextView mOrderTime;
    private TextView mPosName;
    private TextView mPosFee;
    private Button mSubmitBtn;
    private IController mController;
    private ProgressDialog mProgressDialog;
    private String mFee;
    private String mOrderIdStr;
    private Handler mHandler;
    private int orderType;
    private String printerIP;
    private EmptyLoadingView mLoadingView;
    private Bundle bundle;
    private String info;
    String posInfo;
    PosHistory savedPosHistory;
    private String mUserId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_pos_fragment, container, false);
        mMerchantName = (TextView) view.findViewById(R.id.payment_pos_merchant_name);
        mOrderId = (TextView) view.findViewById(R.id.payment_order_id);
        mOrderTime = (TextView) view.findViewById(R.id.payment_order_add_time);
        mPosName = (TextView) view.findViewById(R.id.payment_pos_name);
        mPosFee = (TextView) view.findViewById(R.id.payment_pos_fee);
        mSubmitBtn = (Button) view.findViewById(R.id.payment_pos_submit);
        handleIntent();
        mController = Me31Controller.getInstance();
        mController.setUserCallback(mOnUserCallback);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mUserId = LoginManager.getInstance().getUserId();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTLOST:
                        // 如果蓝牙断开了连接，则取消所有的任务
                        mProgressDialog.dismiss();
                        mController.cancelTask();
                        if (getActivity() != null) {
                            ToastUtil.show(getActivity(), "蓝牙连接断开了");
                        }
                     
                        break;
                    case DOUBLEPAID:
                        // 如果已经支付过，则取消所有的任务
                        mProgressDialog.dismiss();
                        mController.cancelTask();
                        if (getActivity() != null) {
                            ToastUtil.show(getActivity(), "已经支付成功，不得重复支付，请到订单详情同步后台信息");
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
                            case BizConstant.BIZ_CONSUME:
                                mProgressDialog.setMessage("付款成功，正在打印购物小票, 请稍等...");
                                mProgressDialog.show();
                                break;
                            case BizConstant.BIZ_CONSUME_REVERSE:
                            	ToastUtil.show(getActivity(), "交易冲正");
                            	break;
                            case BizConstant.BIZ_CANCEL_CONSUME:
                            	ToastUtil.show(getActivity(), "消费撤销");
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
	                        case BizConstant.BIZ_CANCEL_CONSUME:
	                        	ToastUtil.show(getActivity(), "消费撤销");
	                        	break;
	                        case BizConstant.BIZ_PRINT:
                            case BizConstant.BIZ_CONSUME:
                                Intent intent = new Intent(getActivity(),OrderEditActivity.class);
                                intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
                                intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID,mOrderIdStr);
                                intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE,orderType);
                                intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION,"ADD");
                                intent.putExtra(Constants.Intent.EXTRA_PRINTER_IP, printerIP);
                                startActivity(intent);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
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
        mSubmitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	payAction();
            }
        });
        if (mController.isConnected()) {
        	try{
        		posInfo = mController.getPosInfo();
        		String[] data = posInfo.split("\\|");
                mMerchantName.setText(data[2]);
        	}catch(Exception e){
        		posInfo = "";
        	}
        }
        try {
			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.BIZ_SIGNIN,"","");
		} catch (Exception e) {
			e.printStackTrace();
		}
        mController.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
        mController.signIn();
        
        /*
         * long signinTime = Utils.Preference.getLongPref(getActivity(),
         * Constants.Prefence.PREF_PAYMENT_POS_SIGNIN_TIME, 0); if (signinTime
         * == 0 || System.currentTimeMillis() - signinTime > 0.5 *
         * Constants.DAY_IN_MILLIS) {
         * mController.setServerIp(HostManager.YeePay.SERVERIP,
         * HostManager.YeePay.SERVERPORT); mController.signIn(); } else {
         * setSumbitButtonAttribute(true, R.string.bluetooth_pos_submit); }
         */
    }

    private void handleIntent() {
         bundle = getArguments();
        if (bundle != null) {
            mFee = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_TOTAL_PRICE);
            mOrderIdStr = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            orderType = bundle.getInt(Constants.Intent.EXTRA_ORDER_TYPE);
            printerIP = bundle.getString(Constants.Intent.EXTRA_PRINTER_IP);
            mOrderId.setText(mOrderIdStr);
            mPosFee.setText("¥ " + mFee);
            mPosName.setText(bundle.getString(Constants.Intent.EXTRA_PAYMENT_POS_BLUETOOTH_NAME));
            mOrderTime.setText(bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ADD_TIME));
        }
    }

    /**
     * 支付验证并处理后续流程
     */
    public void payAction(){
    	
    		mSubmitBtn.setEnabled(false);
    		
    		getLoaderManager().restartLoader(PAY_CHECK,null, new LoaderCallbacks<PayCheckLoader.Result>() {
        		
        	    @SuppressWarnings("unchecked")
    			@Override
        	    public Loader<PayCheckLoader.Result> onCreateLoader(int id, Bundle bundle) {
        	        if (id == PAY_CHECK ) {
        	        	try {
        	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.PAY_CHECK_START,"","");
        	    		} catch (Exception e) {
        	    			e.printStackTrace();
        	    		}
        	        	mLoader = new PayCheckLoader(getActivity(),mOrderIdStr);
        	        	mLoader.setNeedDatabase(false);
        	        	mLoader.setProgressNotifiable(mLoadingView);
        	            return (Loader<PayCheckLoader.Result>) mLoader;
        	        }
        	        return null;
        	    }

        	    @Override
        	    public void onLoadFinished(Loader<PayCheckLoader.Result> loader,PayCheckLoader.Result data) {
        	    	try {
    	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.PAY_CHECK_END,"",data.responseInfo);
    	    		} catch (Exception e) {
    	    			e.printStackTrace();
    	    		}
        	    	if(data.responseInfo.equals("ok")){
        	    		if (TextUtils.equals(mSubmitBtn.getText(), getString(R.string.bluetooth_pos_submit))) {
        	        		if(!isOrderPaid()){
    	                        setSumbitButtonAttribute(false, R.string.doing_payment_pos);
    	                        mController.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
    	                        mController.consume(Float.valueOf(mFee), mOrderIdStr);
        	        		}else{
        	            		ToastUtil.show(getActivity(), "此订单已经支付成功，不得重复支付,请截屏并联系开发人员");
        	            	}
                        } else if (TextUtils.equals(mSubmitBtn.getText(), getString(R.string.bluetooth_pos_reprint))) {
                            mSubmitBtn.setEnabled(false);
                            mController.printPre();
                        }   
                        else if (TextUtils.equals(mSubmitBtn.getText(),getString(R.string.bluetooth_pos_resign))) {  //重新签到
                             mSubmitBtn.setEnabled(false);
                             mController.signIn();
                         }
        	    	}else{
        	    		ToastUtil.show(getActivity(), data.responseInfo);
        	    		mSubmitBtn.setEnabled(true);
        	    	}
    	        	
        	    }

    			@Override
    			public void onLoaderReset(Loader<PayCheckLoader.Result> arg0) {
    				
    			}

        	});
    	
    }

	private boolean isOrderPaid() {
		String preOrderId = Utils.Preference.getStringPref(getActivity(), Constants.Prefence.PREF_PREVIOUS_PAID_ORDER_ID, "");
		if(preOrderId == null){
			return false;
		}
		if(mOrderIdStr.equals(preOrderId)){
			return true;
		}
		return false;
	}
    
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
	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,getBizTypeCode(bizType)+"_SUCCESS",mFee+"_"+orderType+"_"+posInfo+"_"+bizType,result.toString());
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
            mHandler.sendEmptyMessage(CLOSEDIALOG);
            /**
             * 这个接口如果是消费类型的话回调不一定代表成功消费 只是代表期间没有异常，需要判断mSuccess是否为TRUE
             */
            switch (bizType) {
                case BizConstant.BIZ_SIGNIN:
                	if(result.mSuccess){
                		 setSumbitButtonAttribute(true, R.string.bluetooth_pos_submit);
                         Utils.Preference.setLongPref(getActivity(),
                                 Constants.Prefence.PREF_PAYMENT_POS_SIGNIN_TIME, System.currentTimeMillis());
                	}
                	else{
                		// 需要重新签到
                        ToastUtil.show(getActivity(), R.string.payment_pos_error_sigin);
                        setSumbitButtonAttribute(true, R.string.bluetooth_pos_resign);
                	}
                    break;
                
                case BizConstant.BIZ_CONSUME:
                    if (result.mSuccess) {
                    	
                        setSumbitButtonAttribute(false, R.string.payment_pos_success);
                        JSONObject dataJson = new JSONObject();
                        try {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("success", result.mSuccess);
                            jsonObj.put("amount", result.mAmount);
                            jsonObj.put("code", result.mCode);
                            jsonObj.put("codemessage", result.mCodeMessage);
                            jsonObj.put("refno", result.mRefNo);
                            jsonObj.put("info", result);
                            jsonObj.put("posInfo", posInfo);
                            dataJson.put("data", jsonObj);
                            info = dataJson.toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                         
                 /*       Intent intent = new Intent(getActivity(), ShopIntentService.class);
                        intent.setAction(Constants.Intent.ACTION_ORDER_PAYMENT_SUCCESS);
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderIdStr);
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_POS_SUCCESS_INFO, dataJson.toString());
                        getActivity().startService(intent);
                   */     
                        ToastUtil.show(getActivity(), "消费成功，交易流水号是："+result.mRefNo);
                       
                        Utils.Preference.setStringPref(getActivity(),
                                Constants.Prefence.PREF_PREVIOUS_PAID_ORDER_ID, mOrderIdStr);
                        backUpOrderIdRefNo(result,info);
                        try {
         	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.BACK_UP_POS_INFO,mFee+"_"+orderType+"_"+posInfo+"_"+result.mSuccess,result.toString());
         	    		} catch (Exception e) {
         	    			e.printStackTrace();
         	    		}
                       
                        addPosInfoPackOut();
                      
                        
                    } else {
                    	 try {
         	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,Constants.LogType.CONSUME_RESULT_FALSE,mFee+"_"+orderType+"_"+posInfo+"_"+result.mSuccess,result.toString());
         	    		} catch (Exception e) {
         	    			e.printStackTrace();
         	    		}
                        setSumbitButtonAttribute(true, R.string.bluetooth_pos_submit);
                        if (result.mTmsUpdateFlag) {
                            mController.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
                            mController.tmsRequest();
                        } else {
                            if ("63".equals(result.mCode)) {
                                // 需要重新签到
                                ToastUtil.show(getActivity(), R.string.payment_pos_error_needsigin_info);
                                mController.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
                                mController.signIn();
                            } else {
                                ToastUtil.show(getActivity(), "支付失败，" + result.mCodeMessage);
                            }
                        }
                    }
                    break;
                case BizConstant.BIZ_CONSUME_REVERSE:
                	ToastUtil.show(getActivity(), "交易冲正");
                	break;
                case BizConstant.BIZ_CANCEL_CONSUME:
                	ToastUtil.show(getActivity(), "消费撤销");
                	break;
              
                default:
                    break;
            }
        }

		private void addPosInfoPackOut() {
			getLoaderManager().initLoader(POS_PAY_LOADER, null, new LoaderCallbacks<PosPayLoader.Result>() {
				final String serviceNumber = mOrderIdStr;
				final int ordertype = orderType;
				final String printerip = printerIP;
				final EmptyLoadingView loadingView = mLoadingView;
				@Override
				public PosPayLoader onCreateLoader(int id,Bundle arg1) {
					if(id == POS_PAY_LOADER){
						try {
			      			LogHelper.getInstance(getActivity()).save((System.currentTimeMillis() +","+serviceNumber+","+Constants.LogType.ADD_PAYINTO_BEDIN+","+serviceNumber+"_"+printerip).getBytes());
			      		} catch (Exception e) {
			      			e.printStackTrace();
			      		}
						PosPayLoader posPayLoader = new PosPayLoader(getActivity(),serviceNumber,info);
						posPayLoader.setNeedDatabase(false);
						posPayLoader.setProgressNotifiable(loadingView);
			            return posPayLoader;
					}
					return null;
				}

				@Override
				public void onLoadFinished(Loader<PosPayLoader.Result> loader,PosPayLoader.Result data) {
					if(data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")){
						ArrayList<PosHistory> posHistoryList = getPosHistoryList();
						if(posHistoryList != null && savedPosHistory != null){
							posHistoryList.remove(savedPosHistory);
							savePosHistoryList(posHistoryList);
						}
					  	try {
			      			LogHelper.getInstance(getActivity()).save((System.currentTimeMillis() +","+serviceNumber+","+Constants.LogType.ADD_PAYINTO_END+","+serviceNumber+"_"+printerip).getBytes());
			      		} catch (Exception e) {
			      			e.printStackTrace();
			      		}
						 if (ordertype == 2) { // 现货销售的才会自动配货出库等
			                    getLoaderManager().initLoader(PACKED_INFO_LOADER, null,new LoaderCallbacks<PackedLoader.Result>() {
			                        @Override
			                        public PackedLoader onCreateLoader(int id, Bundle arg1) {
			                            if (id == PACKED_INFO_LOADER) {
			                            	try {
			                        			LogHelper.getInstance(getActivity()).save((System.currentTimeMillis() +","+serviceNumber+","+Constants.LogType.PACKED_INFO_LOADER_BEGIN+","+serviceNumber+"_"+printerip).getBytes());
			                        		} catch (Exception e) {
			                        			e.printStackTrace();
			                        		}
			                                PackedLoader packedLoader = new PackedLoader(getActivity(),serviceNumber);
			                                packedLoader.setNeedDatabase(false);
			                                packedLoader.setProgressNotifiable(loadingView);
			                                return packedLoader;
			                            }
			                            return null;
			                        }

			                        @Override
			                        public void onLoadFinished(Loader<PackedLoader.Result> loader,PackedLoader.Result data) {
			                            LogUtil.d(TAG, "info loaded.");
			                            if (data != null && data.responseInfo != null&& data.responseInfo.equalsIgnoreCase("OK")) {
			                            	try {
			                        			LogHelper.getInstance(getActivity()).save((System.currentTimeMillis() +","+serviceNumber+","+Constants.LogType.PACKED_INFO_LOADER_END+","+serviceNumber+"_"+printerip).getBytes());
			                        		} catch (Exception e) {
			                        			e.printStackTrace();
			                        		}
			                                ToastUtil.show(getActivity(), "配货完成");
			                            
//		                                                Intent intent = new Intent(getActivity(),OrderEditActivity.class);
//                                                        intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
//                                                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID,serviceNumber);
//                                                        intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE,ordertype);
//                                                        intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION,"ADD");
//                                                        intent.putExtra(Constants.Intent.EXTRA_PRINTER_IP, printerip);
//                                                        startActivity(intent);
//                                                        if (getActivity() != null) {
//                                                            getActivity().finish();
//                                                        }
			                            }
			                            else {
			                            	ToastUtil.show(getActivity(), "配货失败，请在订单详情界面选择重新配货");
//		                                            	Intent intent = new Intent(getActivity(), OrderListActivity.class);
//	                                                    intent.setAction(Constants.Intent.ACTION_VIEW_ORDER);
//	                                                    intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, ordertype);
//	                                                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, serviceNumber);
//		                                                startActivity(intent);
//		                                                if (getActivity() != null) {
//		                                                    getActivity().finish();
//		                                                }
			                            }
			                        }

			                        @Override
			                        public void onLoaderReset(Loader<PackedLoader.Result> loader) {
			                        }
			                    });
			                }
//  									 else{
//  	  									Intent intent = new Intent(getActivity(), OrderEditActivity.class);
//  	                                    intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
//  	                                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, serviceNumber);
//  	                                    intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, ordertype);
//  	                                    intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION, "ADD");
//  	                                    startActivity(intent);
////  	                                    if (getActivity() != null) {
////  	                                        getActivity().finish();
////  	                                    }
//  									 }
					  
					}
					else{
						ToastUtil.show(getActivity(), "存储支付信息失败，请联系管理员！");
					}
					
				}

				@Override
				public void onLoaderReset(Loader<PosPayLoader.Result> arg0) {
					// TODO Auto-generated method stub
					
				}
			});
		}

		private void backUpOrderIdRefNo(BizResult result,String info) {
			ArrayList<PosHistory> posHistoryList = getPosHistoryList();
			if(posHistoryList == null){
				posHistoryList = new ArrayList<PosHistory>();
			}
			savedPosHistory = new PosHistory(mOrderIdStr,result.mRefNo,info, new Date());
			savedPosHistory.setmUserId(mUserId);
			if(posHistoryList.size() > 100){
				for(int i=0;i<20;i++){
					posHistoryList.remove(0);
				}
			}
			posHistoryList.add(savedPosHistory);
			savePosHistoryList(posHistoryList);
		}

        @Override
        public void onStart(int bizType) {
            LogUtil.i(TAG, "onStart bizType = " + getBizType(bizType));
            try {
	    			LogHelper.getInstance(getActivity()).save(mOrderIdStr,getBizTypeCode(bizType)+"_START",printerIP+"_"+mFee+"_"+orderType+"_"+posInfo,"");
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
            switch (bizType) {
                case BizConstant.BIZ_SIGNIN:
                    mProgressDialog.setMessage("正在签到, 请稍等...");
                    break;
                case BizConstant.BIZ_CONSUME:
                	if(!isOrderPaid()){
                		mProgressDialog.setMessage("开始支付, 请稍等...");
                	}else{
                		 mHandler.sendEmptyMessage(DOUBLEPAID);
                	}
                    
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
                	setSumbitButtonAttribute(true, R.string.bluetooth_pos_resign);
                    break;
                case BizConstant.BIZ_CONSUME:
                    if (BizConstant.BIZ_ERROR_NO_PAPER == errorType) {
                        ToastUtil.show(getActivity(), R.string.payment_pos_error_no_paper_info);
                        setSumbitButtonAttribute(true, R.string.bluetooth_pos_reprint);
                    } else if (BizConstant.BIZ_ERROR_NEED_SIGNIN == errorType) {
                        mController.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
                        mController.signIn();
                    } else {
                        ToastUtil.show(getActivity(), R.string.payment_pos_error_info);
                        setSumbitButtonAttribute(true, R.string.bluetooth_pos_submit);
                    }
                    break;
                case BizConstant.BIZ_CONSUME_REVERSE:
                	ToastUtil.show(getActivity(), "交易冲正");
                	break;
                case BizConstant.BIZ_CANCEL_CONSUME:
                	ToastUtil.show(getActivity(), "消费撤销");
                	break;
                case BizConstant.BIZ_PRINT:
                	if(errorType == BizConstant.BIZ_ERROR_NO_PAPER ){
                		ToastUtil.show(getActivity(), "打印购物小票失败，请重新打印");
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
//            String previousBlueToothDeviceMac = Utils.Preference.getStringPref(getActivity(),Constants.Prefence.PREF_CONNECTED_BLUETOOTH_DEVICE,"");
//            IController controller = Me31Controller.getInstance();
//            controller.connect(getActivity(), previousBlueToothDeviceMac, mBluetoothConnectCallback);
          
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
        	if(!isOrderPaid()){
        		mProgressDialog.setMessage("请刷卡...");
        	}else{
        		 mHandler.sendEmptyMessage(DOUBLEPAID);
        	}
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
			//mProgressDialog.setMessage("数据发送中...");
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
    
    private void setSumbitButtonAttribute(boolean isClick, int resId) {
        mSubmitBtn.setEnabled(isClick);
        mSubmitBtn.setText(resId);
    }

    @Override
    public void onDestroyView() {
        LogUtil.i(TAG, "onDestroyView");
        super.onDestroyView();
        if (getActivity() != null) {
            mController.disConnect(getActivity());
        }
    }

    
    private void savePosHistoryList(ArrayList<PosHistory> posHistory) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(posHistory);
		} catch (IOException e) {
			e.printStackTrace();
		}

		SharedPreferences mySharedPreferences = getActivity().getSharedPreferences(Constants.posHistoryCache, Activity.MODE_PRIVATE);
		// 将Product对象转换成byte数组，并将其进行base64编码
		String productBase64 = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		// 将编码后的字符串写到base64.xml文件中
		editor.putString("posHistoryCache", productBase64);
		editor.commit();
	}
	
	private ArrayList<PosHistory> getPosHistoryList(){
		ArrayList<PosHistory> posHistoryListObject = null;
		Map<String, PosHistory> mProductInfoList = new HashMap<String, PosHistory>();
		SharedPreferences pmySharedPreferences = getActivity().getSharedPreferences(Constants.posHistoryCache, Activity.MODE_PRIVATE);
		if(pmySharedPreferences != null){
			String posHistoryList = null;
			if(pmySharedPreferences != null && pmySharedPreferences.getAll() != null && pmySharedPreferences.getAll().size() > 0){
				posHistoryList = pmySharedPreferences.getString("posHistoryCache", null);
			}
			if(posHistoryList != null){
					String pproductBase64 = posHistoryList;
					if (pproductBase64 != null && pproductBase64.length() > 0) {
						// 对Base64格式的字符串进行解码
						byte[] base64Bytes = Base64.decode(pproductBase64.getBytes(), Base64.DEFAULT);
						ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
						ObjectInputStream ois = null;
						try {
							ois = new ObjectInputStream(bais);
						} catch (StreamCorruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						// 从ObjectInputStream中读取Product对象
						try {
							posHistoryListObject = (ArrayList<PosHistory>) ois.readObject();
						} catch (OptionalDataException e1) {
							e1.printStackTrace();
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
			}
		}
		return posHistoryListObject;
	}
   

}
