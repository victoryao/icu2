package com.xiaomi.xms.sales.ui;

import java.io.IOException;
import java.util.ArrayList;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.OrderEditActivity;
import com.xiaomi.xms.sales.adapter.PrinterSpinnerAdapter;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader;
import com.xiaomi.xms.sales.loader.PrinterSaveLoader;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader.PrinterMode;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogcatHelper;
import com.xiaomi.xms.sales.util.PrinterService;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class SavePrinterFragment extends BaseFragment {

	private Bundle mBundle;
    private int printerId;
    private Spinner mPrinterSpinner;
    private PrinterSpinnerAdapter mPrinterAdapter;
    private ArrayList<PrinterMode> mPrinterList;
    private int orderType;
    private TextView orderPrinter;
    private TextView orderId;
    private Button mSave;
    private String mOrderIdStr;
    private boolean printerRedo;
    public static final int GET_PRINTER_LIST = 2;
    private static final int GET_ORDER_INFO = 1001;
    private static final int SAVE_PRINTER = 100;
    private EmptyLoadingView mLoadingView;
    private String ip;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.save_printer_fragment,container, false);
		mSave = (Button) view.findViewById(R.id.save_btn);
        orderPrinter = (TextView) view.findViewById(R.id.order_printer);
        orderId = (TextView) view.findViewById(R.id.order_id);
        mPrinterSpinner = (Spinner) view.findViewById(R.id.printer);
        mPrinterAdapter = new PrinterSpinnerAdapter(getActivity());
        mPrinterSpinner.setAdapter(mPrinterAdapter);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
	    return view;
	}

    
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBundle = getArguments();
        if (mBundle != null) {
        	mOrderIdStr = mBundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            orderType = mBundle.getInt(Constants.Intent.EXTRA_ORDER_TYPE);
            printerRedo = mBundle.getBoolean(Constants.Intent.EXTRA_PRINTER_REDO);
            orderId.setText("订单号："+mOrderIdStr);
        } else {
        	if(getActivity() != null ){
            	getActivity().finish();
            }
        }
        mPrinterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
       
        if(mPrinterList == null){
        	mPrinterList = new ArrayList<PrinterMode>();
        }
        
        if(orderType == 2){   //现货销售的订单才显示选择打印机
        	getPrinterList();
        }
		
       
        mSave.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				PrinterMode printerMode = (PrinterMode)mPrinterSpinner.getSelectedItem();
			    printerId = printerMode.mModeKey;
			    ip = printerMode.mIpAdress;
			  /*
				getLoaderManager().restartLoader(SAVE_PRINTER, null,
        				new LoaderCallbacks<PrinterSaveLoader.Result>() {
        					@Override
        					public Loader<com.xiaomi.xms.sales.loader.PrinterSaveLoader.Result> onCreateLoader(
        							int id, Bundle arg1) {
        						if (id == SAVE_PRINTER) {
        							PrinterSaveLoader  mLoader = new PrinterSaveLoader(getActivity(), printerId,mOrderIdStr);
        							mLoader.setNeedDatabase(false);
        							mLoader.setProgressNotifiable(mLoadingView);
        							return (Loader<PrinterSaveLoader.Result>) mLoader;
        						}
        						return null;
        					}

        					@Override
        					public void onLoadFinished(
        							Loader<com.xiaomi.xms.sales.loader.PrinterSaveLoader.Result> arg0,
        							com.xiaomi.xms.sales.loader.PrinterSaveLoader.Result data) {
        						if(data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")) {
        							ToastUtil.show(getActivity(), "发送打印请求成功");
        							if(printerRedo == false){
        								Intent intent = new Intent(getActivity(), OrderEditActivity.class);
                                        intent.setAction(Constants.Intent.ACTION_ORDER_EDIT);
                                        intent.putExtra(Constants.Intent.EXTRA_PAYMENT_ORDER_ID, mOrderIdStr);
                                        intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, orderType);
                                        intent.putExtra(Constants.Intent.EXTRA_ORDER_EDIT_ACTION, "ADD");
                                        startActivity(intent);
                                        if(getActivity() != null ){
                                        	getActivity().finish();
                                        }
        							}
        						}
        						else{
                                	ToastUtil.show(getActivity(), "发送打印请求失败，请再次选择打印机...");
                                	
        						}
        					}

        					@Override
        					public void onLoaderReset(
        							Loader<com.xiaomi.xms.sales.loader.PrinterSaveLoader.Result> arg0) {
        					}
        				});
				*/
				   getLoaderManager().restartLoader(GET_ORDER_INFO, null, new LoaderCallbacks<OrderInfoLoader.Result>() {
           	        @Override
           	        public OrderInfoLoader onCreateLoader(int id, Bundle arg1) {
           	            if (id == GET_ORDER_INFO) {
           	            	OrderInfoLoader mLoader = new OrderInfoLoader(getActivity());
           	                mLoader.setNeedSecurityKeyTask(false);
           	                mLoader.setNeedDatabase(false);
           	                mLoader.setOrderId(mOrderIdStr);
           	                mLoader.setProgressNotifiable(mLoadingView);
           	                return mLoader;
           	            }
           	            return null;
           	        }

           	        @Override
           	        public void onLoadFinished(Loader<OrderInfoLoader.Result> loader,
           	                OrderInfoLoader.Result data) {
           	            if (data != null && data.mOrderInfo != null) {
           	            	System.out.println("ip:"+ip);
           	            	final String PRINT_IP = ip;
           	            	final Order order = data.mOrderInfo;
           	            	ToastUtil.show(getActivity(), "开始打印购物清单！");
       	            		new Thread(new Runnable() {
									@Override
									public void run() {
										PrinterService printerService;
										try {
											printerService = new PrinterService(PRINT_IP);
											printerService.print(order,getActivity());
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
       	            		}).start();
           	            }
           	        }

           	        @Override
           	        public void onLoaderReset(Loader<OrderInfoLoader.Result> loader) {
           	        }
                   });
			}
		});
	}

	/**
     * 获取打印机信息
     */
   public void getPrinterList(){
	   getLoaderManager().initLoader(GET_PRINTER_LIST, null,new LoaderCallbacks<PrinterInfoLoader.Result>() {
			@Override
			public Loader<com.xiaomi.xms.sales.loader.PrinterInfoLoader.Result> onCreateLoader(
					int id, Bundle arg1) {
				if (id == GET_PRINTER_LIST) {
					PrinterInfoLoader mLoader = new PrinterInfoLoader(getActivity(), Constants.PRINTER);
					mLoader.setNeedDatabase(false);
					mLoader.setProgressNotifiable(mLoadingView);
					return (Loader<PrinterInfoLoader.Result>) mLoader;
				}
				return null;
			}

			@Override
			public void onLoadFinished(
					Loader<com.xiaomi.xms.sales.loader.PrinterInfoLoader.Result> arg0,
					com.xiaomi.xms.sales.loader.PrinterInfoLoader.Result data) {
				if(data != null && data.p != null && data.p.size() > 0){
					mPrinterList = data.p;
					mPrinterAdapter.updateData(mPrinterList);
				}
			}

			@Override
			public void onLoaderReset(
					Loader<com.xiaomi.xms.sales.loader.PrinterInfoLoader.Result> arg0) {
			}
		});
   }
}
