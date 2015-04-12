
package com.xiaomi.xms.sales.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.activity.OrderListActivity;
import com.xiaomi.xms.sales.activity.PaymentActivity;
import com.xiaomi.xms.sales.activity.SavePrinterActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity.Fragments;
import com.xiaomi.xms.sales.adapter.OrderViewProductAdapter;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.loader.PackedLoader;
import com.xiaomi.xms.sales.loader.PosPayLoader;
import com.xiaomi.xms.sales.loader.PrinterSaveLoader;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.PosHistory;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.PrinterService;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class OrderViewFragment extends BaseFragment {

    private static final String TAG = "OrderViewFragment";
    private static final int ORDER_INFO_LOADER = 0;
    private static final int PACKED_INFO_LOADER = 1;
    private static final int GET_ORDER_INFO = 1001;
    private static final int SAVE_PRINTER = 100;
    private static final int POS_PAY_LOADER = 2;
    private TextView mOrderIdText;
    private TextView mOrderStatusText;
    private TextView mOrderFeeText;
    private TextView mOrderFeeSubtotalText;
    private TextView mOrderFeeTextBottom;
    private TextView mOrderInvoiceText;
    private TextView mOrderAddTime;
    private View mActionContainer;
    private Button mPayButton;
    private ImageView mCancelImage;
    private BaseListView mListView;
    private View mHeaderView;
    private View mHeaderContentView;
    private View mExpressView;
    private View mListFooterView;

    private View mPaymentInfoView;
    private TextView mPayTimeText;
    private TextView mPayNoText;
    private View mUserInfoView;
    private TextView mUserNameText;
    private TextView mUserTelText;
    private TextView mUserEmailText;

    private OrderInfoLoader mLoader;
    private OrderViewProductAdapter mAdapter;
    private EmptyLoadingView mLoadingView;

    private String mOrderId;
    private String printerIP;
    private String mOrderFee;
    private int orderType;
    private int payId;
    private TextAppearanceSpan mHighlightTextAppearanceSpan;
    private OnOrderStatusChangedListener mOrderStatusListener;

    private Spinner doSpinner;
    private String doType;
    private Spinner addInfoSpinner;
    private String addInfoType;
    private boolean onlyView;
    private PosHistory posHistoryInfo;
    public interface OnOrderStatusChangedListener {
        public void onOrderStatusChanged();
    }

    public void setOrderStatusListener(OnOrderStatusChangedListener listener) {
        mOrderStatusListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_view_fragment, container, false);
        
        doSpinner = (Spinner)view.findViewById(R.id.do_type);
        doSpinner.setVisibility(View.GONE);
       
        addInfoSpinner = (Spinner)view.findViewById(R.id.add_info);
        addInfoSpinner.setVisibility(View.GONE);
        
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mHeaderView = inflater.inflate(R.layout.order_view_header, null, false);
        mHeaderView.setVisibility(View.GONE);
        mHeaderContentView = inflater.inflate(R.layout.order_view_header_content, null, false);
        mHeaderContentView.setVisibility(View.GONE);
        mListFooterView = inflater.inflate(R.layout.order_view_footer, null, false);
        mListFooterView.setVisibility(View.GONE);
        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addHeaderView(mHeaderContentView, null, false);
        mListView.addFooterView(mListFooterView);
        mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding),0,
                getResources().getDimensionPixelSize(R.dimen.list_item_padding),0);

        mOrderIdText = (TextView) view.findViewById(R.id.order_id);
        mOrderStatusText = (TextView) view.findViewById(R.id.order_status);
        mOrderFeeText = (TextView) view.findViewById(R.id.order_fee);
        mOrderFeeSubtotalText = (TextView) mHeaderView.findViewById(R.id.order_fee_subtotal);
        mOrderFeeTextBottom = (TextView) view.findViewById(R.id.order_fee_bottom);
        mExpressView = view.findViewById(R.id.order_express_info);
        mOrderAddTime = (TextView) view.findViewById(R.id.add_time);
        mOrderInvoiceText = (TextView) view.findViewById(R.id.invoice_info);
        mPayButton = (Button) view.findViewById(R.id.pay_btn);
        mCancelImage = (ImageView) view.findViewById(R.id.cancel_btn);
        mActionContainer = view.findViewById(R.id.action_container);
        mHighlightTextAppearanceSpan = new TextAppearanceSpan(getActivity(),R.style.TextAppearance_Notice_Medium);

        mPaymentInfoView = mHeaderContentView.findViewById(R.id.payment_info);
        mPayTimeText = (TextView) mHeaderContentView.findViewById(R.id.payment_info_time);
        mPayNoText = (TextView) mHeaderContentView.findViewById(R.id.payment_info_refno);
        mUserInfoView = mHeaderContentView.findViewById(R.id.user_info);
        mUserNameText = (TextView) mHeaderContentView.findViewById(R.id.receive_user_name);
        mUserTelText = (TextView) mHeaderContentView.findViewById(R.id.receive_user_tel);
        mUserEmailText = (TextView) mHeaderContentView.findViewById(R.id.receive_user_email);

        handleIntent();
        getActivity().setTitle(R.string.order_view_info);
        return view;
    }

    private void handleIntent() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            printerIP = bundle.getString(Constants.Intent.EXTRA_PRINTER_IP);
            onlyView = bundle.getBoolean(Constants.Intent.EXTRA_ONLY_VIEW);
        }
        LogUtil.d(TAG, mOrderId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);
        mAdapter = new OrderViewProductAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        
        mPayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.equals(mPayButton.getText(), getString(R.string.order_pay))) {
                    Intent intent = new Intent(getActivity(), PaymentActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                    intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_TOTAL_PRICE, mOrderFee);
                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ADD_TIME, mOrderAddTime.getText().toString());
                    OrderListActivity parent = (OrderListActivity) getActivity();
                    if (mOrderStatusListener != null) {
                        mOrderStatusListener.onOrderStatusChanged();
                    }
                    parent.startActivity(intent);
                    mLoader.reset();
                } else if (TextUtils.equals(mPayButton.getText(), getString(R.string.order_view_fee_refund))) {
                    showLogoutDialog();
                } else if(TextUtils.equals(mPayButton.getText(), getString(R.string.order_paid_not_asyn_yet))){
                	if(posHistoryInfo == null){
                		ToastUtil.show(getActivity(), "此订单未支付，属于异常单，请联系开发人员");
                	}else{
                		setSumbitButtonAttribute(false, R.string.order_asyning);
                		asynPosPaidInfo(posHistoryInfo.getmOrderId(),posHistoryInfo.getInfo());//已支付，但未同步后台信息
                	}
                	
                }
                else if(TextUtils.equals(mPayButton.getText(), getString(R.string.order_edit_submit))){
                	doType = (String)doSpinner.getSelectedItem();
                	if(doType != null && doType.equals(getString(R.string.packed_redo))){
                		//配货出库妥投/打单
                    	getLoaderManager().restartLoader(PACKED_INFO_LOADER, null, new LoaderCallbacks<PackedLoader.Result>() {
                            @Override
                            public Loader onCreateLoader(int id, Bundle arg1) {
                                if (id == PACKED_INFO_LOADER) {
                                    PackedLoader packedLoader = new PackedLoader(getActivity(),mOrderId);
                                	packedLoader.setNeedDatabase(false);
                                    return packedLoader;
                                }
                                return null;
                            }

                            @Override
                            public void onLoadFinished(Loader<PackedLoader.Result> loader,PackedLoader.Result data) {
                                LogUtil.d(TAG, "info loaded.");
                                if (data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")) {
                                    ToastUtil.show(getActivity(), "配货完成");
                                    if (mOrderStatusListener != null) {
                                        mOrderStatusListener.onOrderStatusChanged();
                                    }
                               
//                                    Intent intent = new Intent(getActivity(),OrderEditActivity.class);
//                                    intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
//                                    intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID,mOrderId);
//                                    intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE,orderType);
//                                    intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION,"ADD");
//                                    intent.putExtra(Constants.Intent.EXTRA_PRINTER_IP, printerIP);
//                                    startActivity(intent);
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }
                                else{
                                	ToastUtil.show(getActivity(), "配货失败，请确认订单信息或申请退款");
                                }
                            }

                            @Override
                            public void onLoaderReset(Loader<PackedLoader.Result> loader) {
                            }
                        });
                	}
                	else if(doType != null && doType.equals(getString(R.string.order_view_fee_refund))){
                		 showLogoutDialog();
                	}
                }
                else if(TextUtils.equals(mPayButton.getText(), getString(R.string.add_info_submit))){
                	addInfoType = (String)addInfoSpinner.getSelectedItem();
                	if(addInfoType != null && addInfoType.equals(getString(R.string.order_printer))){
                		Intent intent = new Intent(getActivity(), SavePrinterActivity.class);
                        intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                        intent.putExtra(Constants.Intent.EXTRA_PRINTER_REDO, true);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                	}
                	else if(addInfoType != null && addInfoType.equals(getString(R.string.add_info_btn))){
                		 Intent intent = new Intent(getActivity(), OrderEditActivity.class);
                         intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
                         intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                         intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                         intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION, "ADD");
                         startActivity(intent);
                         if (getActivity() != null) {
                             getActivity().finish();
                         }
                	}
                }
            }
        });
        mCancelImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
                dialog.setMessage(R.string.order_ask_cancel);
                dialog.setNegativeButton(R.string.dialog_ask_ok,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCancelImage.setEnabled(false);
                                mPayButton.setEnabled(false);
                                Intent intent = new Intent(getActivity(), ShopIntentService.class);
                                intent.setAction(Constants.Intent.ACTION_CANCEL_ORDER);
                                intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                                intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                                getActivity().startService(intent);
                            }
                        });
                dialog.setPositiveButton(R.string.dialog_ask_cancel, null);
                dialog.show();
            }
        });
        
        doSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
    }

    public void onServiceCompleted(String action, Intent callbackIntent) {
        if (TextUtils.equals(Constants.Intent.ACTION_CANCEL_ORDER, action)) {
            mCancelImage.setEnabled(true);
            mPayButton.setEnabled(true);
            if (callbackIntent.getBooleanExtra(Constants.Intent.EXTRA_RESULT, false)) {
                ToastUtil.show(getActivity(), R.string.order_cancel);
                OrderListActivity parent = (OrderListActivity) getActivity();
                if (mOrderStatusListener != null) {
                    mOrderStatusListener.onOrderStatusChanged();
                }
                parent.onBackPressed();
            } else {
                ToastUtil.show(getActivity(), R.string.order_cancel_err);
            }
        } else if (TextUtils.equals(Constants.Intent.ACTION_ORDER_REFUND, action)) {
            if (callbackIntent.getBooleanExtra(Constants.Intent.EXTRA_RESULT, false)) {
                ToastUtil.show(getActivity(), R.string.order_refund);
                OrderListActivity parent = (OrderListActivity) getActivity();
                if (mOrderStatusListener != null) {
                    mOrderStatusListener.onOrderStatusChanged();
                }
                parent.onBackPressed();
            } else {
                ToastUtil.show(getActivity(), R.string.order_refund_err);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoader.reload();
    }
    
    private void setSumbitButtonAttribute(boolean isClick, int resId) {
    	mPayButton.setEnabled(isClick);
    	mPayButton.setText(resId);
    }

    private LoaderCallbacks<OrderInfoLoader.Result> mOrderInfoCallback = new LoaderCallbacks<OrderInfoLoader.Result>() {
        @Override
        public Loader onCreateLoader(int id, Bundle arg1) {
            if (id == ORDER_INFO_LOADER) {
                mLoader = new OrderInfoLoader(getActivity());
                mLoader.setNeedSecurityKeyTask(false);
                mLoader.setNeedDatabase(false);
                mLoader.setOrderId(mOrderId);
                mLoader.setProgressNotifiable(mLoadingView);
                return mLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<OrderInfoLoader.Result> loader,
                OrderInfoLoader.Result data) {
            LogUtil.d(TAG, "info loaded.");
            if (data != null && data.mOrderInfo != null) {
            	orderType = data.mOrderInfo.getOrderType();
            	payId = data.mOrderInfo.getPayid();
                mHeaderView.setVisibility(View.VISIBLE);
                mHeaderContentView.setVisibility(View.VISIBLE);
                mListFooterView.setVisibility(View.VISIBLE);
                mCancelImage.setVisibility(View.GONE);
                Activity activity = getActivity();
                // product list
                mAdapter.updateData(data.mOrderInfo.getProductList());
                // order id
                mOrderIdText.setText(data.mOrderInfo.getOrderId());
                // invoice info
                mOrderInvoiceText.setText(data.mOrderInfo.getInvoiceInfo());
                // add time
                if(data.mOrderInfo.getAddTime() != null && data.mOrderInfo.getAddTime().length() > 0){
                	 mOrderAddTime.setText(Utils.DateTime.formatTime(getActivity(),
                             String.valueOf(Long.parseLong(data.mOrderInfo.getAddTime()) / 1000)));
                }

                if (data.mOrderInfo.isMihomeBuy()) {
                    mExpressView.setVisibility(View.GONE);
                }

                // order status
                SpannableStringBuilder orderStatusSB = new SpannableStringBuilder(
                        activity.getString(R.string.order_view_status,data.mOrderInfo.getOrderStatusInfo()));

                mOrderFee = Utils.Money.valueOf(data.mOrderInfo.getFee());
                SpannableStringBuilder orderFeeSB = new SpannableStringBuilder(activity.getString(
                        R.string.order_view_fee, mOrderFee));

                if (data.mOrderInfo.getOrderStatus() == 1) {
                    // 待付款
                    mActionContainer.setVisibility(View.VISIBLE);
                    String orderIds = Utils.Preference.getStringPref(ShopApp.getContext(),
                            Constants.Account.PREF_NOTIFY_SERVER_ERROR_ORDERIDS, "");
                    
                    if (!TextUtils.isEmpty(orderIds) && orderIds.contains(mOrderId)) {
                        mPayButton.setEnabled(false);
                    }
                    orderStatusSB.setSpan(mHighlightTextAppearanceSpan,
                            orderStatusSB.length()
                                    - data.mOrderInfo.getOrderStatusInfo().length(),
                            orderStatusSB.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    orderFeeSB.setSpan(mHighlightTextAppearanceSpan, orderFeeSB.length()
                            - mOrderFee.length() - 1,
                            orderFeeSB.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 取消订单
                    mCancelImage.setVisibility(View.VISIBLE);
                    if(paidNotAsynYet(mOrderId)){
                    	mPayButton.setText(R.string.order_paid_not_asyn_yet);
                    }else{
                    	mPayButton.setText(R.string.order_pay);
                    }
                    
                } else if (data.mOrderInfo.getOrderStatus() != 18) {
                    // 已付款
                    mPaymentInfoView.setVisibility(View.VISIBLE);
                    mUserInfoView.setVisibility(View.VISIBLE);
                    if(data.mOrderInfo.getOrderPayTime() != null && data.mOrderInfo.getOrderPayTime().length() > 0){
                    	 mPayTimeText.setText(Utils.DateTime.formatTime(getActivity(),
                                 String.valueOf(Long.parseLong(data.mOrderInfo.getOrderPayTime()) / 1000)));
                    }
                   
                    mPayNoText.setText(data.mOrderInfo.getOrderPayNo());
                    if (data.mOrderInfo.getOrderStatus() == 4) {
                    	if(data.mOrderInfo.getOrderType() == 2){  //现货销售的订单，状态还是4就是配货失败的，能再次配货和退款
                    		ToastUtil.show(getActivity(), "配货失败或未配货，请再次配货或申请退款");
                    		mActionContainer.setVisibility(View.VISIBLE);
                            mOrderFeeTextBottom.setVisibility(View.GONE);
                            mPayButton.setText(R.string.order_edit_submit);  //提交按钮
                            
                            doSpinner.setVisibility(View.VISIBLE);
                            doType = (String)doSpinner.getSelectedItem();
                    	}
                    	else{
                    		// 退款
                            mActionContainer.setVisibility(View.VISIBLE);
                            orderFeeSB = new SpannableStringBuilder(activity.getString(
                                    R.string.order_view_fee_paied, mOrderFee));
                            mPayButton.setText(R.string.order_view_fee_refund);
                    	}
                        
                    }
                    else {
                        mActionContainer.setVisibility(View.GONE);
                        if(onlyView == false && data.mOrderInfo.getOrderType() == 2 && data.mOrderInfo.getOrderStatus() == 8){  //修改订单时是妥投的现货销售订单就跳入填写个人信息
                        	Intent intent = new Intent(getActivity(), OrderEditActivity.class);
                            intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
                            intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                            intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                            intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION, "ADD");
                            startActivity(intent);
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }
                        if(onlyView && data.mOrderInfo.getOrderType() == 2 && data.mOrderInfo.getOrderStatus() == 8){  //查看订单时是妥投的现货销售订单就显示选择打印机和填写用户信息按钮
                        	mActionContainer.setVisibility(View.VISIBLE);
                            mOrderFeeTextBottom.setVisibility(View.GONE);
                            mPayButton.setText(R.string.add_info_submit);  //提交按钮
                            
                            addInfoSpinner.setVisibility(View.VISIBLE);
                            addInfoType = (String)addInfoSpinner.getSelectedItem();
                        }
                    }
                }
                mPayButton.setEnabled(true);
                mUserInfoView.setVisibility(View.VISIBLE);
                mUserNameText.setText(data.mOrderInfo.getOrderUserName());
                mUserTelText.setText(data.mOrderInfo.getOrderUserTel());
                mUserEmailText.setText(data.mOrderInfo.getOrderUserEmail());
                mOrderFeeText.setText(orderFeeSB);
                mOrderFeeSubtotalText.setText(getString(R.string.order_fee_subtotal_text,data.mOrderInfo.getFee()));
                mOrderFeeTextBottom.setText(orderFeeSB);
                mOrderStatusText.setText(orderStatusSB);
            } else {
                if (TextUtils.isEmpty(data.mOrderError)) {
                    mLoadingView.setEmptyText(R.string.order_err);
                } else {
                    mLoadingView.setEmptyText(data.mOrderError);
                }
            }
        }

       

		@Override
        public void onLoaderReset(Loader<OrderInfoLoader.Result> loader) {
        }
    };

    private void showLogoutDialog() {
       /* final BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
        dialog.setTitle(R.string.refund_title);
        dialog.setMessage(getResources().getString(R.string.refund_summary,
                LoginManager.getInstance().getSystemAccountId()));
        dialog.setPositiveButton(R.string.dialog_ask_cancel, new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_ask_ok, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShopIntentService.class);
                intent.setAction(Constants.Intent.ACTION_ORDER_REFUND);
                intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderId);
                getActivity().startService(intent);
            }
        });
        dialog.show();
        */
    	ToastUtil.show(getActivity(), "目前只支持妥投后退款，请走妥投后退货流程");
    }
    
    
    private boolean paidNotAsynYet(String orderId) {
        ArrayList<PosHistory> posHistoryList = getPosHistoryList();
        if(posHistoryList == null){
        	return false;
        }
        for(PosHistory posHistory : posHistoryList){
        	if(posHistory.getmOrderId().equals(orderId)){
        		posHistoryInfo = posHistory;
        		return true;
        	}
        }
        return false;
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
    
    private void asynPosPaidInfo(final String mOrderIdStr, final String info){
    	
    	 getLoaderManager().restartLoader(POS_PAY_LOADER, null, new LoaderCallbacks<PosPayLoader.Result>() {
         	final String serviceNumber = mOrderIdStr;
         	final int ordertype = orderType;
         	final String printerip = printerIP;
         	final EmptyLoadingView loadingView = mLoadingView;
				@Override
				public PosPayLoader onCreateLoader(int id,Bundle arg1) {
					if(id == POS_PAY_LOADER){
					
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
						 if (ordertype == 2) { // 现货销售的才会自动配货出库等
	                            getLoaderManager().restartLoader(PACKED_INFO_LOADER, null,new LoaderCallbacks<PackedLoader.Result>() {
                                 @Override
                                 public PackedLoader onCreateLoader(int id, Bundle arg1) {
                                     if (id == PACKED_INFO_LOADER) {
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
                                         ToastUtil.show(getActivity(), "配货完成");
                                         getLoaderManager().restartLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);
                                     }
                                     else {
                                    	getLoaderManager().restartLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);
                                     	
                                     }
                                 }

                                 @Override
                                 public void onLoaderReset(Loader<PackedLoader.Result> loader) {
                                 }
                             });
	                        }
					  
					}
					else{
						ToastUtil.show(getActivity(),data.responseInfo);
//						setSumbitButtonAttribute(true, R.string.order_paid_not_asyn_yet);
						getLoaderManager().restartLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);
					}
					
				}

				@Override
				public void onLoaderReset(Loader<PosPayLoader.Result> arg0) {
					// TODO Auto-generated method stub
					
				}
			});
    }
}
