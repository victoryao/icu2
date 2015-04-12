package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.SecondCategoryFragment;
import com.xiaomi.xms.sales.util.Constants;

public class SecondCategoryActivity extends BaseActivity {

    private String mCategoryId;
    private String mCategoryName;
    private TextView mTitleText;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCustomContentView(R.layout.second_category_activity);
        mTitleText = new TextView(this);
        mTitleText.setTextAppearance(this, R.style.TextAppearance_Title_Bar);
        mTitleText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mTitleText.setGravity(Gravity.CENTER_HORIZONTAL);
        setLeftView(mTitleText);
        if (handleIntent()) {
            SecondCategoryFragment secondCategoryFragment = (SecondCategoryFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.second_catrgory_fragment);
            secondCategoryFragment.setCateId(mCategoryId);
        }
    }

    private boolean handleIntent() {
        Intent intent = getIntent();
        mCategoryId = intent.getStringExtra(Constants.Intent.EXTRA_CATEGORY_ID);
        mCategoryName = intent.getStringExtra(Constants.Intent.EXTRA_CATEGORY_NAME);
        mTitleText.setText(mCategoryName);
        if (!TextUtils.isEmpty(mCategoryId)) {
            return true;
        }
        return false;
    }
}
