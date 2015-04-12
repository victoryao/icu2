package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.ICUHistoryFragment;

public class IcuHistoryActivity extends BaseActivity {
	
	public int existCountInStack =1;

	public static class Fragments {
		public static final String TAG_ICU_HISTORY_FRAGMENT = "icu_history_fragment";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.icu_action_activity);
		Bundle bundle = getIntent().getExtras();
		handleIntent(bundle);
		setHomeButtonEnable(false);
		setShoppingBarEnable(false);
	}

	

	private void handleIntent(Bundle bundle) {
		showFragment(Fragments.TAG_ICU_HISTORY_FRAGMENT, bundle, false);
	}

	@Override
	protected Fragment newFragmentByTag(String tag) {
		Fragment fragment = null;
		if (Fragments.TAG_ICU_HISTORY_FRAGMENT.equals(tag)) {
			fragment = new ICUHistoryFragment();
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
