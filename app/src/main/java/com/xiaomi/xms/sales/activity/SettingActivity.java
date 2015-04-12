
package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.view.View;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.SettingFragment;
import com.xiaomi.xms.sales.ui.SettingFragment.CheckUpdateListener;
import com.xiaomi.xms.sales.util.ToastUtil;

public class SettingActivity extends BaseActivity implements CheckUpdateListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.setting_activity);
        SettingFragment aboutFragment = (SettingFragment) getSupportFragmentManager()
                .findFragmentById(R.id.setting_fragment);
        aboutFragment.setCheckUpdateListener(this);
        setTitle(R.string.menu_setting);
        setShoppingBarEnable(false);
        mMenuItemSetting.setVisibility(View.GONE);
    }

    @Override
    public void onCheckUpdate() {
        mUserCheckUpdate = true;
        checkUpdate(true);
        ToastUtil.show(this, R.string.checking_update);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMenuItemSetting.setVisibility(View.VISIBLE);
    }
}
