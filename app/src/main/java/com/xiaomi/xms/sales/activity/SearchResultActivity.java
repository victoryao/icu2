
package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.SearchResultFragment;

public class SearchResultActivity extends BaseActivity {
    public final static String TAG_SEARCH_RESULT_FRAGMENT = "search_result_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.search_result_activity);
        showFragment(TAG_SEARCH_RESULT_FRAGMENT, getIntent().getExtras(), false);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TextUtils.equals(tag, TAG_SEARCH_RESULT_FRAGMENT)) {
            fragment = new SearchResultFragment();
        }
        return fragment;
    }
}
