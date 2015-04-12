package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.CamICUAttentionTestBeginFragment;

public class CAMICUAttentionTestActivity extends BaseActivity {

	public int existCountInStack = 1;

	public static class Fragments {
		public static final String TAG_CAM_ICU_ATTENTION_TEST_BEGIN_FRAGMENT = "cam_icu_attention_test_begin_fragment";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.icu_type_select_activity);
		Bundle bundle = getIntent().getExtras();
		handleIntent(bundle);
		setHomeButtonEnable(true);
		setShoppingBarEnable(true);
	}

	private void handleIntent(Bundle bundle) {
		showFragment(Fragments.TAG_CAM_ICU_ATTENTION_TEST_BEGIN_FRAGMENT, bundle, false);
	}

	@Override
	protected Fragment newFragmentByTag(String tag) {
		Fragment fragment = null;
		if (Fragments.TAG_CAM_ICU_ATTENTION_TEST_BEGIN_FRAGMENT.equals(tag)) {
			fragment = new CamICUAttentionTestBeginFragment();
			setTitle(R.string.cam_icu_title);
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
