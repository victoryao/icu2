
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.MiPhoneProductFragment;
import com.xiaomi.xms.sales.ui.ProductFragment;
import com.xiaomi.xms.sales.util.Constants;

public class ProductActivity extends BaseActivity {

    public static String TAG_NORMAL_PRODUCT_FRAGMENT = "normal_product_fragment";
    public static String TAG_MIPHONE_PRODUCT_FRAGMENT = "miphone_product_fragment";
    private String mCategoryId;
    private String mCategoryName;
    private String mDataType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.product_activity);
        if (handleIntent()) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Intent.EXTRA_CATEGORY_ID, mCategoryId);
            if (TextUtils.equals("2", mDataType)) {
                showFragment(TAG_NORMAL_PRODUCT_FRAGMENT, bundle, false);
            } else {
                showFragment(TAG_NORMAL_PRODUCT_FRAGMENT, bundle, false);
            }
        }
    }

    
    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_NORMAL_PRODUCT_FRAGMENT.equals(tag)) {
            fragment = new ProductFragment();
        } else if (TAG_MIPHONE_PRODUCT_FRAGMENT.equals(tag)) {
            fragment = new MiPhoneProductFragment();
        }
        return fragment;
    }

    public BaseFragment getFragment(String tag) {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

    private boolean handleIntent() {
        Intent intent = getIntent();
        mCategoryId = intent.getStringExtra(Constants.Intent.EXTRA_CATEGORY_ID);
        mCategoryName = intent.getStringExtra(Constants.Intent.EXTRA_CATEGORY_NAME);
        mDataType = intent.getStringExtra(Constants.Intent.EXTRA_CATEGORY_DATA_TYPE);
        setTitle(mCategoryName);
        if (!TextUtils.isEmpty(mCategoryId)) {
            return true;
        }
        return false;
    }
}
