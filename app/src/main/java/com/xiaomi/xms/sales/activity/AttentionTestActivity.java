package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.AttentionNumberTestFragment;
import com.xiaomi.xms.sales.ui.AttentionPictureTestFragment;
import com.xiaomi.xms.sales.ui.AttentionResultFragment;
import com.xiaomi.xms.sales.ui.AttentionWordTestFragment;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.ReturnOrderDetailFragment;
import com.xiaomi.xms.sales.util.Constants;

public class AttentionTestActivity extends BaseActivity {

	public static class Fragments {
		public static final String TAG_ATTENTION_NUMBER_TEST_FRAGMENT = "attention_number_test_fragment";
		public static final String TAG_ATTENTION_PICTURE_TEST_FRAGMENT = "attention_picture_test_fragment";
		public static final String TAG_ATTENTION_WORD_TEST_FRAGMENT = "attention_word_test_fragment";
        public static final String TAG_ATTENTION_RESULT_FRAGMENT = "attention_result_fragment";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.same_day_return_activity);
		Bundle bundle = getIntent().getExtras();
		handleIntent(bundle);
		setShoppingBarEnable(false);
	}

	private void handleIntent(Bundle bundle) {
		if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_ATTENTION_PICTURE_TEST_ACTION)) {
			showFragment(Fragments.TAG_ATTENTION_PICTURE_TEST_FRAGMENT, bundle, false);
		}else if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_ATTENTION_NUMBER_TEST_ACTION)){
			showFragment(Fragments.TAG_ATTENTION_NUMBER_TEST_FRAGMENT, bundle, false);
		}else if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_ATTENTION_WORD_TEST_ACTION)){
			showFragment(Fragments.TAG_ATTENTION_WORD_TEST_FRAGMENT, bundle, false);
		}else if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_ATTENTION_RESULT_ACTION)){
            showFragment(Fragments.TAG_ATTENTION_RESULT_FRAGMENT, bundle, false);
        }
		
		
	}

	@Override
	protected Fragment newFragmentByTag(String tag) {
		Fragment fragment = null;
		if (Fragments.TAG_ATTENTION_NUMBER_TEST_FRAGMENT.equals(tag)) {
			fragment = new AttentionNumberTestFragment();
			setTitle(R.string.number_test);
		}else if (Fragments.TAG_ATTENTION_PICTURE_TEST_FRAGMENT.equals(tag)) {
			fragment = new AttentionPictureTestFragment();
			setTitle(R.string.picture_test);
		}else if (Fragments.TAG_ATTENTION_WORD_TEST_FRAGMENT.equals(tag)) {
			fragment = new AttentionWordTestFragment();
			setTitle(R.string.word_test);
		}else if (Fragments.TAG_ATTENTION_RESULT_FRAGMENT.equals(tag)) {
            fragment = new AttentionResultFragment();
            setTitle(R.string.cam_icu_title);
        }
		return fragment;
	}

	public BaseFragment getFragment(String tag) {
		return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
	}

}
