
package com.xiaomi.xms.sales.ui;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.activity.SavePrinterActivity;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.loader.PackedLoader;
import com.xiaomi.xms.sales.loader.PayCheckLoader;
import com.xiaomi.xms.sales.loader.PrinterSaveLoader;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.PrinterService;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class PaymentCashFragment extends BaseFragment {
    private static final String TAG = "PaymentCashFragment";
    private static final int ORDER_INFO_LOADER = 0;
    private static final int PACKED_INFO_LOADER = 1;
    private static final int GET_ORDER_INFO = 1001;
    private static final int PAY_CHECK = 1000;
    private static final int SAVE_PRINTER = 100;
    private OrderInfoLoader mLoader;
    private TextView mOrderId;
    private TextView mOrderTime;
    private TextView mcashFee;
    private Button mSubmitBtn;
    private String mFee;
    private String mOrderIdStr;
    private int orderStatus;
    private int orderType;
    private String printerIP;
    private ProgressDialog mProgressDialog;
    private EmptyLoadingView mLoadingView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_cash_fragment, container, false);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mOrderId = (TextView) view.findViewById(R.id.payment_order_id);
        mOrderTime = (TextView) view.findViewById(R.id.payment_order_add_time);
        mcashFee = (TextView) view.findViewById(R.id.payment_cash_fee);
        mSubmitBtn = (Button) view.findViewById(R.id.payment_cash_submit);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        handleIntent();
        
        return view;
    }

    
    private void handleIntent() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFee = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_TOTAL_PRICE);
            mOrderIdStr = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            orderType = bundle.getInt(Constants.Intent.EXTRA_ORDER_TYPE);
            printerIP = bundle.getString(Constants.Intent.EXTRA_PRINTER_IP);
            mOrderId.setText(mOrderIdStr);
            mcashFee.setText("¥ " + mFee);
            mOrderTime.setText(bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ADD_TIME));
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSubmitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	    payAction();
                }
            
        });
        

    }

    /**
     * 支付验证并处理后续流程
     */
    public void payAction(){
    	getLoaderManager().restartLoader(PAY_CHECK,null, new LoaderCallbacks<PayCheckLoader.Result>() {
    		@SuppressWarnings("unchecked")
    	    @Override
    	    public Loader onCreateLoader(int id, Bundle bundle) {
    	        if (id == PAY_CHECK ) {
    	        	PayCheckLoader mLoader = new PayCheckLoader(getActivity(),mOrderIdStr);
    	        	mLoader.setNeedDatabase(false);
    	            return (Loader<PayCheckLoader.Result>) mLoader;
    	        }
    	        return null;
    	    }

    	    @Override
    	    public void onLoadFinished(Loader<PayCheckLoader.Result> loader,
    	    		PayCheckLoader.Result data) {
    	        if(data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")){
    	        	mProgressDialog.setMessage("正在处理支付, 请稍等...");
                    mProgressDialog.show();
                	//验证状态是否为已支付
                	getLoaderManager().restartLoader(ORDER_INFO_LOADER, null, new LoaderCallbacks<OrderInfoLoader.Result>() {
                        @Override
                        public Loader onCreateLoader(int id, Bundle arg1) {
                            if (id == ORDER_INFO_LOADER) {
                                mLoader = new OrderInfoLoader(getActivity());
                                mLoader.setNeedSecurityKeyTask(false);
                                mLoader.setNeedDatabase(false);
                                mLoader.setOrderId(mOrderIdStr);
                               // mLoader.setProgressNotifiable(mLoadingView);
                                return mLoader;
                            }
                            return null;
                        }

                        @Override
                        public void onLoadFinished(Loader<OrderInfoLoader.Result> loader,
                                OrderInfoLoader.Result data) {
                            LogUtil.d(TAG, "info loaded.");
                            if (data != null && data.mOrderInfo != null) {
                                orderStatus = data.mOrderInfo.getOrderStatus();
                                if(orderStatus == 4){   //验证状态是否为已支付
                                	ToastUtil.show(getActivity(), "现金支付完成");
                                	if(orderType == 2){   //现货销售的才会自动配货出库等
                                		//配货出库妥投/打单
                                		mProgressDialog.dismiss();
                                		mProgressDialog.setMessage("支付成功，正在配货出库, 请稍等...");
                                        mProgressDialog.show();
                                    	getLoaderManager().restartLoader(PACKED_INFO_LOADER, null, new LoaderCallbacks<PackedLoader.Result>() {
                                            @Override
                                            public Loader onCreateLoader(int id, Bundle arg1) {
                                                if (id == PACKED_INFO_LOADER) {
                                                    PackedLoader packedLoader = new PackedLoader(getActivity(),mOrderIdStr);
                                                	packedLoader.setNeedDatabase(false);
                                                    return packedLoader;
                                                }
                                                return null;
                                            }

                                            @Override
                                            public void onLoadFinished(Loader<PackedLoader.Result> loader,
                                            		PackedLoader.Result data) {
                                                LogUtil.d(TAG, "info loaded.");
                                                if (data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")) {
                                                    ToastUtil.show(getActivity(), "配货完成");
                                                
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
                                                }
                                                else{
                                                	 Intent intent = new Intent(getActivity(), OrderListActivity.class);
                                                     intent.setAction(Constants.Intent.ACTION_VIEW_ORDER);
                                                     intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                                                     intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderIdStr);
                                                    
                                                     startActivity(intent);
                                                     
                                                     if (getActivity() != null) {
                                                         getActivity().finish();
                                                     }
                                                	
                                                }
                                            }

                                            @Override
                                            public void onLoaderReset(Loader<PackedLoader.Result> loader) {
                                            }
                                        });
                                    	mProgressDialog.dismiss();
                                	}
                                	else{
                                		mProgressDialog.dismiss();
                                		Intent intent = new Intent(getActivity(), OrderEditActivity.class);
                                        intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
                                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderIdStr);
                                        intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                                        intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION, "ADD");
                                        startActivity(intent);
                                        if (getActivity() != null) {
                                            getActivity().finish();
                                        }
                                	}
                                }
                                else{
                                	mProgressDialog.dismiss();
                                	ToastUtil.show(getActivity(), "还未支付，请先到收银台支付！");
                                }
                               
                            }
                        }

                        @Override
                        public void onLoaderReset(Loader<OrderInfoLoader.Result> loader) {
                        }
                    });
    	        }
    	        else{
    	        	ToastUtil.show(getActivity(), data !=null && data.responseInfo != null && data.responseInfo.length() > 0 ? 
    	        			data.responseInfo :"IP不合法或支付信息有误！");
    	        }
    	    }

			@Override
			public void onLoaderReset(Loader<PayCheckLoader.Result> arg0) {
				
			}

    	});
    }
    
    private void setSumbitButtonAttribute(boolean isClick, int resId) {
        mSubmitBtn.setEnabled(isClick);
        mSubmitBtn.setText(resId);
    }

    @Override
    public void onDestroyView() {
        LogUtil.i(TAG, "onDestroyView");
        super.onDestroyView();
    };
}
