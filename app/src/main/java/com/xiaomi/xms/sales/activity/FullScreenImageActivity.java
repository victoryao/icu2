package com.xiaomi.xms.sales.activity;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.HomeFullScreenFragment;
import com.xiaomi.xms.sales.ui.ProductDetailFullScreenFragment;
import com.xiaomi.xms.sales.util.Constants;

public class FullScreenImageActivity extends BaseActivity {
    public static final String TAG_HOME_FULL_SCREEN_FRAGMENT = "home_full_screen_fragment";
    public static final String TAG_PRODUCT_DETAIL_FULL_SCREEN_FRAGMENT = "product_detail_full_screen_fragment";
    private String mCurrentFragmentTag;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.full_screen_image_activity);
        setTitleBarEnable(false);
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras == null || TextUtils.isEmpty(extras.getString(Constants.Intent.EXTRA_GO_TO_FRAGMENT))) {
            finish();
        }
        mCurrentFragmentTag = extras.getString(Constants.Intent.EXTRA_GO_TO_FRAGMENT);
        showFragment(mCurrentFragmentTag, extras, false);
    }

    @Override
    protected Fragment newFragmentByTag(String tag) {
        Fragment fragment = null;
        if (TAG_HOME_FULL_SCREEN_FRAGMENT.equals(tag)) {
            fragment = new HomeFullScreenFragment();
        } else if (TAG_PRODUCT_DETAIL_FULL_SCREEN_FRAGMENT.equals(tag)) {
            fragment = new ProductDetailFullScreenFragment();
        }
        return fragment;
    }

    @Override
    public void showFragment(String tag, Bundle bundle, boolean addToBackStack) {
        mCurrentFragmentTag = tag;
        super.showFragment(tag, bundle, addToBackStack);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                && TAG_HOME_FULL_SCREEN_FRAGMENT.equals(mCurrentFragmentTag)) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!TAG_HOME_FULL_SCREEN_FRAGMENT.equals(mCurrentFragmentTag)) {
            super.onBackPressed();
        } else {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration cf= this.getResources().getConfiguration();
        if(cf.orientation == cf.ORIENTATION_PORTRAIT && TAG_HOME_FULL_SCREEN_FRAGMENT.equals(mCurrentFragmentTag)){
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
