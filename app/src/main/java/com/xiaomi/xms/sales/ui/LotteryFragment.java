package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.AsynExceptionOrderService;
import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.SameDayExchangeActivity;
import com.xiaomi.xms.sales.activity.SameDayReturnActivity;
import com.xiaomi.xms.sales.nfc.NfcActivity;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.zxing.ScannerActivity;

public class LotteryFragment extends BaseFragment {

	private static final String TAG = "LotteryFragment";
	private View mNfcFindView;
	private View mScanView;
	private View mNfcAddView;
	private View mXianhuoSalesView; // 现货销售扫描商品
	private View mSameDayReturnView; // 当日退货View
	private View mSameDayExchangeView; // 当日换货View

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.lottery_fragment, container, false);
		mScanView = view.findViewById(R.id.account_1d_find);
		mScanView.setOnClickListener(mScanClickListner);
		mNfcFindView = view.findViewById(R.id.account_nfc_find);
		mNfcFindView.setOnClickListener(mNfcClickListner);
		mNfcAddView = view.findViewById(R.id.account_nfc_add);
		mNfcAddView.setOnClickListener(mNfcAddClickListner);
		mSameDayReturnView = view.findViewById(R.id.same_day_return);
		mSameDayReturnView.setOnClickListener(mSameDayReturnClickListner);
		mSameDayExchangeView = view.findViewById(R.id.same_day_exchange);
		mSameDayExchangeView.setOnClickListener(mSameDayExchangeClickListner);
		
		mXianhuoSalesView = view.findViewById(R.id.xianhuo_sales);
		mXianhuoSalesView.setOnClickListener(mXianhuoSalesClickListner);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private OnClickListener mXianhuoSalesClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!LoginManager.getInstance().hasLogin()) {
				((BaseActivity) getActivity()).gotoAccount();
				ToastUtil.show(getActivity(), R.string.mihome_buy_no_login);
				return;
			}
			if(canAccessOrdersubmit()){
				Intent intent = new Intent(getActivity(), ScannerActivity.class);
				intent.setAction(Constants.Intent.ACTION_XIANHUO_SCAN);
				getActivity().startActivity(intent);
				Intent intent1 = new Intent(getActivity(), AsynExceptionOrderService.class);
				getActivity().startService(intent1);
			}else{
				ToastUtil.show(getActivity(), R.string.test_env_warning);
			}
			
		}

		
	};

	private boolean canAccessOrdersubmit() {
		if(HostManager.isTest){
			String[] testerList = getResources().getStringArray(R.array.testerlist);
			String currentUserId = LoginManager.getInstance().getUserId();
			for(int i=0;i<testerList.length;i++){
				if(testerList[i].equals(currentUserId)){
					return true;
				}
			}
			return false;
		}else{
			return true;
		}
		
	}
	
	private OnClickListener mSameDayReturnClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!LoginManager.getInstance().hasLogin()) {
				((BaseActivity) getActivity()).gotoAccount();
				ToastUtil.show(getActivity(), R.string.mihome_buy_no_login);
				return;
			}
			String auths = Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_AUTHS, "");
			if(!auths.contains("DAY_REFUND")){
				ToastUtil.show(getActivity(), R.string.same_day_return_auth_warning);
				return;
			}
			Intent intent = new Intent(getActivity(), SameDayReturnActivity.class);
			getActivity().startActivity(intent);
		}
	};

	private OnClickListener mSameDayExchangeClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!LoginManager.getInstance().hasLogin()) {
				((BaseActivity) getActivity()).gotoAccount();
				ToastUtil.show(getActivity(), R.string.mihome_buy_no_login);
				return;
			}
			String auths = Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_AUTHS, "");
			if(!auths.contains("DAY_REFUND")){
				ToastUtil.show(getActivity(), R.string.same_day_exchange_auth_warning);
				return;
			}
			Intent intent = new Intent(getActivity(), SameDayExchangeActivity.class);
			getActivity().startActivity(intent);
		}
	};
	
	private OnClickListener mScanClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!LoginManager.getInstance().hasLogin()) {
				((BaseActivity) getActivity()).gotoAccount();
				ToastUtil.show(getActivity(), R.string.mihome_buy_no_login);
				return;
			}
			Intent intent = new Intent(getActivity(), ScannerActivity.class);
			intent.setAction(Constants.Intent.ACTION_PRODUCT_SCAN);
			getActivity().startActivity(intent);
		}
	};

	private OnClickListener mNfcClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!LoginManager.getInstance().hasLogin()) {
				((BaseActivity) getActivity()).gotoAccount();
				ToastUtil.show(getActivity(), R.string.mihome_buy_no_login);
				return;
			}
			Intent intent = new Intent(getActivity(), NfcActivity.class);
			intent.putExtra(Constants.Intent.EXTRA_ENTER_NFC_TYPE, true);
			getActivity().startActivity(intent);
		}
	};

	private OnClickListener mNfcAddClickListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (LoginManager.getInstance().hasLogin()) {
				Intent intent = new Intent(getActivity(), ScannerActivity.class);
				intent.setAction(Constants.Intent.ACTION_PRODUCT_SCANNER);
				startActivity(intent);
			} else {
				((BaseActivity) getActivity()).gotoAccount();
			}
		}
	};
}
