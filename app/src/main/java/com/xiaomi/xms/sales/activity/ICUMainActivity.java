package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.ICUSearchPatientFragment;
import com.xiaomi.xms.sales.ui.ICUTypeSelectFragment;

public class ICUMainActivity extends BaseActivity {

	public int existCountInStack = 1;

	public static class Fragments {
		public static final String TAG_ICU_SEARCH_PATIENT_FRAGMENT = "icu_search_patient_fragment";
		public static final String TAG_ICU_TYPE_SELECT_FRAGMENT = "icu_type_select_fragment";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.icu_type_select_activity);
		Bundle bundle = getIntent().getExtras();
		handleIntent(bundle);
		setHomeButtonEnable(false);
		setShoppingBarEnable(false);
	}

	private void handleIntent(Bundle bundle) {
		showFragment(Fragments.TAG_ICU_SEARCH_PATIENT_FRAGMENT, bundle, false);
	}

	@Override
	protected Fragment newFragmentByTag(String tag) {
		Fragment fragment = null;
		if (Fragments.TAG_ICU_SEARCH_PATIENT_FRAGMENT.equals(tag)) {
			fragment = new ICUSearchPatientFragment();
			setTitle(R.string.icu_find_patient_title);
		}else if(Fragments.TAG_ICU_TYPE_SELECT_FRAGMENT.equals(tag)) {
			setHomeButtonEnable(true);
			setShoppingBarEnable(true);
			fragment = new ICUTypeSelectFragment();
			setTitle(R.string.app_name);
		}
		return fragment;
	}

	public BaseFragment getFragment(String tag) {
		return (BaseFragment) getSupportFragmentManager()
				.findFragmentByTag(tag);
	}

	public void addExistCountInStack() {
		existCountInStack++;
	}

	public int getExistCountInStack() {
		return existCountInStack;
	}

	public void setExistCountInStack(int existCountInStack) {
		this.existCountInStack = existCountInStack;
	}

}
