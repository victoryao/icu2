package com.xiaomi.xms.sales.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Base64;
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
import com.xiaomi.xms.sales.activity.SameDayExchangeActivity;
import com.xiaomi.xms.sales.adapter.OrderViewProductAdapter;
import com.xiaomi.xms.sales.loader.ExchangeOrderInfoLoader;
import com.xiaomi.xms.sales.loader.ReturnOrderInfoLoader;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class ExchangeOrderDetailFragment extends Fragment {
	private static final String TAG = "OrderViewFragment";
	private static final int ORDER_INFO_LOADER = 0;
	private static final int REFUND_CONFIRM_LOADER = 1;
	private static final int SAVE_PRINTER = 100;
	private TextView mOrderIdText;
	private TextView mOrderStatusText;
	private TextView mOrderFeeText;
	private TextView mOrderFeeSubtotalText;
	private TextView mOrderInvoiceText;
	private TextView mOrderAddTime;
	private View mActionContainer;
	private View mOperationContainer;
	private Button mPayButton;
	private BaseListView mListView;
	private View mHeaderView;
	private View mHeaderContentView;
	private View mExpressView;
	private View mListFooterView;
	private View mListSpinnerFooterView;
	private View mPaymentInfoView;
	private TextView mPayTimeText;
	private TextView mPayNoText;
	private View mUserInfoView;
	private TextView mUserNameText;
	private TextView mUserTelText;
	private TextView mUserEmailText;

	private ExchangeOrderInfoLoader mLoader;
	private OrderViewProductAdapter mAdapter;
	private EmptyLoadingView mLoadingView;

	private String mOrderId;
	private String mOrderFee;
	private int orderType;
	private String orderStatusType;
	private TextAppearanceSpan mHighlightTextAppearanceSpan;
	private OnOrderStatusChangedListener mOrderStatusListener;

	private Spinner doSpinner;
	private String doType;
	private boolean onlyView;
	private Bundle mBundle;

	public interface OnOrderStatusChangedListener {
		public void onOrderStatusChanged();
	}

	public void setOrderStatusListener(OnOrderStatusChangedListener listener) {
		mOrderStatusListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.exchange_order_detail_fragment, container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mHeaderView = inflater.inflate(R.layout.return_order_detail_header, null, false);
		mHeaderView.setVisibility(View.GONE);
		mHeaderContentView = inflater.inflate(R.layout.order_view_header_content, null, false);
		mHeaderContentView.setVisibility(View.GONE);
		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer, null, false);
		mListFooterView.setVisibility(View.GONE);
		mListSpinnerFooterView = inflater.inflate(R.layout.exchange_order_detail_spinner_footer, null, false);
		mListSpinnerFooterView.setVisibility(View.GONE);
		doSpinner = (Spinner) mListSpinnerFooterView.findViewById(R.id.exchange_operation_type);
		doSpinner.setVisibility(View.VISIBLE);
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.addHeaderView(mHeaderContentView, null, false);
		mListView.addFooterView(mListFooterView);
		mListView.addFooterView(mListSpinnerFooterView);
		mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding), 0, getResources()
				.getDimensionPixelSize(R.dimen.list_item_padding), getResources()
				.getDimensionPixelSize(R.dimen.list_item_padding));

		mOrderIdText = (TextView) view.findViewById(R.id.order_id);
		mOrderStatusText = (TextView) view.findViewById(R.id.order_status);
		mOrderFeeText = (TextView) view.findViewById(R.id.order_fee);
		mOrderFeeSubtotalText = (TextView) mHeaderView.findViewById(R.id.order_fee_subtotal);
		mExpressView = view.findViewById(R.id.order_express_info);
		mOrderAddTime = (TextView) view.findViewById(R.id.add_time);
		mOrderInvoiceText = (TextView) view.findViewById(R.id.invoice_info);
		mPayButton = (Button) view.findViewById(R.id.confirm_btn);
		mActionContainer = view.findViewById(R.id.action_container);
		mOperationContainer = mListSpinnerFooterView.findViewById(R.id.exchange_operation_spinner);
		
		mHighlightTextAppearanceSpan = new TextAppearanceSpan(getActivity(), R.style.TextAppearance_Notice_Medium);

		mPaymentInfoView = mHeaderContentView.findViewById(R.id.payment_info);
		mPayTimeText = (TextView) mHeaderContentView.findViewById(R.id.payment_info_time);
		mPayNoText = (TextView) mHeaderContentView.findViewById(R.id.payment_info_refno);
		mUserInfoView = mHeaderContentView.findViewById(R.id.user_info);
		mUserNameText = (TextView) mHeaderContentView.findViewById(R.id.receive_user_name);
		mUserTelText = (TextView) mHeaderContentView.findViewById(R.id.receive_user_tel);
		mUserEmailText = (TextView) mHeaderContentView.findViewById(R.id.receive_user_email);

		handleIntent();

		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
//		mLoadingView.setEmptyText(R.string.order_list_empty);
		return view;
	}

	private void handleIntent() {
		mBundle = getArguments();
		if (mBundle != null) {
			mOrderId = mBundle.getString(Constants.Intent.EXTRA_EXCHANGE_ORDER_SN_STR);
			onlyView = mBundle.getBoolean(Constants.Intent.EXTRA_ONLY_VIEW);
		}else{
			mBundle = new Bundle();
		}
		LogUtil.d(TAG, mOrderId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(ORDER_INFO_LOADER, null, mOrderInfoCallback);
		mAdapter = new OrderViewProductAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mPayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				SameDayExchangeActivity father = (SameDayExchangeActivity) getActivity();
				mBundle.putString(Constants.Intent.EXTRA_RETURN_ORDER_SN_STR, mOrderId);
				father.showFragment(SameDayExchangeActivity.Fragments.TAG_EXCHANGE_LIST_FRAGMENT, mBundle, true);
			}
		});

		doSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mLoader.reload();
	}

	
	private LoaderCallbacks<ExchangeOrderInfoLoader.Result> mOrderInfoCallback = new LoaderCallbacks<ExchangeOrderInfoLoader.Result>() {
		@Override
		public Loader onCreateLoader(int id, Bundle arg1) {
			if (id == ORDER_INFO_LOADER) {
				mLoader = new ExchangeOrderInfoLoader(getActivity());
				mLoader.setNeedSecurityKeyTask(false);
				mLoader.setNeedDatabase(false);
				mLoader.setOrderId(mOrderId);
				mLoader.setProgressNotifiable(mLoadingView);
				return mLoader;
			}
			return null;
		}

		@Override
		public void onLoadFinished(Loader<ExchangeOrderInfoLoader.Result> loader, ExchangeOrderInfoLoader.Result data) {
			LogUtil.d(TAG, "info loaded.");
			if (data != null && data.mOrderInfo != null) {
				orderStatusType = data.mOrderInfo.getmPayStatus();
				mOrderId = data.mOrderInfo.getOrderId();
				ArrayList<ProductBrief>  productSnList = data.mOrderInfo.getmProductSnList();
				if(productSnList != null && productSnList.size() > 0){
					saveProductSnList(productSnList);
				}
				orderType = data.mOrderInfo.getOrderType();
				mHeaderView.setVisibility(View.VISIBLE);
				mHeaderContentView.setVisibility(View.VISIBLE);
				mListFooterView.setVisibility(View.VISIBLE);
				mActionContainer.setVisibility(View.VISIBLE);
				mOperationContainer.setVisibility(View.VISIBLE);
				Activity activity = getActivity();
				// product list
				mAdapter.updateData(data.mOrderInfo.getProductList());
				// order id
				mOrderIdText.setText(data.mOrderInfo.getOrderId());
				
				// invoice info
				mOrderInvoiceText.setText(data.mOrderInfo.getInvoiceInfo());
				// add time
				if (data.mOrderInfo.getAddTime() != null && data.mOrderInfo.getAddTime().length() > 0) {
					mOrderAddTime.setText(Utils.DateTime.formatTime(getActivity(), String.valueOf(Long.parseLong(data.mOrderInfo.getAddTime()) / 1000)));
				}

				if (data.mOrderInfo.isMihomeBuy()) {
					mExpressView.setVisibility(View.GONE);
				}

				// order status
				SpannableStringBuilder orderStatusSB = new SpannableStringBuilder(activity.getString(R.string.order_view_status,
						data.mOrderInfo.getOrderStatusInfo()));

				mOrderFee = Utils.Money.valueOf(data.mOrderInfo.getFee());
				SpannableStringBuilder orderFeeSB = new SpannableStringBuilder(activity.getString(R.string.order_view_fee, mOrderFee));
				
				mUserInfoView.setVisibility(View.VISIBLE);
				mUserNameText.setText(data.mOrderInfo.getOrderUserName());
				mUserTelText.setText(data.mOrderInfo.getOrderUserTel());
				mUserEmailText.setText(data.mOrderInfo.getOrderUserEmail());
				mOrderFeeText.setText(orderFeeSB);
				mOrderFeeSubtotalText.setText(getString(R.string.order_fee_subtotal_text, data.mOrderInfo.getFee()));
				mOrderStatusText.setText(orderStatusSB);
			} else {
				if (TextUtils.isEmpty(data.mOrderError)) {
					mLoadingView.setEmptyText(R.string.order_err);
				} else {
					mLoadingView.setEmptyText(data.mOrderError);
				}
			}
		}
		
		private void saveProductSnList(ArrayList<ProductBrief> productSnList) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(productSnList);
			} catch (IOException e) {
				e.printStackTrace();
			}

			SharedPreferences mySharedPreferences = getActivity().getSharedPreferences(Constants.productSnCache, Activity.MODE_PRIVATE);
			// 将Product对象转换成byte数组，并将其进行base64编码
			String productBase64 = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			// 将编码后的字符串写到base64.xml文件中
			editor.putString("productSnList", productBase64);
			editor.commit();
		}

		@Override
		public void onLoaderReset(Loader<ExchangeOrderInfoLoader.Result> loader) {
		}
	};

}
