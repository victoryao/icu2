package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.ReturnBluetoothFragment;
import com.xiaomi.xms.sales.ui.ReturnChoiceFragment;
import com.xiaomi.xms.sales.ui.ReturnConfirmFragment;
import com.xiaomi.xms.sales.ui.ReturnOrderDetailFragment;
import com.xiaomi.xms.sales.ui.ReturnSearchOrderFragment;
import com.xiaomi.xms.sales.util.Constants;

public class SameDayReturnActivity extends BaseActivity {

	public static class Fragments {
		public static final String TAG_RETURN_SEARCH_ORDER_FRAGMENT = "return_search_order_fragment";
		public static final String TAG_RETURN_ORDER_DETAIL_FRAGMENT = "return_order_detail_fragment";
		public static final String TAG_RETURN_CHOICE_FRAGMENT = "return_choice_fragment";
		public static final String TAG_RETURN_CONFIRM_FRAGMENT = "return_confirm_fragment";
		public static final String TAG_RETURN_BLUETOOTH_FRAGMENT = "return_bluetooth_fragment";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.same_day_return_activity);
		Bundle bundle = getIntent().getExtras();
		handleIntent(bundle);
	}

	private void handleIntent(Bundle bundle) {
		if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_SAMEDAYRETURN_SCAN)) {
			showFragment(Fragments.TAG_RETURN_ORDER_DETAIL_FRAGMENT, bundle, false);
		}else {
			showFragment(Fragments.TAG_RETURN_SEARCH_ORDER_FRAGMENT, bundle, false);
		}
	}

	@Override
	protected Fragment newFragmentByTag(String tag) {
		Fragment fragment = null;
		if (Fragments.TAG_RETURN_SEARCH_ORDER_FRAGMENT.equals(tag)) {
			fragment = new ReturnSearchOrderFragment();
			setTitle(R.string.same_day_return_find_order_title);
		}else if (Fragments.TAG_RETURN_ORDER_DETAIL_FRAGMENT.equals(tag)) {
			fragment = new ReturnOrderDetailFragment();
			setTitle(R.string.same_day_return_order_detail_title);
		}else if (Fragments.TAG_RETURN_CHOICE_FRAGMENT.equals(tag)) {
			fragment = new ReturnChoiceFragment();
			setTitle(R.string.same_day_return_choice_title);
		}else if (Fragments.TAG_RETURN_CONFIRM_FRAGMENT.equals(tag)) {
			fragment = new ReturnConfirmFragment();
			setTitle(R.string.same_day_return_confirm_title);
		}else if(Fragments.TAG_RETURN_BLUETOOTH_FRAGMENT.equals(tag)) {
			fragment = new ReturnBluetoothFragment();
			setTitle(R.string.bluetooth_connect_title);
		}
		
		
		
		return fragment;
	}

	public BaseFragment getFragment(String tag) {
		return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
	}

}
