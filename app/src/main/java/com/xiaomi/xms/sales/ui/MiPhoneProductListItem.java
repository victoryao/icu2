
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.CampaignActivity;
import com.xiaomi.xms.sales.activity.FCodeActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.MiPhoneInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class MiPhoneProductListItem extends BaseListItem<MiPhoneInfo> {

    private ImageView mPhoneImage;
    private TextView mPriceView;
    private TextView mPhoneNameView;
    private TextView mPhonedescView;
    private TextView mPhoneActivityView;
    private TextView mPhoneActivityView2;
    private Button mFcodeButton;
    private Button mActivtyButton;
    private Button mImmdeButton;
    private View mView;
    private Context mContext;
    private MiPhoneInfo mProductInfo;

    public MiPhoneProductListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void bind(MiPhoneInfo data) {
        mProductInfo = data;
        mPhoneNameView.setText(data.getProductName());
        mPhonedescView.setText(data.getBrief());
        mPhoneActivityView.setText(data.getDescribe());
        mPhoneActivityView2.setText(data.getDescribe2());
        mPriceView.setText(getResources().getString(R.string.rmb_identification,
                data.getProductPrice()));
        ImageLoader.getInstance().loadImage(mPhoneImage, data.getImage(),
                R.drawable.default_pic_small);
        mView.setOnClickListener(mClickListener);
        mFcodeButton.setText(data.getLeftText());
        mActivtyButton.setText(data.getRightText());
        mFcodeButton.setOnClickListener(mClickListener);
        mActivtyButton.setOnClickListener(mClickListener);
        mImmdeButton.setOnClickListener(mClickListener);
        if(TextUtils.isEmpty(data.getLeftUrl()) && !TextUtils.isEmpty(data.getRightUrl())) {
            mImmdeButton.setText(data.getRightText());
            mImmdeButton.setVisibility(VISIBLE);
            mActivtyButton.setVisibility(GONE);
            mFcodeButton.setVisibility(GONE);
        } else if (!TextUtils.isEmpty(data.getLeftUrl()) && TextUtils.isEmpty(data.getRightUrl())) {
            mImmdeButton.setText(data.getLeftText());
            mImmdeButton.setVisibility(VISIBLE);
            mActivtyButton.setVisibility(GONE);
            mFcodeButton.setVisibility(GONE);
        } else if (!TextUtils.isEmpty(data.getLeftUrl()) && !TextUtils.isEmpty(data.getRightUrl())){
            mImmdeButton.setVisibility(GONE);
            mActivtyButton.setVisibility(VISIBLE);
            mFcodeButton.setVisibility(VISIBLE);
        } else if (TextUtils.isEmpty(data.getLeftUrl()) && TextUtils.isEmpty(data.getRightUrl())) {
            mImmdeButton.setVisibility(GONE);
            mActivtyButton.setVisibility(GONE);
            mFcodeButton.setVisibility(GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPhoneImage = (ImageView) findViewById(R.id.miphone_image);
        mPriceView = (TextView) findViewById(R.id.miphone_product_price);
        mPhoneNameView = (TextView) findViewById(R.id.miphone_product_name);
        mPhonedescView = (TextView) findViewById(R.id.miphone_product_description);
        mPhoneActivityView = (TextView) findViewById(R.id.miphone_activity_description);
        mPhoneActivityView2 = (TextView) findViewById(R.id.miphone_activity_description2);
        mFcodeButton = (Button) findViewById(R.id.f_buy_btn);
        mActivtyButton = (Button) findViewById(R.id.activity_btn);
        mImmdeButton = (Button) findViewById(R.id.immd_buy_btn);
        mView = findViewById(R.id.desc_container);

    }

    private OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.immd_buy_btn:
                    immediatelyBuyMiPhone();
                    break;
                case R.id.activity_btn:
                    activityMiPhone();
                    break;
                case R.id.f_buy_btn:
                    fCodeBuyMiPhone();
                    break;
                case R.id.desc_container:
                    gotoDetailPage();
                    break;
            }
        }
    };

    private void immediatelyBuyMiPhone() {
        String urlString = "";
        if (TextUtils.isEmpty(mProductInfo.getLeftUrl())
                && !TextUtils.isEmpty(mProductInfo.getRightUrl())) {
            urlString = mProductInfo.getRightUrl();
        } else if (!TextUtils.isEmpty(mProductInfo.getLeftUrl())
                && TextUtils.isEmpty(mProductInfo.getRightUrl())) {
            urlString = mProductInfo.getLeftUrl();
        }
        if (LoginManager.getInstance().hasLogin()) {
            CampaignActivity.startActivityStandard((BaseActivity) mContext, urlString);
        } else {
            ToastUtil.show(mContext, mContext.getString(R.string.please_login));
            ((BaseActivity) mContext).gotoAccount();
        }
    }

    private void activityMiPhone() {
        if (LoginManager.getInstance().hasLogin()) {
            String url = mProductInfo.getRightUrl();
            LogUtil.d("immediatelyBuyMiPhone", url);
            CampaignActivity.startActivityStandard((BaseActivity) mContext, url);
        } else {
            ToastUtil.show(mContext, mContext.getString(R.string.please_login));
            ((BaseActivity) mContext).gotoAccount();
        }
    }

    private void fCodeBuyMiPhone() {
        if (LoginManager.getInstance().hasLogin()) {
            if (TextUtils.equals(mProductInfo.getLeftUrl(), "fcode")) {
                Intent intent = new Intent(mContext, FCodeActivity.class);
                mContext.startActivity(intent);
            } else {
                String url = mProductInfo.getLeftUrl();
                LogUtil.d("fCodeBuyMiPhone", url);
                CampaignActivity.startActivityStandard((BaseActivity) mContext, url);
            }

        } else {
            ToastUtil.show(mContext, mContext.getString(R.string.fcode_buy_please_login));
            ((BaseActivity) mContext).gotoAccount();
        }
    }

    private void gotoDetailPage() {
        Intent intent = new Intent();
        if (Tags.Product.DISPLAY_BROWSER.equals(mProductInfo.getDisplayType())) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mProductInfo.getProductUrl()));
        } else if (Tags.Product.DISPLAY_WEB.equals(mProductInfo.getDisplayType())) {
            // 应用中的Web界面打开
            intent.setClass(mContext, CampaignActivity.class);
            intent.putExtra(Constants.Intent.EXTRA_COMPAIGN_URL, mProductInfo.getProductUrl());
            CampaignActivity.startActivityStandard((BaseActivity) mContext,
                    mProductInfo.getProductUrl());
            return;
        } else if (Tags.Product.DISPLAY_NATIVE.equals(mProductInfo.getDisplayType())
                && !TextUtils.isEmpty(mProductInfo.getProductId())) {
            // 本地应用打开
            intent.setClass(mContext, ProductDetailsActivity.class);
            intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductInfo.getProductId());
            intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
            intent.putExtra(Constants.Intent.EXTRA_MIPHONE_NAME,
                    mProductInfo.getProductName());
        }
        mContext.startActivity(intent);
    }
}
