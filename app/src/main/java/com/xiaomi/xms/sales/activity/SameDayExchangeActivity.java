package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.ExchangeListFragment;
import com.xiaomi.xms.sales.ui.ExchangeOrderDetailFragment;
import com.xiaomi.xms.sales.ui.ExchangeSearchOrderFragment;
import com.xiaomi.xms.sales.util.Constants;

public class SameDayExchangeActivity extends BaseActivity {
	
	public int existCountInStack =1;

	public static class Fragments {
		public static final String TAG_EXCHANGE_SEARCH_ORDER_FRAGMENT = "exchange_search_order_fragment";
		public static final String TAG_EXCHANGE_ORDER_DETAIL_FRAGMENT = "exchange_order_detail_fragment";
		public static final String TAG_EXCHANGE_LIST_FRAGMENT = "exchange_list_fragment";
		public static final String TAG_EXCHANGE_SCANNER_FRAGMENT = "exchange_scanner_fragment";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.same_day_exchange_activity);
		Bundle bundle = getIntent().getExtras();
		handleIntent(bundle);
	}

	

	private void handleIntent(Bundle bundle) {
		if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_SAMEDAYEXCHANGE_SCAN)) {
			showFragment(Fragments.TAG_EXCHANGE_ORDER_DETAIL_FRAGMENT, bundle, false);
		}else if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_SAMEDAYEXCHANGE_REPLACE_SN_SCAN)) {
			showFragment(Fragments.TAG_EXCHANGE_LIST_FRAGMENT, bundle, false);
		}else {
			showFragment(Fragments.TAG_EXCHANGE_SEARCH_ORDER_FRAGMENT, bundle, false);
		}
	}

	@Override
	protected Fragment newFragmentByTag(String tag) {
		Fragment fragment = null;
		if (Fragments.TAG_EXCHANGE_SEARCH_ORDER_FRAGMENT.equals(tag)) {
			fragment = new ExchangeSearchOrderFragment();
			setTitle(R.string.same_day_return_find_order_title);
		}else if (Fragments.TAG_EXCHANGE_ORDER_DETAIL_FRAGMENT.equals(tag)) {
			fragment = new ExchangeOrderDetailFragment();
			setTitle(R.string.same_day_exchange_order_detail_title);
		}else if (Fragments.TAG_EXCHANGE_LIST_FRAGMENT.equals(tag)) {
			fragment = new ExchangeListFragment();
			setTitle(R.string.same_day_exchange_list_title);
		}
		return fragment;
	}

	public BaseFragment getFragment(String tag) {
		return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
	}
	
	public void addExistCountInStack(){
		existCountInStack ++;
	}

	public int getExistCountInStack() {
		return existCountInStack;
	}

	public void setExistCountInStack(int existCountInStack) {
		this.existCountInStack = existCountInStack;
	}

}
