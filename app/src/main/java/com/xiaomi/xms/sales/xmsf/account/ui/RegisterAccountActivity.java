
package com.xiaomi.xms.sales.xmsf.account.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;

/**
 * Activity that holds registration steps fragments. Registration state should
 * be saved in the situation where activity is re-created.
 */
public class RegisterAccountActivity extends BaseActivity {
    private static final String TAG = "RegisterAccountActivity";

    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.register_activity);
        setTitle(R.string.title_reg);

        mFragmentManager = getSupportFragmentManager();

        RegisterSelectAccountTypeFragment f = new RegisterSelectAccountTypeFragment();
        mFragmentManager.beginTransaction().add(R.id.fragment_container, f).commitAllowingStateLoss();
        setShoppingBarEnable(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
