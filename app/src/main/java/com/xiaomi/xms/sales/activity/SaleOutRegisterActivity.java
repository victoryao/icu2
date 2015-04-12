
package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.SaleOutRegisterResultFragment;

public class SaleOutRegisterActivity extends BaseActivity {
    public final static String TAG_RESULT_FRAGMENT = "result_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.sale_out_register_activity);
        setTitle(R.string.sale_out_registration);
        showFragment(TAG_RESULT_FRAGMENT, getIntent().getExtras(), false);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TextUtils.equals(tag, TAG_RESULT_FRAGMENT)) {
            // 缺货提醒
            fragment = new SaleOutRegisterResultFragment();
        }
        return fragment;
    }
}
