package com.xiaomi.xms.sales.ui;

import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.OrderViewProductSnAdapter;
import com.xiaomi.xms.sales.ui.RASSTestFragment.OnOrderStatusChangedListener;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class RiskEnvironmentFragment extends BaseFragment {
	
	private static final String TAG = "RiskPatientFragment";
	private Button mConfirmButton;
	private BaseListView mListView;
	private View mListFooterView;
	private View mListRadioButtonFooterView;
	private View mActionContainer;
	private View mradioGroupContainer;
	private OrderViewProductSnAdapter mAdapter;
	private EmptyLoadingView mLoadingView;
	private String mOrderId;
	private String mOrderStatus;
	private OnOrderStatusChangedListener mOrderStatusListener;
	private HashMap<Integer, String> mFormDefaultValue = new HashMap<Integer, String>();
	private Bundle mBundle;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mAdapter = new OrderViewProductSnAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mActionContainer.setVisibility(View.VISIBLE);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.return_choice_fragment,
				container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		view.findViewById(R.id.rass_confirm_btn);
		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer,
				null, false);
		mListFooterView.setVisibility(View.VISIBLE);
		mListRadioButtonFooterView = inflater.inflate(
				R.layout.risk_environment_fragment, null, false);
		mListRadioButtonFooterView.setVisibility(View.VISIBLE);
		
		mListView.addFooterView(mListRadioButtonFooterView);
		mListView.addFooterView(mListFooterView);
		mListView
				.setPadding(
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding),
						0,
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding),
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding));
		mActionContainer = view.findViewById(R.id.action_container);
	

		mConfirmButton = (Button) view.findViewById(R.id.rass_confirm_btn);

		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mLoadingView.setEmptyText(R.string.order_list_empty);
		
		return view;
	}

}
