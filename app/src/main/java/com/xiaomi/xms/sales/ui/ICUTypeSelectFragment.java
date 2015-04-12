package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.CAMICUMainTestActivity;
import com.xiaomi.xms.sales.activity.ICUTestActivity;
import com.xiaomi.xms.sales.activity.SameDayExchangeActivity;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.zxing.ScannerActivity;

public class ICUTypeSelectFragment extends BaseFragment {

	private View mScanOrderAndSNView;

	private View mSearchButton;
	private View mSubmitBtn;
	private View mPatientInfoContainer;
	private TextView mOrderSNInputView;
	private Bundle mBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.icu_type_select_fragment, container, false);
		mBundle = getArguments();
		if(mBundle == null){
			mBundle = new Bundle();
		}
		Button cam_icu_btn = (Button)view.findViewById(R.id.CAM_ICU);
		cam_icu_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(), CAMICUMainTestActivity.class);
//				Intent intent = new Intent(getActivity(), ICUTestActivity.class);
				startActivity(intent);
				getActivity().finish();
			}
			
		});
		return view;
	}

	private OnClickListener mScanOrderAndSNViewClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), ScannerActivity.class);
			intent.setAction(Constants.Intent.ACTION_PATIENT_SEARCH_SCAN);
			startActivity(intent);
			Utils.SoftInput.hide(getActivity(), mSearchButton.getWindowToken());
		}

	};

	private OnClickListener mSearchClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.search_order_sn_button:
				if(mOrderSNInputView.getText().toString().trim().equals("")){
					ToastUtil.show(getActivity(), R.string.patient_id_null_warning);
					return;
				}
				mPatientInfoContainer.setVisibility(View.VISIBLE);
				Utils.SoftInput.hide(getActivity(), mSearchButton.getWindowToken());
//				searchOrderIdOrSN();
				break;

			}
		}

		private void searchOrderIdOrSN() {
			Utils.SoftInput.hide(getActivity(), mOrderSNInputView.getWindowToken());
			SameDayExchangeActivity father = (SameDayExchangeActivity) getActivity();
			String toSearchStr = mOrderSNInputView.getText().toString();
			mBundle.putString(Constants.Intent.EXTRA_EXCHANGE_ORDER_SN_STR, toSearchStr);
			father.showFragment(SameDayExchangeActivity.Fragments.TAG_EXCHANGE_ORDER_DETAIL_FRAGMENT, mBundle, true);
		}
	};
}
