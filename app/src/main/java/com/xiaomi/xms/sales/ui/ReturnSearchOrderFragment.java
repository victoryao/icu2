package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.SameDayReturnActivity;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.zxing.ScannerActivity;

public class ReturnSearchOrderFragment extends BaseFragment {

	private View mScanOrderAndSNView;

	private View mSearchButton;
	private TextView mOrderSNInputView;
	private Bundle mBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.return_search_order_fragment, container, false);
		mBundle = new Bundle();
		mScanOrderAndSNView = view.findViewById(R.id.scan_order_sn);
		mScanOrderAndSNView.setOnClickListener(mScanOrderAndSNViewClickListner);
		mOrderSNInputView = (TextView) view.findViewById(R.id.search_order_sn_input);
		mSearchButton = view.findViewById(R.id.search_order_sn_button);
		mSearchButton.setOnClickListener(mSearchClickListener);
		return view;
	}

	private OnClickListener mScanOrderAndSNViewClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!LoginManager.getInstance().hasLogin()) {
				((BaseActivity) getActivity()).gotoAccount();
				ToastUtil.show(getActivity(), R.string.mihome_buy_no_login);
				return;
			}
			Intent intent = new Intent(getActivity(), ScannerActivity.class);
			intent.setAction(Constants.Intent.ACTION_SAMEDAYRETURN_SCAN);
			startActivity(intent);
		}

	};

	private OnClickListener mSearchClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.search_order_sn_button:
				searchOrderIdOrSN();
				break;

			}
		}

		private void searchOrderIdOrSN() {
			Utils.SoftInput.hide(getActivity(), mOrderSNInputView.getWindowToken());
			SameDayReturnActivity father = (SameDayReturnActivity) getActivity();
			String toSearchStr = mOrderSNInputView.getText().toString();
			mBundle.putString(Constants.Intent.EXTRA_RETURN_ORDER_SN_STR, toSearchStr);
			father.showFragment(SameDayReturnActivity.Fragments.TAG_RETURN_ORDER_DETAIL_FRAGMENT, mBundle, true);

		}
	};
}
