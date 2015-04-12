
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.PaymentActivity;
import com.xiaomi.xms.sales.adapter.PayModeSpinnerAdapter;
import com.xiaomi.xms.sales.adapter.PrinterSpinnerAdapter;
import com.xiaomi.xms.sales.loader.AddPayTypeLoader;
import com.xiaomi.xms.sales.loader.PaymentInfoLoader;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader;
import com.xiaomi.xms.sales.loader.PaymentInfoLoader.PayMode;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader;
import com.xiaomi.xms.sales.loader.PrinterInfoLoader.PrinterMode;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PaymentFragment extends BaseFragment {

    private static final int PAY_MODE_ALIPAY = 1;
    private static final int PAY_MODE_UPPAY = 2;

    public static final int ADD_PAYTYPE = 1;
    public static final int GET_PRINTER_LIST = 2;
    public static int PAY_TYPE = 100;
    
    private Button mPayButton;
    private Spinner mPayTypeSpinner;
    private PayModeSpinnerAdapter mPayTypeAdapter;
    private OrderLayout mOrderLayout;
    private Order mOrder;
    private String mOrderId;
    public ArrayList<PayMode> mPayModeList;
    private Bundle mBundle;
    private String payType;
    private String printerIp;
    private Spinner mPrinterSpinner;
    private PrinterSpinnerAdapter mPrinterAdapter;
    private ArrayList<PrinterMode> mPrinterList;
    private int orderType;
    private TextView orderPrinter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.order_info_fragment, container, false);
        mPayTypeSpinner = (Spinner) v.findViewById(R.id.pay_type);
        mPayTypeAdapter = new PayModeSpinnerAdapter(getActivity());
        mPayTypeSpinner.setAdapter(mPayTypeAdapter);
        
        mPrinterSpinner = (Spinner) v.findViewById(R.id.printer);
        mPrinterAdapter = new PrinterSpinnerAdapter(getActivity());
        mPrinterSpinner.setAdapter(mPrinterAdapter);
        if(mPrinterList == null){
        	mPrinterList = new ArrayList<PrinterMode>();
        }
        orderPrinter = (TextView) v.findViewById(R.id.order_printer);
        
        mPrinterSpinner.setVisibility(View.GONE);
    	orderPrinter.setVisibility(View.GONE);
        
        mPayButton = (Button) v.findViewById(R.id.pay_btn);
        mPayButton.setOnClickListener(mPayButtonClickListener);
        mOrderLayout = (OrderLayout) v.findViewById(R.id.order_table);
        handleIntent();
        if (mPayModeList == null) {
            mPayModeList = new ArrayList<PaymentInfoLoader.PayMode>();
            mPayModeList.add(new PayMode(getActivity().getString(R.string.pay_pos), getActivity().getString(R.string.pay_pos)));
//            mPayModeList.add(new PayMode(getActivity().getString(R.string.pay_cash),getActivity().getString(R.string.pay_cash)));  //暂时不上
        }
        
        return v;
    }

    private void handleIntent() {
        mBundle = getArguments();
        if (mBundle != null) {
            mOrder = new Order();
            mOrderId = mBundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
            orderType = mBundle.getInt(Constants.Intent.EXTRA_ORDER_TYPE);
            double totalPrice = Double.valueOf(mBundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_TOTAL_PRICE));
            mOrder.setOrderId(mOrderId);
            mOrder.setFee(totalPrice);
            mOrder.setOrderType(orderType);
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOrderLayout.fillOrderTable(mOrder);
        mPayTypeAdapter.updateData(mPayModeList);
        mPayTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        mPrinterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
        
        if(orderType == 2){   //现货销售的订单才显示选择打印机
        	mPrinterSpinner.setVisibility(View.VISIBLE);
        	orderPrinter.setVisibility(View.VISIBLE);
        	getPrinterList();
        }
         
    }

    /**
     * 获取打印机信息
     */
   public void getPrinterList(){
	   getLoaderManager().initLoader(GET_PRINTER_LIST, null,
				new LoaderCallbacks<PrinterInfoLoader.Result>() {
					@SuppressWarnings("unchecked")
					@Override
					public Loader<PrinterInfoLoader.Result> onCreateLoader(
							int id, Bundle arg1) {
						if (id == GET_PRINTER_LIST) {
							mLoader = new PrinterInfoLoader(getActivity(), Constants.PRINTER);
							mLoader.setNeedDatabase(false);
							return (Loader<PrinterInfoLoader.Result>) mLoader;
						}
						return null;
					}

					@Override
					public void onLoadFinished(
							Loader<PrinterInfoLoader.Result> arg0,PrinterInfoLoader.Result data) {
						if(data != null && data.p != null && data.p.size() > 0){
							mPrinterList = data.p;
							mPrinterAdapter.updateData(mPrinterList);
						}
					}

					@Override
					public void onLoaderReset(Loader<PrinterInfoLoader.Result> arg0) {
					}
				});
   }
    
    private OnClickListener mPayButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PaymentActivity father = (PaymentActivity) getActivity();
            PayMode payMode = (PayMode)mPayTypeSpinner.getSelectedItem();
            payType = payMode.mModeValue;
            PrinterMode printerMode = (PrinterMode)mPrinterSpinner.getSelectedItem();
            if(printerMode != null && printerMode.mModeKey >=0 
            		&& printerMode.mIpAdress != null && printerMode.mIpAdress.length() > 0){   //只有现货销售的才会有选择打印机的
            	 printerIp = printerMode.mIpAdress;
                 mBundle.putString(Constants.Intent.EXTRA_PRINTER_IP,printerIp);
                 
            }
            if(payType.equals(getActivity().getString(R.string.pay_pos))){   //POS机支付
                father.showFragment(PaymentActivity.TAG_BLUETOOTH_FRAGMENT, mBundle, false);
            }
            else if(payType.equals(getActivity().getString(R.string.pay_cash))){  //现金支付
            	PAY_TYPE = Constants.CASH_PAY;
            	addPayType();
            	father.showFragment(PaymentActivity.TAG_PAYMENT_CASH_FRAGMENT, mBundle, false);
            }
        }
    };
    
    
    public void addPayType(){
    	getLoaderManager().initLoader(ADD_PAYTYPE, 
    			null, new LoaderCallbacks<AddPayTypeLoader.Result>() {
    		@SuppressWarnings("unchecked")
    	    @Override
    	    public Loader onCreateLoader(int id, Bundle bundle) {
    	        if (id == ADD_PAYTYPE ) {
    	        	AddPayTypeLoader mLoader = new AddPayTypeLoader(getActivity(), "payType", mOrderId, PAY_TYPE);
    	        	mLoader.setNeedDatabase(false);
    	            return (Loader<AddPayTypeLoader.Result>) mLoader;
    	        }
    	        return null;
    	    }

    	    @Override
    	    public void onLoadFinished(Loader<AddPayTypeLoader.Result> loader,
    	    		AddPayTypeLoader.Result data) {
    	        if(data == null || data.responseInfo == null || data.responseInfo.equalsIgnoreCase("error")){
    	        	ToastUtil.show(getActivity(), "添加支付方式失败！");
    	        }
    	    }

			@Override
			public void onLoaderReset(Loader<AddPayTypeLoader.Result> arg0) {
				
			}

    	});
    }

}
